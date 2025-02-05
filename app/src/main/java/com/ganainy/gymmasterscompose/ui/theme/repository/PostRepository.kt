package com.ganainy.gymmasterscompose.ui.theme.repository

import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.models.User.Companion.POST_COUNT
import com.ganainy.gymmasterscompose.ui.theme.models.User.Companion.USERS
import com.ganainy.gymmasterscompose.ui.theme.models.User.Companion.USER_STATS
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost.Companion.LIKES
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost.Companion.POSTS_COLLECTION
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost.Companion.POST_METRICS
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostByUserEntry
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostByUserEntry.Companion.POSTS_BY_USER
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostLike
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostLike.Companion.POST_LIKES
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Posts and feed management
interface IPostRepository {
    suspend fun createPost(feedPost: FeedPost, postAuthor: User?): ResultWrapper<Unit>
    suspend fun togglePostReaction(userId: String?, postId: String): ResultWrapper<Unit>
    suspend fun observeIsPostLikedByUser(
        userId: String?,
        postId: String
    ): Flow<ResultWrapper<Boolean>>

    suspend fun observePostsLikes(postIds: Set<String>, userId: String?): Flow<Map<String, Boolean>>
    suspend fun observePostsOfUsers(
        userIds: Set<String>,
        lastPostTimestamp: Long?
    ): Flow<List<FeedPost>>
}


class PostRepository @Inject constructor(
    val userRepository: IUserRepository, val database: FirebaseDatabase,
    private val hashtagRepository: IHashtagRepository
) :
    IPostRepository {

    //todo fix more posts loading same initial posts
    // Number of posts to retrieve per page
    private val PAGE_SIZE = 20

    override suspend fun observePostsOfUsers(
        userIds: Set<String>,
        lastPostTimestamp: Long?
    ): Flow<List<FeedPost>> = callbackFlow {
        if (userIds.isEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }

        val currentPosts = mutableMapOf<String, FeedPost>()
        val listenerMap = mutableMapOf<String, Pair<Query, ValueEventListener>>()

        // Function to emit current posts
        fun emitPosts() {
            trySend(currentPosts.values.toList().sortedByDescending { it.createdAt })
        }

        // Listener for each user
        userIds.forEach { userId ->
            val userPostsRef = database.getReference("posts_by_user/$userId")
                .orderByChild("createdAt")
                .limitToLast(PAGE_SIZE)
                .apply {
                    lastPostTimestamp?.let { timestamp ->
                        endBefore(timestamp.toDouble())
                    }
                }

            val userListener = userPostsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Remove old post listeners for this user
                    listenerMap.entries
                        .filter { it.key.startsWith("post_$userId") }
                        .forEach { (key, pair) ->
                            pair.first.removeEventListener(pair.second)
                            listenerMap.remove(key)
                        }

                    // Add listeners for each post
                    snapshot.children.forEach { postSnapshot ->
                        val postId = postSnapshot.key ?: return@forEach

                        val postRef = database.getReference("posts/$postId")
                        val postListener = postRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(postSnapshot: DataSnapshot) {
                                val post = postSnapshot.getValue(FeedPost::class.java)
                                if (post != null) {
                                    currentPosts[postId] = post
                                } else {
                                    currentPosts.remove(postId)
                                }
                                emitPosts()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error but keep the flow going
                                println("Error loading post $postId: ${error.message}")
                            }
                        })
                        listenerMap["post_${userId}_$postId"] = postRef to postListener
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })

            listenerMap["user_$userId"] = userPostsRef to userListener
        }

        awaitClose {
            listenerMap.forEach { (_, pair) ->
                pair.first.removeEventListener(pair.second)
            }
        }
    }


    /**
     * Checks the like status of multiple posts for a specific user.
     * if userId is null, the currently authenticated user is used.
     *
     * @param postIds The set of post IDs to check.
     * @param userId The ID of the user whose like status is to be checked.
     * @return A map where the keys are post IDs and the values are Booleans indicating whether each post is liked by the user.
     */
    override suspend fun observePostsLikes(
        postIds: Set<String>,
        userId: String?
    ): Flow<Map<String, Boolean>> = flow {
        try {
            val userId = userId ?: userRepository.getCurrentUserId()
            val likeStatuses = mutableMapOf<String, Boolean>()

            // Check all posts in one batch
            postIds.forEach { postId ->
                val likeId = PostLike.createId(userId, postId)
                val likeDoc = database.getReference("${POST_LIKES}/$likeId")
                    .get()
                    .await()

                likeStatuses[postId] = likeDoc.exists()
            }

            emit(likeStatuses)
        } catch (e: Exception) {
            emit(emptyMap())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Creates a new post in the database.
     *
     * This function performs the following steps:
     * 1. Copies the provided `FeedPost` object and assigns it a new ID, sets the post creator details,
     *    and sets the current timestamp as the creation time.
     * 2. Prepares a map of updates to be applied to the database, including the new post and incrementing
     *    the post count for the user, updating, creating hashtags and creating an entry in the post by user collection.
     * 3. Updates the database with the prepared updates.
     *
     * @param feedPost The `FeedPost` object containing the details of the post to be created.
     * @return A `ResultWrapper` containing `Unit` on success or an error if the operation fails.
     */
    override suspend fun createPost(feedPost: FeedPost, postAuthor: User?): ResultWrapper<Unit> {

        if (postAuthor == null) {
            return ResultWrapper.Error(Exception("couldn't retrieve post because of missing author"))
        }

        val createdAt = System.currentTimeMillis()
        val modifiedFeedPost = feedPost.copy(
            id = FeedPost.createId(),
            postCreator = PostCreator(
                id = postAuthor.id,
                displayName = postAuthor.displayName,
                profilePictureUrl = postAuthor.profilePictureUrl ?: ""
            ),
            createdAt = createdAt
        )

        return try {
            val updates = hashMapOf<String, Any>(
                "${POSTS_COLLECTION}/${modifiedFeedPost.id}" to modifiedFeedPost,
                "${USERS}/${postAuthor.id}/${USER_STATS}/${POST_COUNT}" to ServerValue.increment(1),
                "$POSTS_BY_USER/${modifiedFeedPost.postCreator.id}/${modifiedFeedPost.id}" to PostByUserEntry(
                    createdAt = modifiedFeedPost.createdAt
                )
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
                updates["${POSTS_COLLECTION}/$postId/${POST_METRICS}/${LIKES}"] =
                    ServerValue.increment(-1)
            } else {
                // If not liked, add the like and increment the like count
                updates["${POST_LIKES}/${likeId}"] = PostLike(
                    id = likeId,
                    userId = effectiveUserId,
                    postId = postId,
                    timestamp = System.currentTimeMillis()
                )
                updates["${POSTS_COLLECTION}/$postId/${POST_METRICS}/${LIKES}"] =
                    ServerValue.increment(1)
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
    override suspend fun observeIsPostLikedByUser(
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