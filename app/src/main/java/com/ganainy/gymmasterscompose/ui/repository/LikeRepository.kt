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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.google.firebase.firestore.FieldPath

interface ILikeRepository {
    suspend fun toggleLike(
        targetId: String,
        type: LikeType,
        postId: String? = null,
        userId: String? = null
    ): ResultWrapper<Unit>

    suspend fun syncPendingLikes() // Implementation uses toggleLike
    fun observeLikeStatus( // Observes Room cache
        targetId: String,
        type: LikeType,
        postId: String? = null,
        userId: String? = null
    ): Flow<Boolean>

    suspend fun observeTypeLikes(type: LikeType, userId: String?): Flow<List<CachedLike?>> // Observes Room cache
    suspend fun getLikeStatus(
        targetId: String,
        type: LikeType,
        postId: String? = null
    ): ResultWrapper<Boolean>
}



/**
 * Repository for managing likes with local caching
 */
class LikeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val appDatabase: AppDatabase,
    private val userRepository: IUserRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ILikeRepository {
    private val cachedLikeDao = appDatabase.cachedLikeDao()

    override suspend fun toggleLike(
        targetId: String,
        type: LikeType,
        postId: String?,
        userId: String?
    ): ResultWrapper<Unit> = withContext(dispatcher) {
        val effectiveUserId = userId ?: try {
            userRepository.getCurrentUserId()
        } catch (e: Exception) {
            Log.e("LikeRepository", "Cannot toggle like: User not authenticated")
            return@withContext ResultWrapper.Error(AuthRepository.UserNotAuthenticatedException("User not authenticated"))
        }

        // Generate the unique Like Document ID
        val likeDocId = when (type) {
            LikeType.POST -> PostLike.createId(effectiveUserId, targetId)
            LikeType.WORKOUT -> WorkoutLike.createId(effectiveUserId, targetId)
            LikeType.COMMENT -> {
                if (postId == null) return@withContext ResultWrapper.Error(IllegalArgumentException("postId needed for Comment"))
                CommentLike.createId(effectiveUserId, targetId, postId)
            }
        }

        // --- 1. Determine Intended Action & Optimistic Cache Update ---
        // Read cache *once* to decide the very next action user intends
        val cachedStateBeforeOptimistic = cachedLikeDao.getLike(likeDocId)
        val isCurrentlyLikedInCache = cachedStateBeforeOptimistic?.isLiked ?: false
        val intendedActionIsLike = !isCurrentlyLikedInCache // The user wants to achieve this state

        val optimisticCachedLike = CachedLike(
            id = likeDocId,
            userId = effectiveUserId,
            targetId = targetId,
            postId = postId,
            likeType = type,
            timestamp = Timestamp.now(),
            isPending = true, // Mark as pending
            isLiked = intendedActionIsLike // Set the intended new state
        )

        try {
            // Update cache FIRST for immediate UI feedback
            cachedLikeDao.insertLike(optimisticCachedLike)
            Log.d("LikeRepository", "Optimistic cache update for $likeDocId: isLiked=$intendedActionIsLike")
        } catch(cacheError: Exception) {
            Log.e("LikeRepository", "Error updating Room cache optimistically for $likeDocId", cacheError)
            // If cache write fails, we can't proceed reliably
            return@withContext ResultWrapper.Error(Exception("Failed to update local cache", cacheError))
        }


        // --- 2. Perform Firestore Transaction ---
        try {
            firestore.runTransaction { transaction ->
                // Get paths helper (assuming it doesn't throw and returns valid paths/refs)
                val paths = getFirestorePaths(type, targetId, likeDocId, postId)
                val likeDocRef = paths.likeDocRef
                val metricsDocRef = paths.metricsDocRef
                val metricsFieldPath = paths.metricsFieldPath

                // Crucial null check for paths
                if (metricsDocRef == null || metricsFieldPath == null) {
                    // Throw exception to abort transaction
                    throw IllegalStateException("Invalid Firestore paths for like type $type")
                }

                // Read the current like state *within the transaction*
                val currentLikeSnapshot = transaction.get(likeDocRef)
                val likeExistsBackend = currentLikeSnapshot.exists()

                // Decide action based on backend state
                if (intendedActionIsLike) { // User wants to LIKE
                    if (!likeExistsBackend) { // Only proceed if not already liked on backend
                        val likeObject = createLikeObject(type, likeDocId, effectiveUserId, targetId, postId)
                        transaction.set(likeDocRef, likeObject)
                        transaction.update(metricsDocRef, metricsFieldPath, FieldValue.increment(1))
                        Log.d("LikeRepository", "[TX] Liking $likeDocId (Backend state: Not Liked)")
                    } else {
                        Log.w("LikeRepository", "[TX] Like intended for $likeDocId, but already liked on backend. No op.")
                        // Optional: Could still update metrics if cache was wrong, but usually means sync issue
                    }
                } else { // User wants to UNLIKE
                    if (likeExistsBackend) { // Only proceed if currently liked on backend
                        transaction.delete(likeDocRef)
                        transaction.update(metricsDocRef, metricsFieldPath, FieldValue.increment(-1))
                        Log.d("LikeRepository", "[TX] Unliking $likeDocId (Backend state: Liked)")
                    } else {
                        Log.w("LikeRepository", "[TX] Unlike intended for $likeDocId, but not liked on backend. No op.")
                        // Optional: Could still update metrics if cache was wrong
                    }
                }
                // Transaction automatically commits if no exception is thrown
            }.await() // Wait for transaction to complete

            // --- 3. Transaction Succeeded: Update Cache (Remove Pending) ---
            Log.d("LikeRepository", "Firestore transaction successful for $likeDocId")
            try {
                cachedLikeDao.insertLike(optimisticCachedLike.copy(isPending = false))
                Log.d("LikeRepository", "Cache pending state removed for $likeDocId")
            } catch (cacheError: Exception) {
                // This is less critical, log it. The main operation succeeded. Sync might fix later.
                Log.e("LikeRepository", "Error removing pending state from cache for $likeDocId after successful TX", cacheError)
            }

            ResultWrapper.Success(Unit)

        } catch (e: Exception) {
            // --- 4. Transaction Failed: Keep Cache Pending ---
            // No need to revert cache here. The optimistic state (pending=true) remains,
            // reflecting the user's last intent. syncPendingLikes will retry later.
            Log.e("LikeRepository", "Firestore transaction failed for $likeDocId. Cache remains pending.", e)
            ResultWrapper.Error(e) // Report the Firestore error
        }
    }

    data class FirestoreLikePaths(
        val likeCollectionPath: String,
        val likeDocRef: DocumentReference,
        val metricsDocRef: DocumentReference?,
        val metricsFieldPath: FieldPath?
    )

    // Helper to create the like object based on type
    private fun createLikeObject(type: LikeType, likeId: String, userId: String, targetId: String, postId: String?): Any {
        val timestamp = FieldValue.serverTimestamp() // Use server timestamp
        return when (type) {
            LikeType.POST -> PostLike(id = likeId, userId = userId, postId = targetId, timestamp = Timestamp.now())
            LikeType.WORKOUT -> WorkoutLike(id = likeId, userId = userId, workoutId = targetId, timestamp = Timestamp.now())
            LikeType.COMMENT -> CommentLike(id = likeId, userId = userId, commentId = targetId, postId = postId!!, timestamp = Timestamp.now())
        }
    }

    // Helper to get Firestore paths based on type
    private fun getFirestorePaths(
        type: LikeType,
        targetId: String,
        likeDocId: String,
        postId: String? // postId still needed for comment like ID generation, but not directly returned here
    ): FirestoreLikePaths {
        return when (type) {
            LikeType.POST -> FirestoreLikePaths(
                likeCollectionPath = POST_LIKES_COLLECTION,
                likeDocRef = firestore.collection(POST_LIKES_COLLECTION).document(likeDocId),
                metricsDocRef = firestore.collection(POSTS_COLLECTION).document(targetId),
                metricsFieldPath = FieldPath.of(POST_METRICS, POST_LIKES)
            )
            LikeType.WORKOUT -> FirestoreLikePaths(
                likeCollectionPath = WORKOUT_LIKES_COLLECTION,
                likeDocRef = firestore.collection(WORKOUT_LIKES_COLLECTION).document(likeDocId),
                metricsDocRef = firestore.collection(WORKOUTS_COLLECTION).document(targetId),
                metricsFieldPath = FieldPath.of(WORKOUTS_METRICS, WORKOUT_LIKES_COUNT)
            )
            LikeType.COMMENT -> FirestoreLikePaths(
                likeCollectionPath = COMMENT_LIKES_COLLECTION,
                likeDocRef = firestore.collection(COMMENT_LIKES_COLLECTION).document(likeDocId),
                metricsDocRef = firestore.collection(Comment.COMMENTS_COLLECTION).document(targetId),
                metricsFieldPath = FieldPath.of(Comment.COMMENTS_LIKES_COUNT)
                // Note: postId is not directly part of the paths returned, but was used to create likeDocId
            )
        }
    }

    override suspend fun getLikeStatus(
        targetId: String,
        type: LikeType,
        postId: String?
    ): ResultWrapper<Boolean> {
        return try {
            val userId = userRepository.getCurrentUserId()

            // Generate the specific Like Document ID
            val likeDocId = when (type) {
                LikeType.POST -> PostLike.createId(userId, targetId)
                LikeType.WORKOUT -> WorkoutLike.createId(userId, targetId)
                LikeType.COMMENT -> {
                    if (postId == null) return ResultWrapper.Error(IllegalArgumentException("postId needed for Comment"))
                    CommentLike.createId(userId, targetId, postId)
                }
            }

            // Determine the collection based on the like type
            val collectionPath = when (type) {
                LikeType.POST -> POST_LIKES_COLLECTION
                LikeType.COMMENT -> COMMENT_LIKES_COLLECTION
                LikeType.WORKOUT -> WORKOUT_LIKES_COLLECTION
            }

            // Check if the like document exists
            val likeDocRef = firestore.collection(collectionPath).document(likeDocId)
            val snapshot = likeDocRef.get().await()

            ResultWrapper.Success(snapshot.exists())
        } catch (e: AuthRepository.UserNotAuthenticatedException) {
            Log.w("LikeRepository", "getLikeStatus: User not authenticated.")
            // If user isn't logged in, they haven't liked anything
            ResultWrapper.Success(false)
        }
        catch (e: Exception) {
            Log.e("LikeRepository", "Error getting like status for $type/$targetId", e)
            ResultWrapper.Error(e)
        }
    }

    /**
     *  Syncs pending likes on app startup or network reconnection
     */
    override suspend fun syncPendingLikes() {
        val pendingLikes = cachedLikeDao.getPendingLikes()
        pendingLikes.forEach { pendingLike ->
            // Call toggleLike - it will handle the correct Firestore operation based on the *intended* state
            // It internally checks the *current* cache state before deciding like/unlike Firestore action.
            // Important: If the sync fails, the item remains pending in cache for next try.
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