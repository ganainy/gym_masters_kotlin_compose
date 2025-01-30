package com.ganainy.gymmasterscompose.ui.theme.repository

import com.ganainy.gymmasterscompose.Constants
import com.ganainy.gymmasterscompose.ui.theme.models.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.PostStats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Posts and feed management
interface IPostRepository {
    suspend fun createPost(feedPost: FeedPost): ResultWrapper<Unit>
    suspend fun togglePostReaction(postId: String, reactionType: String): ResultWrapper<Unit>
    suspend fun getUserReactionForPost(postId: String, userId: String): String?
    fun getPostsByUsers(userIds: Set<String>): Flow<List<FeedPost>>
    fun getPostStats(postId: String) : Flow<ResultWrapper<PostStats>>
}




class PostRepository @Inject constructor( val auth : FirebaseAuth,val database: FirebaseDatabase,) : IPostRepository{
    override suspend fun createPost(feedPost: FeedPost): ResultWrapper<Unit> {
        return try {
            val postRef = database.getReference("posts/${feedPost.id}")
            postRef.setValue(feedPost).await()
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(Exception("Error creating post"))
        }
    }



    override suspend fun togglePostReaction(postId: String, reactionType: String): ResultWrapper<Unit> {
        return try {
            val userId = auth.currentUser?.uid
            val reactionRef = database.getReference("reactions/$postId/reactions/$userId")

            // Check if reaction exists
            val currentReaction = reactionRef.get().await()

            val updates = mutableMapOf<String, Any?>()

            if (currentReaction.exists()) {
                // Remove reaction
                updates["reactions/$postId/reactions/$userId"] = null
                updates["posts/$postId/stats/likes"] = ServerValue.increment(-1)
            } else {
                // Add reaction
                updates["reactions/$postId/reactions/$userId"] = mapOf(
                    "userId" to userId,
                    "type" to "LIKE",
                    "timestamp" to ServerValue.TIMESTAMP
                )
                updates["posts/$postId/stats/likes"] = ServerValue.increment(1)
            }

            // Perform updates atomically
            database.reference.updateChildren(updates).await()
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(Exception("Error updating reaction"))
        }
    }



    override suspend fun getUserReactionForPost(postId: String, userId: String): String? {
        return try {
            val reactionRef = database.getReference("reactions/$postId/reactions/$userId")
            val snapshot = reactionRef.get().await()
            if (snapshot.exists()) {
                snapshot.child("type").getValue(String::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }


    override fun getPostsByUsers(userIds: Set<String>): Flow<List<FeedPost>> {
        TODO("Not yet implemented")
    }

    override fun getPostStats(postId: String): Flow<ResultWrapper<PostStats>> {
        return callbackFlow {
            try {
                val statsRef = database.getReference("${Constants.POST_STATS}/$postId")
                val snapshot = statsRef.get().await()
                val stats = snapshot.getValue(PostStats::class.java)
                if (stats != null) {
                    trySend(ResultWrapper.Success(stats))
                } else {
                    trySend(ResultWrapper.Error(Exception("Stats not found")))
                }
            } catch (e: Exception) {
                trySend(ResultWrapper.Error(e))
            }
            awaitClose { }
        }
    }
}