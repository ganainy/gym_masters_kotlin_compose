package com.ganainy.gymmasterscompose.ui.repository

import android.util.Log
import com.ganainy.gymmasterscompose.ui.models.Follow
import com.ganainy.gymmasterscompose.ui.models.Follow.Companion.FOLLOWED_ID
import com.ganainy.gymmasterscompose.ui.models.Follow.Companion.FOLLOWER_ID
import com.ganainy.gymmasterscompose.ui.models.Follow.Companion.FOLLOWS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.User
import com.ganainy.gymmasterscompose.ui.models.User.Companion.FOLLOWERS_COUNT
import com.ganainy.gymmasterscompose.ui.models.User.Companion.FOLLOWING_COUNT
import com.ganainy.gymmasterscompose.ui.models.User.Companion.USERS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.User.Companion.USER_STATS
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Social interactions (following/followers)
interface ISocialRepository {

    fun getUserFollowers(userId: String?): Flow<ResultWrapper<List<String>>>
    fun isFollowing(userIdToCheck: String): Flow<ResultWrapper<Boolean>> // Check if the current user is following the target user, returns Boolean
    suspend fun updateFollowState(userId: String): ResultWrapper<Unit>
    suspend fun getFollowedUsers(userId: String): ResultWrapper<List<String>>
}

class SocialRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ISocialRepository {


    override fun getUserFollowers(userId: String?): Flow<ResultWrapper<List<String>>> = callbackFlow {
        // Use the passed userId or the current user if null
        val targetUserId = userId ?: getCurrentUserIdSafe()

        if (targetUserId == null) {
            trySend(ResultWrapper.Error(Exception("Target User ID not available")))
            close()
            return@callbackFlow
        }

        val followsCollection = firestore.collection(FOLLOWS_COLLECTION)
        var listenerRegistration: ListenerRegistration? = null

        try {
            // Query where the followedId matches the targetUserId
            val query = followsCollection.whereEqualTo(FOLLOWED_ID, targetUserId)

            listenerRegistration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ResultWrapper.Error(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val followerIds = snapshot.documents.mapNotNull { document ->
                        try {
                            document.toObject(Follow::class.java)?.followerId // Extract followerId
                        } catch (e: Exception) {
                            Log.e("SocialRepo", "Error parsing Follow document: ${document.id}", e)
                            null
                        }
                    }
                    trySend(ResultWrapper.Success(followerIds))
                } else {
                    trySend(ResultWrapper.Success(emptyList())) // Or an error
                }
            }
        } catch (e: Exception) {
            trySend(ResultWrapper.Error(e))
            close(e)
        }

        awaitClose { listenerRegistration?.remove() }
    }


    override suspend fun getFollowedUsers(userId: String): ResultWrapper<List<String>> {
        // Validate userId
        if (userId.isBlank()) {
            return ResultWrapper.Error(Exception("User ID cannot be blank"))
        }
        return try {
            val followsCollection = firestore.collection(FOLLOWS_COLLECTION)

            // Query where the followerId matches the input userId
            val query = followsCollection.whereEqualTo(FOLLOWER_ID, userId)

            // Perform one-time read
            val snapshot = query.get().await()

            // Parse followed user IDs from the snapshot
            val followedIds = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Follow::class.java)?.followedId // Extract followedId
                } catch (e: Exception) {
                    Log.e("SocialRepo", "Error parsing Follow document: ${document.id}", e)
                    null
                }
            }

            ResultWrapper.Success(followedIds)
        } catch (e: Exception) {
            Log.e("SocialRepo", "Error fetching followed users for $userId", e)
            ResultWrapper.Error(e)
        }
    }

    private fun getCurrentUserIdSafe(): String? {
        return auth.currentUser?.uid
    }

    override fun isFollowing(userIdToCheck: String): Flow<ResultWrapper<Boolean>> = flow {
        // Emit loading state initially
        emit(ResultWrapper.Loading())

        val currentUserUid = getCurrentUserIdSafe()
            ?: throw AuthRepository.UserNotAuthenticatedException("User not logged in")

        if (userIdToCheck == currentUserUid) {
            emit(ResultWrapper.Success(false)) // Cannot follow self, emit success(false)
            return@flow // Complete the flow successfully
        }

        // Perform the Firestore operation directly
        val followId = Follow.createId(
            followerId = currentUserUid,
            followedId = userIdToCheck
        )
        val documentSnapshot = firestore.collection(FOLLOWS_COLLECTION)
            .document(followId)
            .get()
            .await()

        // Emit success if Firestore operation succeeds
        emit(ResultWrapper.Success(documentSnapshot.exists()))

    }.catch { e -> // Catch exceptions from the upstream flow block
        // Emit an error state when an exception occurs
        Log.e("SocialRepo", "Error in isFollowing flow for $userIdToCheck", e)
        // Ensure the emitted error is a ResultWrapper.Error
        emit(ResultWrapper.Error(e as? Exception ?: Exception(e)))
    }



    override suspend fun updateFollowState(userIdToFollowOrUnfollow: String): ResultWrapper<Unit> {
        val currentUserUid = getCurrentUserIdSafe()
            ?: return ResultWrapper.Error(AuthRepository.UserNotAuthenticatedException("User is not authenticated"))

        if (currentUserUid == userIdToFollowOrUnfollow) {
            return ResultWrapper.Error(Exception("Cannot follow/unfollow self"))
        }

        try {
            // 1. Check the current follow state first
            val followingResult = isFollowing(userIdToCheck = userIdToFollowOrUnfollow)
                .filterNot { it is ResultWrapper.Loading } // Wait for non-loading state
                .first() // Get the first non-loading result

            return when (followingResult) {
                is ResultWrapper.Error -> {
                    // Propagate the error from isFollowing check
                    Log.e("SocialRepo", "Error checking follow state: ${followingResult.exception}")
                    ResultWrapper.Error(followingResult.exception)
                }
                is ResultWrapper.Success -> {
                    val currentlyFollowing = followingResult.data
                    val followId = Follow.createId(currentUserUid, userIdToFollowOrUnfollow)

                    // 2. Create and execute an atomic WriteBatch
                    val batch = firestore.batch()

                    // References to user documents for stats updates
                    val targetUserDocRef = firestore.collection(USERS_COLLECTION).document(userIdToFollowOrUnfollow)
                    val currentUserDocRef = firestore.collection(USERS_COLLECTION).document(currentUserUid)

                    // References to follow document
                    val followDocRef = firestore.collection(FOLLOWS_COLLECTION).document(followId)

                    // Paths to nested stats fields
                    val targetUserFollowersPath = "$USER_STATS.$FOLLOWERS_COUNT"
                    val currentUserFollowingPath = "$USER_STATS.$FOLLOWING_COUNT"

                    if (currentlyFollowing) {
                        // --- Unfollow Logic ---
                        // Delete the follow document
                        batch.delete(followDocRef)
                        // Decrement target user's followers count
                        batch.update(targetUserDocRef, targetUserFollowersPath, FieldValue.increment(-1))
                        // Decrement current user's following count
                        batch.update(currentUserDocRef, currentUserFollowingPath, FieldValue.increment(-1))
                        Log.d("SocialRepo", "Batch prepared for UNFOLLOW")

                    } else {
                        // --- Follow Logic ---
                        // Create the Follow object
                        val followObject = Follow(
                            id = followId,
                            followerId = currentUserUid,
                            followedId = userIdToFollowOrUnfollow,
                            timestamp =  Timestamp.now()
                        )
                        // Create the follow document
                        batch.set(followDocRef, followObject)
                        // Increment target user's followers count
                        batch.update(targetUserDocRef, targetUserFollowersPath, FieldValue.increment(1))
                        // Increment current user's following count
                        batch.update(currentUserDocRef, currentUserFollowingPath, FieldValue.increment(1))
                        Log.d("SocialRepo", "Batch prepared for FOLLOW")
                    }

                    // 3. Commit the batch
                    batch.commit().await()
                    Log.d("SocialRepo", "Batch committed successfully")
                    ResultWrapper.Success(Unit)
                }
                is ResultWrapper.Loading -> {
                    // Should not happen due to filterNot/first, but handle defensively
                    ResultWrapper.Error(Exception("Unexpected loading state"))
                }
            }
        } catch (e: Exception) {
            Log.e("SocialRepo", "Error updating follow state: ${e.message}", e)
            return ResultWrapper.Error(e)
        }
    }

}


