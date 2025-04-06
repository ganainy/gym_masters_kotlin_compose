package com.ganainy.gymmasterscompose.ui.repository

import Comment
import android.util.Log
import com.ganainy.gymmasterscompose.ui.models.comment.CommentLike
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.models.post.PostMetrics
import com.ganainy.gymmasterscompose.ui.room.AppDatabase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.firestore.FieldValue
interface ICommentsRepository {
    suspend fun deleteComment(comment: Comment): ResultWrapper<Unit>
    suspend fun getPostComments(postId: String): ResultWrapper<List<Comment>>
    suspend fun addComment(comment: Comment): ResultWrapper<Unit>
}

class CommentsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ICommentsRepository {

    // Firestore Collection References
    private val commentsCollection = firestore.collection(Comment.COMMENTS_COLLECTION)
    private val commentLikesCollection = firestore.collection(CommentLike.COMMENT_LIKES_COLLECTION)
    private val postsCollection = firestore.collection(FeedPost.POSTS_COLLECTION)

    override suspend fun getPostComments(postId: String): ResultWrapper<List<Comment>> {
        return try {
            // Query comments collection where postId matches
            val querySnapshot = commentsCollection
                .whereEqualTo(Comment.POST_ID_FIELD, postId)
                .orderBy(Comment.TIMESTAMP_FIELD, Query.Direction.DESCENDING) // Order by timestamp, newest first
                .get()
                .await()

            // Map documents to Comment objects
            val comments = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Comment::class.java)
                } catch (e: Exception) {
                    Log.e("CommentsRepository", "Error converting document ${document.id} to Comment", e)
                    null // Skip invalid documents
                }
            }
            ResultWrapper.Success(comments)

        } catch (e: Exception) {
            Log.e("CommentsRepository", "Error fetching comments for post $postId", e)
            ResultWrapper.Error(e)
        }
    }

    override suspend fun addComment(comment: Comment): ResultWrapper<Unit> {
        // Ensure comment has a valid ID and postId
        if (comment.id.isBlank() || comment.postId.isBlank()) {
            Log.e("CommentsRepository", "Cannot add comment with blank ID or postId")
            return ResultWrapper.Error(IllegalArgumentException("Comment ID and Post ID cannot be blank"))
        }

        val batch = firestore.batch()
        return try {
            // Document references
            val commentDocRef = commentsCollection.document(comment.id)
            val postDocRef = postsCollection.document(comment.postId)

            // Add operations to batch:
            // a) Set the new comment document (ensure isPending is handled or removed if not needed)
            // comment model has isPending=true, create a copy without it for Firestore
            val commentToSave = if (comment.isPending) comment.copy(isPending = false) else comment
            batch.set(commentDocRef, commentToSave)

            // b) Increment the post's comment count using dot notation for nested field
            val postMetricsCommentsPath = "${FeedPost.POST_METRICS}.${PostMetrics.POST_METRICS_COMMENTS}"
            batch.update(postDocRef, postMetricsCommentsPath, FieldValue.increment(1))

            // Commit the batch
            batch.commit().await()
            Log.d("CommentsRepository", "Comment ${comment.id} added successfully.")
            ResultWrapper.Success(Unit)

        } catch (e: Exception) {
            Log.e("CommentsRepository", "Error adding comment ${comment.id}", e)
            ResultWrapper.Error(e)
        }
    }

    override suspend fun deleteComment(comment: Comment): ResultWrapper<Unit> {
        // Ensure comment has a valid ID and postId
        if (comment.id.isBlank() || comment.postId.isBlank()) {
            Log.e("CommentsRepository", "Cannot delete comment with blank ID or postId")
            return ResultWrapper.Error(IllegalArgumentException("Comment ID and Post ID cannot be blank"))
        }

        val batch = firestore.batch()
        return try {
            // 1. Find associated comment likes
            val likesQuerySnapshot = commentLikesCollection
                .whereEqualTo(CommentLike.COMMENT_ID_FIELD, comment.id)
                .get()
                .await()

            val likeDocRefsToDelete = likesQuerySnapshot.documents.map { it.reference }

            // 2. Define main document references
            val commentDocRef = commentsCollection.document(comment.id)
            val postDocRef = postsCollection.document(comment.postId)

            // 3. Add operations to batch:
            // a) Delete the comment itself
            batch.delete(commentDocRef)

            // b) Delete all associated comment likes
            likeDocRefsToDelete.forEach { likeRef ->
                batch.delete(likeRef)
            }
            Log.d("CommentsRepository", "Prepared batch to delete ${likeDocRefsToDelete.size} likes for comment ${comment.id}")

            // c) Decrement the post's comment count
            val postMetricsCommentsPath = "${FeedPost.POST_METRICS}.${PostMetrics.POST_METRICS_COMMENTS}"
            batch.update(postDocRef, postMetricsCommentsPath, FieldValue.increment(-1))

            // 4. Commit the batch
            batch.commit().await()
            Log.d("CommentsRepository", "Comment ${comment.id} and associated data deleted successfully.")
            ResultWrapper.Success(Unit)

        } catch (e: Exception) {
            Log.e("CommentsRepository", "Error deleting comment ${comment.id}", e)
            ResultWrapper.Error(e)
        }
    }




}