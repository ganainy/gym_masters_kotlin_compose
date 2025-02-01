package com.ganainy.gymmasterscompose.ui.theme.repository

import com.ganainy.gymmasterscompose.ui.theme.models.Follow
import com.ganainy.gymmasterscompose.ui.theme.models.Follow.Companion.FOLLOWER_ID
import com.ganainy.gymmasterscompose.ui.theme.models.Follow.Companion.FOLLOWS
import com.ganainy.gymmasterscompose.ui.theme.models.User.Companion.FOLLOWERS_COUNT
import com.ganainy.gymmasterscompose.ui.theme.models.User.Companion.FOLLOWING_COUNT
import com.ganainy.gymmasterscompose.ui.theme.models.User.Companion.USERS
import com.ganainy.gymmasterscompose.ui.theme.models.User.Companion.USER_STATS
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Social interactions (following/followers)
interface ISocialRepository {
    fun getUserFollowers(userId: String?): Flow<ResultWrapper<List<String>>>
    fun getUserFollowing(userId: String?): Flow<ResultWrapper<List<String>>>
    abstract fun isFollowing(userIdToCheck: String): Flow<ResultWrapper<Boolean>> // Check if the current user is following the target user, returns Boolean
    suspend fun updateFollowState(userId: String): ResultWrapper<Unit>
}

class SocialRepository @Inject constructor(
    private val userRepository: IUserRepository,
    private val database: FirebaseDatabase,
) : ISocialRepository {

    private val currentUserUid = userRepository.getCurrentUserId()


    override fun getUserFollowers(userId: String?): Flow<ResultWrapper<List<String>>> = flow {
        TODO()
    }


    /**
     * Retrieves the list of user IDs that the specified user is following.
     *
     * @param userId The ID of the user whose following list is to be retrieved. If null, an error is returned.
     * @return A Flow emitting a ResultWrapper containing a list of user IDs that the specified user is following or an error.
     */
    override fun getUserFollowing(userId: String?): Flow<ResultWrapper<List<String>>> =
        callbackFlow {
            if (userId == null) {
                trySend(ResultWrapper.Error(Exception("logged in user id is null")))
                close()
                return@callbackFlow
            }

            val followsRef = database.getReference(FOLLOWS)
                .orderByChild(FOLLOWER_ID)
                .equalTo(userId)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val followedIds =
                        snapshot.children.mapNotNull { it.getValue(Follow::class.java)?.followedId }
                    trySend(ResultWrapper.Success(followedIds))
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(ResultWrapper.Error(error.toException()))
                    close(error.toException()) // Close the flow on error
                }
            }

            followsRef.addListenerForSingleValueEvent(listener)

            awaitClose { followsRef.removeEventListener(listener) } // Cleanup when flow is closed
        }



    /**
     * Checks if the current user is following the specified user.
     *
     * @param userIdToCheck The ID of the user to check if they are being followed by the current user.
     * @return A Flow emitting a ResultWrapper containing a Boolean indicating whether the current user is following the specified user or an error.
     */
    override fun isFollowing(userIdToCheck: String): Flow<ResultWrapper<Boolean>> = flow {
        // Validate IDs
        if (currentUserUid == null || userIdToCheck == null) {
            emit(ResultWrapper.Error(Exception("logged in user id is null")))
            return@flow
        }

        if (userIdToCheck == currentUserUid) {
            emit(ResultWrapper.Success(false))
            return@flow
        }

        val isFollowing = database.getReference(
            "${FOLLOWS}/${
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
        emit(ResultWrapper.Error(Exception("error occurred while checking if following")))
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

            // Use the first value emitted by isFollowing() flow
            val followingResult = isFollowing(userIdToCheck = userId).first()

            when (followingResult) {
                is ResultWrapper.Error -> followingResult
                is ResultWrapper.Success -> {
                    val updates = if (followingResult.data) {
                        // If already following, remove follow and decrement counts
                        hashMapOf(
                            "${FOLLOWS}/${followId}" to null,
                            "$USERS/$userId/$USER_STATS/$FOLLOWERS_COUNT" to ServerValue.increment(-1),
                            "$USERS/$currentUserUid/$USER_STATS/$FOLLOWING_COUNT" to ServerValue.increment(-1)
                        )
                    } else {
                        // If not following, add follow and increment counts
                        hashMapOf(
                            "${FOLLOWS}/${followId}" to Follow(
                                id = followId,
                                followerId = currentUserUid,
                                followedId = userId,
                                timestamp = System.currentTimeMillis()
                            ),
                            "$USERS/$userId/$USER_STATS/$FOLLOWERS_COUNT" to ServerValue.increment(1),
                            "$USERS/$currentUserUid/$USER_STATS/$FOLLOWING_COUNT" to ServerValue.increment(1)
                        )
                    }

                    // Perform the database update
                    database.reference.updateChildren(updates).await()

                    ResultWrapper.Success(Unit)
                }
            }
        } catch (e: Exception) {
            // Catching exceptions and returning as ResultWrapper.Error
            ResultWrapper.Error(e)
        }
    }



    private suspend fun updateCounters(
        otherUserId: String,
        currentUserStatsRef: DatabaseReference,
        targetUserStatsRef: DatabaseReference,
        updates: MutableMap<String, Any?>,
        decrease: Boolean
    ) {
        TODO()
    }
}


