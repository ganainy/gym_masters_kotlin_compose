package com.ganainy.gymmasterscompose.ui.repository

import Comment
import com.ganainy.gymmasterscompose.ui.models.comment.CommentLike
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.models.post.PostMetrics
import com.ganainy.gymmasterscompose.ui.room.AppDatabase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface ICommentsRepository {
    suspend fun deleteComment(comment: Comment): ResultWrapper<Unit>
    suspend fun getPostComments(postId: String): ResultWrapper<List<Comment>>
    suspend fun addComment(comment: Comment): ResultWrapper<Unit>
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

    override suspend fun getPostComments(postId: String): ResultWrapper<List<Comment>> {
        return try {
            // Reference to the comments collection for the given post
            val commentsSnapshot = commentsRef.orderByChild("postId").equalTo(postId).get().await()

            // Check if there are any comments
            if (!commentsSnapshot.exists()) {
                // No comments found, return empty list
                ResultWrapper.Success(emptyList())
            } else {
                // Parse comments from the snapshot
                val comments = commentsSnapshot.children.mapNotNull { snapshot ->
                    snapshot.getValue(Comment::class.java)?.takeIf { it.postId == postId }
                }.sortedByDescending { it.timestamp } // Sort by timestamp (newest first)

                ResultWrapper.Success(comments)
            }
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    override suspend fun addComment(comment: Comment): ResultWrapper<Unit> {
        return try {
            // Create updates map for atomic operation
            val updates = HashMap<String, Any>()

            // Add comment to backend
            updates["/${Comment.COMMENTS_COLLECTION}/${comment.id}"] = comment.copy(isPending = false)

            // Increment post comment count
            val postMetricsRef = "/${FeedPost.POSTS_COLLECTION}/${comment.postId}/${FeedPost.POST_METRICS}"
            // Get current comment count first
            val currentCount = postsRef.child(comment.postId)
                .child(FeedPost.POST_METRICS)
                .child(PostMetrics.POST_METRICS_COMMENTS)
                .get()
                .await()
                .getValue(Long::class.java) ?: 0

            updates["$postMetricsRef/${PostMetrics.POST_METRICS_COMMENTS}"] = currentCount + 1

            // Perform atomic update
            rootRef.updateChildren(updates).await()

            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    override suspend fun deleteComment(comment: Comment): ResultWrapper<Unit> {
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

            // Update post comment count (ensure it doesn't go negative)
            val newCount = (currentCount - 1).coerceAtLeast(0)
            updates["/${FeedPost.POSTS_COLLECTION}/${comment.postId}/${FeedPost.POST_METRICS}/${PostMetrics.POST_METRICS_COMMENTS}"] =
                newCount

            // Perform atomic update
            rootRef.updateChildren(updates).await()

            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }




}