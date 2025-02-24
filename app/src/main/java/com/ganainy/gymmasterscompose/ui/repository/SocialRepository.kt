package com.ganainy.gymmasterscompose.ui.repository

import com.ganainy.gymmasterscompose.ui.models.Follow
import com.ganainy.gymmasterscompose.ui.models.Follow.Companion.FOLLOWED_ID
import com.ganainy.gymmasterscompose.ui.models.Follow.Companion.FOLLOWER_ID
import com.ganainy.gymmasterscompose.ui.models.Follow.Companion.FOLLOWS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.User.Companion.FOLLOWERS_COUNT
import com.ganainy.gymmasterscompose.ui.models.User.Companion.FOLLOWING_COUNT
import com.ganainy.gymmasterscompose.ui.models.User.Companion.USERS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.User.Companion.USER_STATS
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
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
    private val userRepository: IUserRepository,
    private val database: FirebaseDatabase,
) : ISocialRepository {

    private val currentUserUid = userRepository.getCurrentUserId()


    override fun getUserFollowers(userId: String?): Flow<ResultWrapper<List<String>>> = flow {
        emit(ResultWrapper.Loading())

        try {
            if (userId == null) {
                emit(ResultWrapper.Success(emptyList()))
                return@flow
            }

            // Query follows collection where followedId matches the userId
            val followersSnapshot = database.getReference(FOLLOWS_COLLECTION)
                .orderByChild(FOLLOWED_ID)
                .equalTo(userId)
                .get()
                .await()

            // Extract followerIds from the snapshot
            val followerIds = followersSnapshot.children.mapNotNull { snapshot ->
                snapshot.getValue(Follow::class.java)?.followerId
            }

            emit(ResultWrapper.Success(followerIds))

        } catch (e: Exception) {
            emit(ResultWrapper.Error(e))
        }
    }


    override suspend fun getFollowedUsers(userId: String): ResultWrapper<List<String>> {
        return try {
            // Validate userId
            if (userId.isBlank()) {
                return ResultWrapper.Error(Exception("User ID cannot be blank"))
            }

            // Reference to the follows collection, filtered by followerId (current user)
            val followsRef = database.getReference(FOLLOWS_COLLECTION)
                .orderByChild(FOLLOWER_ID)
                .equalTo(userId)

            // Perform one-time read
            val snapshot = followsRef.get().await()

            // Parse followed user IDs from the snapshot
            val followedIds = snapshot.children.mapNotNull { it.getValue(Follow::class.java)?.followedId }

            ResultWrapper.Success(followedIds)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }


    /**
     * Checks if the current user is following the specified user.
     *
     * @param userIdToCheck The ID of the user to check if they are being followed by the current user.
     * @return A Flow emitting a ResultWrapper containing a Boolean indicating whether the current user is following the specified user or an error.
     */
    override fun isFollowing(userIdToCheck: String): Flow<ResultWrapper<Boolean>> = flow {
        emit(ResultWrapper.Loading())

        if (userIdToCheck == currentUserUid) {
            emit(ResultWrapper.Success(false))
            return@flow
        }

        val isFollowing = database.getReference(
            "${FOLLOWS_COLLECTION}/${
                Follow.createId(
                    followerId = currentUserUid,
                    followedId = userIdToCheck
                )
            }"
        )
            .get()
            .await()
            .exists()

        emit(ResultWrapper.Success(isFollowing))
    }.catch { e ->
        // Preserve the original exception
        emit(ResultWrapper.Error(e as? Exception ?: Exception(e.message)))
    }

    /**
     * Updates the follow state of a user.
     *
     * This function checks if the current user is already following the specified user.
     * If the user is currently being followed, it removes the follow relationship and decrements the follower and following counts.
     * If the user is not currently being followed, it creates a new follow relationship and increments the follower and following counts.
     *
     * @param userId The ID of the user to follow or unfollow.
     * @return A ResultWrapper containing Unit on success or an error on failure.
     */
    override suspend fun updateFollowState(userId: String): ResultWrapper<Unit> {
        return try {
            val followId = Follow.createId(currentUserUid, userId)

            // Wait for a non-loading state from isFollowing() flow
            when (val followingResult = isFollowing(userIdToCheck = userId)
                .filterNot { it is ResultWrapper.Loading }
                .first())
            {
                is ResultWrapper.Error -> followingResult
                is ResultWrapper.Success -> {
                    val updates = if (followingResult.data) {
                        // If already following, remove follow and decrement counts
                        hashMapOf(
                            "${FOLLOWS_COLLECTION}/${followId}" to null,
                            "$USERS_COLLECTION/$userId/$USER_STATS/$FOLLOWERS_COUNT" to ServerValue.increment(-1),
                            "$USERS_COLLECTION/$currentUserUid/$USER_STATS/$FOLLOWING_COUNT" to ServerValue.increment(-1)
                        )
                    } else {
                        // If not following, add follow and increment counts
                        hashMapOf(
                            "${FOLLOWS_COLLECTION}/${followId}" to Follow(
                                id = followId,
                                followerId = currentUserUid,
                                followedId = userId,
                                timestamp = System.currentTimeMillis()
                            ),
                            "$USERS_COLLECTION/$userId/$USER_STATS/$FOLLOWERS_COUNT" to ServerValue.increment(1),
                            "$USERS_COLLECTION/$currentUserUid/$USER_STATS/$FOLLOWING_COUNT" to ServerValue.increment(1)
                        )
                    }

                    // Perform the database update
                    database.reference.updateChildren(updates).await()

                    ResultWrapper.Success(Unit)
                }
                is ResultWrapper.Loading -> ResultWrapper.Error(Exception("Unexpected loading state"))
            }
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }




}


