package com.ganainy.gymmasterscompose.ui.theme.repository

import com.ganainy.gymmasterscompose.Constants.ID
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.models.User.Companion.POST_COUNT
import com.ganainy.gymmasterscompose.ui.theme.models.User.Companion.USERS
import com.ganainy.gymmasterscompose.ui.theme.models.User.Companion.USER_STATS
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost.Companion.LIKES
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost.Companion.POSTS
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost.Companion.POST_CREATOR
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost.Companion.POST_METRICS
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostLike
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostLike.Companion.POST_LIKES
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

// Posts and feed management
interface IPostRepository {
    suspend fun createPost(feedPost: FeedPost, postAuthor: User?): ResultWrapper<Unit>
    suspend fun togglePostReaction(userId: String?, postId: String): ResultWrapper<Unit>
    suspend fun isPostLikedByUser(
        userId: String?,
        postId: String
    ): Flow<ResultWrapper<Boolean>>

    suspend fun checkPostListLikeStatus(postIds: Set<String>, userId: String): Map<String, Boolean>
    suspend fun getPostsForUsers(userIds: Set<String>): Flow<List<FeedPost>>
}


class PostRepository @Inject constructor(
    val userRepository: IUserRepository, val database: FirebaseDatabase,
    private val hashtagRepository: IHashtagRepository
) :
    IPostRepository {


    /**
     * Retrieves posts for a set of user IDs.
     *
     * @param userIds A set of user IDs for which to retrieve posts.
     * @return A Flow emitting lists of FeedPost objects corresponding to the user IDs.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getPostsForUsers(userIds: Set<String>): Flow<List<FeedPost>> =
        channelFlow {
            if (userIds.isEmpty()) {
                send(emptyList<FeedPost>())
            } else {
                val databaseRef = database.getReference(POSTS)

                val listener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val posts = dataSnapshot.children
                            .mapNotNull { snapshot ->
                                snapshot.key?.let {
                                    snapshot.getValue(FeedPost::class.java)?.copy(id = it)
                                }
                            }
                            .filter { post ->
                                userIds.contains(post.postCreator.id)
                            }

                        launch {
                            send(posts)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        close(error.toException())
                    }
                }

                databaseRef.orderByChild("${POST_CREATOR}/$ID")
                    .startAt(userIds.first())
                    .endAt(userIds.last() + "\uf8ff")
                    .addValueEventListener(listener)

                awaitClose {
                    databaseRef.removeEventListener(listener)
                }
            }
        }.flowOn(Dispatchers.IO)


    /**
     * Checks the like status of multiple posts for a specific user.
     *
     * @param postIds The set of post IDs to check.
     * @param userId The ID of the user whose like status is to be checked.
     * @return A map where the keys are post IDs and the values are Booleans indicating whether each post is liked by the user.
     */
    override suspend fun checkPostListLikeStatus(
        postIds: Set<String>,
        userId: String
    ): Map<String, Boolean> = withContext(Dispatchers.IO) {
        try {
            val likeStatuses = mutableMapOf<String, Boolean>()

            // Check all posts in one batch
            postIds.forEach { postId ->
                val likeId = PostLike.createId(userId, postId)
                val likeDoc = database.getReference("${POST_LIKES}/$likeId")
                    .get()
                    .await()

                likeStatuses[postId] = likeDoc.exists()
            }

            likeStatuses
        } catch (e: Exception) {
            emptyMap()
        }
    }


    /**
     * Creates a new post in the database.
     *
     * This function performs the following steps:
     * 1. Copies the provided `FeedPost` object and assigns it a new ID, sets the post creator details,
     *    and sets the current timestamp as the creation time.
     * 2. Prepares a map of updates to be applied to the database, including the new post and incrementing
     *    the post count for the user, updating, creating hashtags.
     * 3. Updates the database with the prepared updates.
     *
     * @param feedPost The `FeedPost` object containing the details of the post to be created.
     * @return A `ResultWrapper` containing `Unit` on success or an error if the operation fails.
     */
    override suspend fun createPost(feedPost: FeedPost, postAuthor: User?): ResultWrapper<Unit> {

        if (postAuthor == null) {
            return ResultWrapper.Error(Exception("couldn't retrieve post because of missing author"))
        }

        val modifiedFeedPost = feedPost.copy(
            id = FeedPost.createId(),
            postCreator = PostCreator(
                id = postAuthor.id,
                displayName = postAuthor.displayName,
                profilePictureUrl = postAuthor.profilePictureUrl ?: ""
            ),
            createdAt = System.currentTimeMillis()
        )

        return try {
            val updates = hashMapOf<String, Any>(
                "${POSTS}/${modifiedFeedPost.id}" to modifiedFeedPost,
                "${USERS}/${postAuthor.id}/${USER_STATS}/${POST_COUNT}" to ServerValue.increment(1)
            )

            // Check if the post has tags
            // If tags exist, increment the use count and update the last used timestamp
            // Otherwise, create new hashtags
            updates.putAll(hashtagRepository.updateHashtags(modifiedFeedPost.tags))


            database.reference.updateChildren(updates).await()
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(Exception("Error creating post"))
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
        return try {
            // Determine the user ID to use
            val effectiveUserId = userId ?: userRepository.getCurrentUserId()
            val likeId = PostLike.createId(effectiveUserId, postId)
            val postLikeRef = database.getReference("${POST_LIKES}/$likeId")

            val updates = mutableMapOf<String, Any?>()

            // Check if the post is already liked by the user
            if (postLikeRef.get().await().exists()) {
                // If liked, remove the like and decrement the like count
                updates["${POST_LIKES}/${likeId}"] = null
                updates["${POSTS}/$postId/${POST_METRICS}/${LIKES}"] = ServerValue.increment(-1)
            } else {
                // If not liked, add the like and increment the like count
                updates["${POST_LIKES}/${likeId}"] = PostLike(
                    id = likeId,
                    userId = effectiveUserId,
                    postId = postId,
                    timestamp = System.currentTimeMillis()
                )
                updates["${POSTS}/$postId/${POST_METRICS}/${LIKES}"] = ServerValue.increment(1)
            }

            database.reference.updateChildren(updates).await()
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
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
        val likeId = PostLike.createId(_userId, postId)
        try {
            // Reference to the post like in the database
            val postLikeRef = database.getReference("${POST_LIKES}/$likeId")
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