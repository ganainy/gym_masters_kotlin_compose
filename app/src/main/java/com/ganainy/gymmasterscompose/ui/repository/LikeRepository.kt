package com.ganainy.gymmasterscompose.ui.repository

import Comment
import NavigationArgs.USER_ID
import android.util.Log
import com.ganainy.gymmasterscompose.ui.models.comment.CommentLike
import com.ganainy.gymmasterscompose.ui.models.comment.CommentLike.Companion.COMMENT_LIKES_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost.Companion.POSTS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost.Companion.POST_LIKES
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost.Companion.POST_METRICS
import com.ganainy.gymmasterscompose.ui.models.post.PostLike
import com.ganainy.gymmasterscompose.ui.models.post.PostLike.Companion.POST_LIKES_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutEntity.Companion.WORKOUTS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutEntity.Companion.WORKOUTS_METRICS
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutLike
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutLike.Companion.WORKOUT_LIKES_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutMetrics.Companion.WORKOUT_LIKES_COUNT
import com.ganainy.gymmasterscompose.ui.room.AppDatabase
import com.ganainy.gymmasterscompose.ui.room.CachedLike
import com.ganainy.gymmasterscompose.ui.room.LikeType
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ILikeRepository {
    suspend fun toggleLike(
        targetId: String,
        type: LikeType,
        postId: String? = null,
        userId: String? = null
    ): ResultWrapper<Unit>

    suspend fun syncPendingLikes()
    fun observeLikeStatus(
        targetId: String,
        type: LikeType,
        postId: String? = null,
        userId: String? = null
    ): Flow<Boolean>

    suspend fun observeTypeLikes(type: LikeType, userId: String?): Flow<List<CachedLike?>>
    suspend fun getLikeStatus(
        targetId: String,
        type: LikeType,
        postId: String?
    ): ResultWrapper<Boolean>
}


/**
 * Repository for managing likes with local caching
 */
class LikeRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val appDatabase: AppDatabase,
    private val userRepository: IUserRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ILikeRepository {
    private val cachedLikeDao = appDatabase.cachedLikeDao()

    /**
     * Toggles like status for any supported content type with local caching
     *
     * @param targetId ID of the content being liked (post, workout, or comment ID)
     * @param type Type of content being liked
     * @param postId Optional post ID for comment likes
     * @param userId Optional user ID (defaults to current user)
     * @return ResultWrapper indicating success or failure
     */
    override suspend fun toggleLike(
        targetId: String,
        type: LikeType,
        postId: String?,
        userId: String?
    ): ResultWrapper<Unit> = withContext(dispatcher) {
        try {
            val effectiveUserId = userId ?: userRepository.getCurrentUserId()
            val likeId = when (type) {
                LikeType.POST -> PostLike.createId(effectiveUserId, targetId)
                LikeType.WORKOUT -> WorkoutLike.createId(effectiveUserId, targetId)
                LikeType.COMMENT -> CommentLike.createId(effectiveUserId, targetId, postId!!)
            }

            // Get current like status
            val isCurrentlyLiked = cachedLikeDao.isLiked(effectiveUserId, targetId, type, postId)

            // Update cache immediately
            val newCachedLike = CachedLike(
                id = likeId,
                userId = effectiveUserId,
                targetId = targetId,
                postId = postId,
                likeType = type,
                timestamp = System.currentTimeMillis(),
                isPending = true,
                isLiked = !isCurrentlyLiked
            )
            cachedLikeDao.insertLike(newCachedLike)

            // Prepare Firebase updates
            val updates = buildFirebaseUpdates(
                type = type,
                likeId = likeId,
                targetId = targetId,
                userId = effectiveUserId,
                isLiked = !isCurrentlyLiked,
                postId = postId
            )

            // Perform Firebase update
            database.reference.updateChildren(updates).await()

            // Update cache to remove pending state
            cachedLikeDao.insertLike(newCachedLike.copy(isPending = false))

            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            // Revert cache on failure
            try {
                val effectiveUserId = userId ?: userRepository.getCurrentUserId()
                val likeId = when (type) {
                    LikeType.POST -> PostLike.createId(effectiveUserId, targetId)
                    LikeType.WORKOUT -> WorkoutLike.createId(effectiveUserId, targetId)
                    LikeType.COMMENT -> CommentLike.createId(effectiveUserId, targetId, postId!!)
                }
                cachedLikeDao.deleteLike(likeId)
            } catch (revertError: Exception) {
                Log.e("LikeRepository", "Failed to revert cache", revertError)
            }
            ResultWrapper.Error(e)
        }
    }


    private val postLikesRef = database.reference.child(POST_LIKES_COLLECTION)
    private val commentLikesRef = database.reference.child(COMMENT_LIKES_COLLECTION)
    private val workoutLikesRef = database.reference.child(WORKOUT_LIKES_COLLECTION)
    override suspend fun getLikeStatus(
        targetId: String,
        type: LikeType,
        postId: String?
    ): ResultWrapper<Boolean> {
        return try {
            val userId = userRepository.getCurrentUserId()

            // Determine the collection based on the like type
            val likesRef = when (type) {
                LikeType.POST -> postLikesRef
                LikeType.COMMENT -> commentLikesRef
                LikeType.WORKOUT -> workoutLikesRef
            }

            // Query the like entry for the current user and target
            val likeSnapshot = likesRef
                .orderByChild(USER_ID)
                .equalTo(userId)
                .get()
                .await()

            // Check if the user has liked the target
            val isLiked = likeSnapshot.children.any { snapshot ->
                val like = snapshot.getValue(CachedLike::class.java)
                like?.targetId == targetId && like.likeType == type
            }

            ResultWrapper.Success(isLiked)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    /**
     * Builds Firebase update map based on like type and action
     */
    private fun buildFirebaseUpdates(
        type: LikeType,
        likeId: String,
        targetId: String,
        userId: String,
        isLiked: Boolean,
        postId: String?
    ): Map<String, Any?> {
        val updates = mutableMapOf<String, Any?>()

        val (collectionPath, metricsPath) = when (type) {
            LikeType.POST -> Pair(
                POST_LIKES_COLLECTION,
                "${POSTS_COLLECTION}/$targetId/${POST_METRICS}/${POST_LIKES}"
            )
            LikeType.WORKOUT -> Pair(
                WORKOUT_LIKES_COLLECTION,
                "${WORKOUTS_COLLECTION}/$targetId/$WORKOUTS_METRICS/$WORKOUT_LIKES_COUNT"
            )
            LikeType.COMMENT -> Pair(
                CommentLike.COMMENT_LIKES_COLLECTION,
                "${Comment.COMMENTS_COLLECTION}/$targetId/${Comment.COMMENTS_LIKES_COUNT}"
            )
        }

        if (isLiked) {
            val likeObject = when (type) {
                LikeType.POST -> PostLike(
                    id = likeId,
                    userId = userId,
                    postId = targetId,
                    timestamp = System.currentTimeMillis()
                )
                LikeType.WORKOUT -> WorkoutLike(
                    id = likeId,
                    userId = userId,
                    workoutId = targetId,
                    timestamp = System.currentTimeMillis()
                )
                LikeType.COMMENT -> CommentLike(
                    id = likeId,
                    userId = userId,
                    commentId = targetId,
                    postId = postId!!,
                    timestamp = System.currentTimeMillis()
                )
            }
            updates["$collectionPath/$likeId"] = likeObject
            updates[metricsPath] = ServerValue.increment(1)
        } else {
            updates["$collectionPath/$likeId"] = null
            updates[metricsPath] = ServerValue.increment(-1)
        }

        return updates
    }

    /**
     *  Syncs pending likes on app startup or network reconnection
     */
    override suspend fun syncPendingLikes() {
        val pendingLikes = cachedLikeDao.getPendingLikes()
        pendingLikes.forEach { pendingLike ->
            toggleLike(
                targetId = pendingLike.targetId,
                type = pendingLike.likeType,
                postId = pendingLike.postId,
                userId = pendingLike.userId
            )
        }
    }

    /**
     * Observes like status for a specific piece of content
     */
    override fun observeLikeStatus(
        targetId: String,
        type: LikeType,
        postId: String?,
        userId: String?
    ): Flow<Boolean> = flow {
        val effectiveUserId = userId ?: userRepository.getCurrentUserId()
        cachedLikeDao.observeLike(effectiveUserId, targetId, type, postId)
            .map { it?.isLiked ?: false }
            .collect { emit(it) }
    }


    override suspend fun observeTypeLikes(
        type: LikeType,
        userId: String?
    ): Flow<List<CachedLike?>> {
        val effectiveUserId = userId ?: userRepository.getCurrentUserId()
        return cachedLikeDao.observeTypeLikes(effectiveUserId, type)
    }
}