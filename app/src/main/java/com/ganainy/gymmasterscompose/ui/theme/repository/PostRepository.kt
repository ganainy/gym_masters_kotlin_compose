package com.ganainy.gymmasterscompose.ui.theme.repository

import com.ganainy.gymmasterscompose.Constants
import com.ganainy.gymmasterscompose.Constants.ID
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.post.LIKES
import com.ganainy.gymmasterscompose.ui.theme.models.post.POST_CREATOR
import com.ganainy.gymmasterscompose.ui.theme.models.post.POST_METRICS
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostLike
import com.ganainy.gymmasterscompose.utils.Utils.generateRandomId
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

// Posts and feed management
interface IPostRepository {
    suspend fun createPost(feedPost: FeedPost): ResultWrapper<Unit>
    suspend fun togglePostReaction(userId: String?, postId: String): ResultWrapper<Unit>
    suspend fun isPostLikedByUser(
        userId: String?,
        postId: String
    ): Flow<ResultWrapper<Boolean>>

    suspend fun checkPostsLikeStatus(postIds: Set<String>, userId: String): Map<String, Boolean>
    fun getPostsForUsers(userIds: Set<String>): Flow<List<FeedPost>>
}


class PostRepository @Inject constructor(
    val userRepository: IUserRepository, val database: FirebaseDatabase,
) :
    IPostRepository {


    /**
     * Retrieves posts for a set of user IDs.
     *
     * @param userIds A Flow emitting sets of user IDs for which to retrieve posts.
     * @return A Flow emitting lists of FeedPost objects corresponding to the user IDs.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getPostsForUsers(userIds: Set<String>): Flow<List<FeedPost>> = flow {
        if (userIds.isEmpty()) {
            emit(emptyList<FeedPost>())
        } else {
            val posts = database.getReference(Constants.POSTS)
                .orderByChild("${POST_CREATOR}/$ID")
                .startAt(userIds.first())
                .endAt(userIds.last() + "\uf8ff")
                .limitToFirst(20)
                .get()
                .await()
                .children
                .mapNotNull { snapshot ->
                    snapshot.key?.let {
                        snapshot.getValue(FeedPost::class.java)?.copy(id = it)
                    }
                }
            emit(posts)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Checks the like status of multiple posts for a specific user.
     *
     * @param postIds The set of post IDs to check.
     * @param userId The ID of the user whose like status is to be checked.
     * @return A map where the keys are post IDs and the values are Booleans indicating whether each post is liked by the user.
     */
    override suspend fun checkPostsLikeStatus(
        postIds: Set<String>,
        userId: String
    ): Map<String, Boolean> = withContext(Dispatchers.IO) {
        try {
            val likeStatuses = mutableMapOf<String, Boolean>()

            // Check all posts in one batch
            postIds.forEach { postId ->
                val likeId = PostLike.createId(userId, postId)
                val likeDoc = database.getReference("${Constants.POST_LIKES}/$likeId")
                    .get()
                    .await()

                likeStatuses[postId] = likeDoc.exists()
            }

            likeStatuses
        } catch (e: Exception) {
            emptyMap()
        }
    }


    override suspend fun createPost(feedPost: FeedPost): ResultWrapper<Unit> {
        val userResult = userRepository.getUserDetails(null)
        if (userResult is ResultWrapper.Success<User>) {
            val user = userResult.data
            val modifiedFeedPost = feedPost.copy(
                id = generateRandomId(
                    Constants.POST
                ),
                postCreator = PostCreator(
                    id = user.id,
                    displayName = user.displayName,
                    profilePictureUrl = user.profilePictureUrl ?: ""
                ),
                createdAt = System.currentTimeMillis()
            )

            return try {
                val postRef = database.getReference("${Constants.POSTS}/${modifiedFeedPost.id}")
                postRef.setValue(modifiedFeedPost).await()
                ResultWrapper.Success(Unit)
            } catch (e: Exception) {
                ResultWrapper.Error(Exception("Error creating post"))
            }

        } else {
            return ResultWrapper.Error(Exception("Error creating post"))
        }

    }

    /**
     * Toggles the reaction (like/unlike) on a post for a specific user.
     *
     * @param userId The ID of the user performing the action. If null, the currently authenticated user is used.
     * @param postId The ID of the post to toggle the reaction on.
     * @return A ResultWrapper containing Unit on success or an error.
     */
    override suspend fun togglePostReaction(userId: String?, postId: String): ResultWrapper<Unit> {
        // Determine the user ID to use
        val userId = userId ?: userRepository.getCurrentUserId()

        val likeId = PostLike.createId(userId, postId)
        // Reference to the post like in the database
        val postLikeRef = database.getReference("${Constants.POST_LIKES}/$likeId")

        // Check if the post is already liked by the user
        if (postLikeRef.get().await().exists()) {
            // If liked, remove the like and decrement the like count
            postLikeRef.removeValue().await()
            database.getReference("${Constants.POSTS}/$postId/${POST_METRICS}/${LIKES}")
                .setValue(ServerValue.increment(-1)).await()
        } else {
            // If not liked, add the like and increment the like count
            val postLike = PostLike(
                id = "${userId}_$postId",
                userId = userId,
                postId = postId,
                timestamp = System.currentTimeMillis()
            )
            postLikeRef.setValue(postLike).await()
            database.getReference("${Constants.POSTS}/$postId/${POST_METRICS}/${LIKES}")
                .setValue(ServerValue.increment(1)).await()
        }

        // Return success result
        return ResultWrapper.Success(Unit)
    }

    /**
     * Checks if a post is liked by a specific user.
     *
     * @param userId The ID of the user to check. If null, the currently authenticated user is used.
     * @param postId The ID of the post to check.
     * @return A Flow emitting ResultWrapper containing a Boolean indicating whether the post is liked by the user or an error.
     */
    override suspend fun isPostLikedByUser(
        userId: String?,
        postId: String
    ): Flow<ResultWrapper<Boolean>> = flow {
        // Determine the user ID to use
        val _userId = userId ?: userRepository.getCurrentUserId()
        try {
            // Reference to the post like in the database
            val postLikeRef = database.getReference("${Constants.POST_LIKES}/${_userId}_$postId")
            // Retrieve the post like data
            val postLike = postLikeRef.get().await()
            // Emit success result with whether the post like exists
            emit(ResultWrapper.Success(postLike.exists()))
        } catch (e: Exception) {
            // Emit error result if an exception occurs
            emit(ResultWrapper.Error(Exception("Error checking if post is liked")))
        }
    }

}