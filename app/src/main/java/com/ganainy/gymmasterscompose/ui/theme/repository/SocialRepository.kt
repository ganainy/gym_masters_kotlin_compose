package com.ganainy.gymmasterscompose.ui.theme.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

// Social interactions (following/followers)
interface ISocialRepository {
    suspend fun followUser(userId: String): ResultWrapper<Unit>
    suspend fun unfollowUser(userId: String): ResultWrapper<Unit>
    fun getUserFollowers(userId: String?): Flow<ResultWrapper<List<String>>>
    fun getUserFollowing(userId: String?): Flow<ResultWrapper<List<String>>>
    abstract fun isFollowing(userId: String): Flow<ResultWrapper<Boolean>> // Check if the current user is following the target user, returns Boolean
}

class SocialRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
) : ISocialRepository {

    private val currentUserUid = auth.currentUser?.uid

    companion object {
        private const val FOLLOWERS = "followers"
        private const val FOLLOWING = "following"
    }

    override suspend fun followUser(userId: String): ResultWrapper<Unit> {
        return updateFollowState(userId, isFollowing = false)
    }

    override suspend fun unfollowUser(userId: String): ResultWrapper<Unit> {
        return updateFollowState(userId, isFollowing = true)
    }

    override fun getUserFollowers(userId: String?): Flow<ResultWrapper<List<String>>> = flow {

        if (userId == null) {
            emit(ResultWrapper.Error(Exception("logged in user id is null")))
        }

        try {
            val followers = withContext(Dispatchers.IO) {
                database.getReference("$FOLLOWERS/$userId")
                    .get()
                    .await()
                    .children
                    .map { it.key!! }
            }
            emit(ResultWrapper.Success(followers))
        } catch (e: Exception) {
            emit(ResultWrapper.Error(e))
        }
    }

    override fun getUserFollowing(userId: String?): Flow<ResultWrapper<List<String>>> = flow {

        if (userId == null) {
            emit(ResultWrapper.Error(Exception("logged in user id is null")))
        }

        try {
            val following = withContext(Dispatchers.IO) {
                database.getReference("$FOLLOWING/$userId")
                    .get()
                    .await()
                    .children
                    .map { it.key!! }
            }
            emit(ResultWrapper.Success(following))
        } catch (e: Exception) {
            emit(ResultWrapper.Error(e))
        }
    }

    override fun isFollowing(userId: String): Flow<ResultWrapper<Boolean>> = flow {
        try {
            val isFollowing = withContext(Dispatchers.IO) {
                database.getReference("$FOLLOWING/$currentUserUid/$userId")
                    .get()
                    .await()
                    .exists()
            }
            emit(ResultWrapper.Success(isFollowing))
        } catch (e: Exception) {
            emit(ResultWrapper.Error(e))
        }
    }

    private suspend fun updateFollowState(userId: String, isFollowing: Boolean): ResultWrapper<Unit> {
        val followersRef = database.getReference("$FOLLOWERS/$userId")
        val followingRef = database.getReference("$FOLLOWING/$currentUserUid")

        val currentUserStatsRef = database.getReference("users/$currentUserUid/stats")
        val targetUserStatsRef = database.getReference("users/$userId/stats")

        return try {
            val updates = mutableMapOf<String, Any?>()

            if (isFollowing) {
                // Unfollow logic
                updates["$FOLLOWING/$currentUserUid/$userId"] = null
                updates["$FOLLOWERS/$userId/$currentUserUid"] = null
                updateCounters(
                    userId,
                    currentUserStatsRef,
                    targetUserStatsRef,
                    updates,
                    decrease = true
                )
            } else {
                // Follow logic
                updates["$FOLLOWING/$currentUserUid/$userId"] = true
                updates["$FOLLOWERS/$userId/$currentUserUid"] = true
                updateCounters(
                    userId,
                    currentUserStatsRef,
                    targetUserStatsRef,
                    updates,
                    decrease = false
                )
            }

            withContext(Dispatchers.IO) {
                database.reference.updateChildren(updates).await()
            }

            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
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
        val currentUserStats = withContext(Dispatchers.IO) {
            currentUserStatsRef.get().await()
        }
        val targetUserStats = withContext(Dispatchers.IO) {
            targetUserStatsRef.get().await()
        }

        val currentFollowingCount =
            (currentUserStats.child("followingCount").getValue(Long::class.java) ?: 0)
        val targetFollowersCount =
            (targetUserStats.child("followersCount").getValue(Long::class.java) ?: 0)

        if (decrease) {
            updates["users/$currentUserUid/stats/followingCount"] =
                maxOf(currentFollowingCount - 1, 0)
            updates["users/$otherUserId/stats/followersCount"] = maxOf(targetFollowersCount - 1, 0)
        } else {
            updates["users/$currentUserUid/stats/followingCount"] = currentFollowingCount + 1
            updates["users/$otherUserId/stats/followersCount"] = targetFollowersCount + 1
        }
    }
}


