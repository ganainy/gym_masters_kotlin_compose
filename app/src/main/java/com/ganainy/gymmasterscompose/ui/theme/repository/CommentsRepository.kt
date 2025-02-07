package com.ganainy.gymmasterscompose.ui.theme.repository

import Comment
import UserDisplayInfo
import com.ganainy.gymmasterscompose.ui.theme.models.comment.CommentLike
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostMetrics
import com.ganainy.gymmasterscompose.ui.theme.room.AppDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface ICommentsRepository {
    suspend fun isCommentLikedByUser(userId: String, commentId: String, postId: String): Boolean
    suspend fun addComment(postId: String, content: String): Result<Comment>
    suspend fun deleteComment(comment: Comment): Result<Unit>
    suspend fun toggleCommentLike(comment: Comment): Result<Boolean>
    fun observePostComments(postId: String): Flow<List<Comment>>
    fun observeIsCommentLikedByCurrentUser(commentId: String, postId: String):  Flow<Boolean>
}

class CommentsRepository @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val userRepository: IUserRepository,
    private val appDatabase: AppDatabase,
) : ICommentsRepository {
    private val commentsRef = firebaseDatabase.reference.child(Comment.COMMENTS_COLLECTION)
    private val commentLikesRef =
        firebaseDatabase.reference.child(CommentLike.COMMENT_LIKES_COLLECTION)
    private val postsRef = firebaseDatabase.reference.child(FeedPost.POSTS_COLLECTION)
    private val rootRef = firebaseDatabase.reference


    //todo add periodic work manager to sync local db with firebase db
    /**
     * Checks locally if a comment is liked by the current user.
     *
     * @param commentId The ID of the comment.
     * @param postId The ID of the post containing the comment.
     * @return True if the comment is liked by the current user, false otherwise.
     */
    override  fun observeIsCommentLikedByCurrentUser(commentId: String, postId: String): Flow<Boolean> {
        val currentUserId = userRepository.getCurrentUserId()
        return try {
            appDatabase.commentLikeDao().isCommentLiked(currentUserId, commentId, postId).map { it != null }
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Checks remotly if a comment is liked by a specific user.
     *
     * @param userId The ID of the user.
     * @param commentId The ID of the comment.
     * @param postId The ID of the post containing the comment.
     * @return True if the comment is liked by the user, false otherwise.
     */
    override suspend fun isCommentLikedByUser(
        userId: String,
        commentId: String,
        postId: String
    ): Boolean {
        val likeId = CommentLike.createId(userId, commentId, postId)
        return try {
            val snapshot = commentLikesRef.child(likeId).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun addComment(postId: String, content: String): Result<Comment> {
        return try {
            val userId = userRepository.getCurrentUserId()
            val commentRef = commentsRef.push()
            val commentId =
                commentRef.key ?: throw IllegalStateException("Failed to generate comment ID")

            // Get user display info
            when (val userResult = userRepository.getUser(null)) {
                is ResultWrapper.Error -> throw userResult.exception
                else -> {
                    val user = (userResult as ResultWrapper.Success).data


                    val userDisplayInfo = UserDisplayInfo(
                        displayName = user.displayName,
                        profileImageUrl = user.profilePictureUrl
                    )

                    val comment = Comment(
                        id = commentId,
                        postId = postId,
                        userId = userId,
                        content = content,
                        userDisplayInfo = userDisplayInfo,
                        timestamp = System.currentTimeMillis(),
                        isPending = false,
                    )

                    // Create updates map for atomic operation
                    val updates = HashMap<String, Any>()

                    // Add comment
                    updates["/${Comment.COMMENTS_COLLECTION}/$commentId"] = comment

                    // Increment post comment count
                    val postMetricsRef =
                        "/${FeedPost.POSTS_COLLECTION}/$postId/${FeedPost.POST_METRICS}"
                    // Get current comment count first
                    val currentCount = postsRef.child(postId)
                        .child(FeedPost.POST_METRICS)
                        .child(PostMetrics.POST_METRICS_COMMENTS)
                        .get()
                        .await()
                        .getValue(Long::class.java) ?: 0

                    updates["$postMetricsRef/${PostMetrics.POST_METRICS_COMMENTS}"] =
                        currentCount + 1

                    // Perform atomic update
                    rootRef.updateChildren(updates).await()

                    Result.success(comment)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteComment(comment: Comment): Result<Unit> {
        return try {
            // Get all likes for this comment
            val likesSnapshot = commentLikesRef
                .orderByChild("commentId")
                .equalTo(comment.id)
                .get()
                .await()

            // Create updates map for atomic operation
            val updates = HashMap<String, Any?>()

            // Remove comment
            updates["/${Comment.COMMENTS_COLLECTION}/${comment.id}"] = null

            // Remove all associated likes
            likesSnapshot.children.forEach { likeSnapshot ->
                updates["/${CommentLike.COMMENT_LIKES_COLLECTION}/${likeSnapshot.key}"] = null
            }

            // Get current comment count
            val currentCount = postsRef.child(comment.postId)
                .child(FeedPost.POST_METRICS)
                .child(PostMetrics.POST_METRICS_COMMENTS)
                .get()
                .await()
                .getValue(Long::class.java) ?: 1

            // Update post comment count
            updates["/${FeedPost.POSTS_COLLECTION}/${comment.postId}/${FeedPost.POST_METRICS}/${PostMetrics.POST_METRICS_COMMENTS}"] =
                currentCount - 1

            // Perform atomic update
            rootRef.updateChildren(updates).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleCommentLike(comment: Comment): Result<Boolean> {
        return try {
            val userId = userRepository.getCurrentUserId()
            val likeId = CommentLike.createId(userId, comment.id, comment.postId)

            //  Check local cache first
            val isLiked = appDatabase.commentLikeDao()
                .isCommentLiked(userId, comment.id, comment.postId).first() != null


            val updates = HashMap<String, Any?>()

            if (isLiked) {
                // Remove like
                updates["/${CommentLike.COMMENT_LIKES_COLLECTION}/$likeId"] = null
                updates["/${Comment.COMMENTS_COLLECTION}/${comment.id}/${Comment.COMMENTS_LIKES_COUNT}"] =
                    ServerValue.increment(-1)
                //  Remove from local cache
                appDatabase.commentLikeDao().removeLike(likeId)
            } else {
                // Add like
                val commentLike = CommentLike(
                    id = likeId,
                    userId = userId,
                    commentId = comment.id,
                    postId = comment.postId,
                    timestamp = System.currentTimeMillis()
                )
                updates["/${CommentLike.COMMENT_LIKES_COLLECTION}/$likeId"] = commentLike
                updates["/${Comment.COMMENTS_COLLECTION}/${comment.id}/${Comment.COMMENTS_LIKES_COUNT}"] =
                    ServerValue.increment(1)
                // Save to local cache
                appDatabase.commentLikeDao().insertLike(commentLike.toEntity())
            }

            // Perform atomic update
            rootRef.updateChildren(updates).await()

            Result.success(!isLiked)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observePostComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = snapshot.children.mapNotNull {
                    it.getValue(Comment::class.java)
                }.sortedByDescending { it.timestamp }

                trySend(comments).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        commentsRef.orderByChild("postId").equalTo(postId).addValueEventListener(listener)

        awaitClose { commentsRef.removeEventListener(listener) }
    }.distinctUntilChanged() // Avoid unnecessary emissions


}