package com.ganainy.gymmasterscompose.ui.repository

import android.net.Uri
import com.ganainy.gymmasterscompose.ui.models.User
import com.ganainy.gymmasterscompose.ui.models.User.Companion.POST_COUNT
import com.ganainy.gymmasterscompose.ui.models.User.Companion.USERS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.User.Companion.USER_STATS
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost.Companion.POSTS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost.Companion.POST_METRICS
import com.ganainy.gymmasterscompose.ui.models.post.PostByUserEntry
import com.ganainy.gymmasterscompose.ui.models.post.PostByUserEntry.Companion.POSTS_BY_USER_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.models.post.PostLike
import com.ganainy.gymmasterscompose.ui.models.post.PostLike.Companion.POST_LIKES_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.post.PostMetrics
import com.ganainy.gymmasterscompose.utils.Utils.generateRandomId
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Posts and feed management
interface IPostRepository {
    suspend fun createPost(feedPost: FeedPost, postAuthor: User?, selectedImages: List<Uri>): ResultWrapper<Unit>
    suspend fun observePostsLikes(postIds: Set<String>, userId: String?): Flow<Map<String, Boolean>>
    suspend fun observePostsOfUsers(
        userIds: Set<String>,
        lastPostTimestamp: Long?
    ): Flow<List<FeedPost>>

    fun observePostMetricsUpdates(postId: String): Flow<PostMetrics>
}


class PostRepository @Inject constructor(
    val userRepository: IUserRepository, val database: FirebaseDatabase,
    private val hashtagRepository: IHashtagRepository,
    private val storage: FirebaseStorage
) :
    IPostRepository {

    //todo fix more posts loading same initial posts
    // Number of posts to retrieve per page
    private val PAGE_SIZE = 20

    override suspend fun observePostsOfUsers(
        userIds: Set<String>,
        lastPostTimestamp: Long?
    ): Flow<List<FeedPost>> = callbackFlow {
        // Handle empty user IDs case
        // WHY: Ensures immediate emission of empty list and flow completion
        if (userIds.isEmpty()) {
            trySend(emptyList())
            close() // Explicitly close the flow to signal completion
            return@callbackFlow
        }

        val currentPosts = mutableMapOf<String, FeedPost>()
        val listenerMap = mutableMapOf<String, Pair<Query, ValueEventListener>>()

        // Function to emit current posts
        // WHY: Centralizes emission logic and ensures sorted output
        fun emitPosts() {
            val sortedPosts = currentPosts.values.toList().sortedByDescending { it.createdAt }
            trySend(sortedPosts)
            // If no posts are available after processing, close the flow
            // WHY: Prevents indefinite loading when no data is available
            if (sortedPosts.isEmpty() && listenerMap.isNotEmpty()) {
                close()
            }
        }

        try {
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
                        // WHY: Prevents memory leaks and duplicate listeners
                        listenerMap.entries
                            .filter { it.key.startsWith("post_$userId") }
                            .forEach { (key, pair) ->
                                pair.first.removeEventListener(pair.second)
                                listenerMap.remove(key)
                            }

                        // Handle empty snapshot case
                        // WHY: Ensures proper handling when no posts exist
                        if (!snapshot.exists() || snapshot.childrenCount.toInt() == 0) {
                            currentPosts.entries
                                .removeIf { it.key.startsWith("post_$userId") }
                            emitPosts()
                            return
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
                                    // Log error but keep the flow going
                                    // WHY: Prevents flow termination on individual post errors
                                    println("Error loading post $postId: ${error.message}")
                                }
                            })
                            listenerMap["post_${userId}_$postId"] = postRef to postListener
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Close flow with error
                        // WHY: Properly handles user-level query cancellation
                        close(error.toException())
                    }
                })

                listenerMap["user_$userId"] = userPostsRef to userListener
            }
        } catch (e: Exception) {
            // Handle setup errors
            // WHY: Ensures flow closes with error if setup fails
            close(e)
        }

        awaitClose {
            // Clean up all listeners
            // WHY: Prevents memory leaks
            listenerMap.forEach { (_, pair) ->
                pair.first.removeEventListener(pair.second)
            }
            listenerMap.clear()
        }
    }.catch { e ->
        // Handle flow-level errors
        // WHY: Ensures errors don't terminate the flow unexpectedly
        emit(emptyList())
        println("Flow error: ${e.message}")
    }

    override fun observePostMetricsUpdates(postId: String): Flow<PostMetrics> {
        return callbackFlow {
            val postMetricsRef = database.getReference(POSTS_COLLECTION).child(postId).child(POST_METRICS)
            val listener = postMetricsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val metrics = snapshot.getValue(PostMetrics::class.java)
                    if (metrics != null) {
                        trySend(metrics)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })

            awaitClose {
                postMetricsRef.removeEventListener(listener)
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
                val likeDoc = database.getReference("${POST_LIKES_COLLECTION}/$likeId")
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
     * This function handles the creation of a new feed post, including uploading images,
     * associating the post with its author, and updating relevant counters and indices.
     *
     * @param feedPost The [FeedPost] object containing the content of the post. This object should not contain the `id`, `postCreator`, `createdAt`, and `imageUrlList` properties.
     *                 These properties will be generated and set within the function.
     * @param postAuthor The [User] object representing the author of the post. If null, an error is returned.
     * @param selectedImages A list of [Uri] objects representing the images to be included in the post.
     *                       These images will be uploaded, and their URLs will be stored in the post.
     *
     * @return A [ResultWrapper] object indicating the success or failure of the operation.
     *         - [ResultWrapper.Success] with [Unit] if the post was created successfully.
     *         - [ResultWrapper.Error] with an [Exception] if an error occurred during post creation.
     *           Possible error conditions include:
     *           - `postAuthor` is null.
     *           - Errors during image upload.
     *           - Errors during database operations.
     *
     * @throws Exception if any operation within the try block fails. Exceptions are caught and wrapped in a ResultWrapper.Error.
     *
     * **Process:**
     * 1. **Author Check:** Verifies that `postAuthor` is not null. If it is, returns an error.
     * 2. **Image Upload:** Uploads the images specified in `selectedImages` to Firebase Storage.
     *    Returns a list of download URLs for the uploaded images.
     * 3. **Post Creation:**
     *    - Generates a unique ID for the post using `FeedPost.createId()`.
     *    - Creates a [PostCreator] object from the `postAuthor` information.
     *    - Sets the creation timestamp (`createdAt`) to the current time.
     *    - Creates a copy of the provided `feedPost` object using its copy method and fill all the missing data: `id`, `postCreator`, `createdAt` and `imageUrlList`.
     */
    override suspend fun createPost(
        feedPost: FeedPost,
        postAuthor: User?,
        selectedImages: List<Uri>
    ): ResultWrapper<Unit> {
        if (postAuthor == null) {
            return ResultWrapper.Error(Exception("couldn't retrieve post because of missing author"))
        }

        return try {
            // Upload images first and get their download URLs
            val imageUrls = uploadImages(postAuthor.id, selectedImages)

            val createdAt = System.currentTimeMillis()
            val modifiedFeedPost = feedPost.copy(
                id = FeedPost.createId(),
                postCreator = PostCreator(
                    id = postAuthor.id,
                    displayName = postAuthor.displayName,
                    profilePictureUrl = postAuthor.profilePictureUrl ?: ""
                ),
                createdAt = createdAt,
                imageUrlList = imageUrls // Add the image URLs to the post
            )

            val updates = hashMapOf<String, Any>(
                "${POSTS_COLLECTION}/${modifiedFeedPost.id}" to modifiedFeedPost,
                "${USERS_COLLECTION}/${postAuthor.id}/${USER_STATS}/${POST_COUNT}" to ServerValue.increment(1),
                "$POSTS_BY_USER_COLLECTION/${modifiedFeedPost.postCreator.id}/${modifiedFeedPost.id}" to PostByUserEntry(
                    createdAt = modifiedFeedPost.createdAt
                )
            )

            updates.putAll(hashtagRepository.updateHashtags(modifiedFeedPost.tags))

            database.reference.updateChildren(updates).await()
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(Exception("Error creating post: ${e.message}"))
        }
    }

    /**
     * Uploads a list of images to Firebase Storage for a specific user.
     *
     * This function takes a user ID and a list of image URIs, uploads each image
     * to Firebase Storage under a user-specific directory, and returns a list of
     * download URLs for the uploaded images.
     *
     * @param userId The ID of the user uploading the images. This is used to
     *               organize images within the storage bucket.
     * @param images A list of image URIs representing the images to be uploaded.
     *               These URIs should point to local files.
     * @return A list of strings, where each string is the download URL of an
     *         uploaded image. The order of the URLs corresponds to the order of
     *         the input image URIs.
     * @throws Exception If any image fails to upload, an exception is thrown
     *                   with a message describing the failure.
     *
     * Example usage:
     * ```kotlin
     * val userId = "user123"
     * val imageUris = listOf(Uri.parse("file:///path/to/image1.jpg"), Uri.parse("file:///path/to/image2.png"))
     * try {
     *     val downloadUrls = uploadImages(userId, imageUris)
     *     // Use the downloadUrls (e.g., store them in a database)
     * } catch (e: Exception) {
     *     // Handle the upload failure (e.g., show an error message)
     *     println("Image upload failed: ${e.message}")
     * }
     * ```
     *
     * Note: This function uses coroutines for asynchronous operations. It
     *       is designed to be called within a coroutine scope.
     */
    private suspend fun uploadImages(userId: String, images: List<Uri>): List<String> = coroutineScope {
        val storageRef = storage.reference

        images.map { imageUri ->
            async {
                try {
                    // Create a unique filename for each image
                    val filename = "${generateRandomId("image")}.jpg"
                    val imageRef = storageRef
                        .child(POSTS_COLLECTION)
                        .child(userId)
                        .child(filename)

                    // Upload the image
                    imageRef.putFile(imageUri).await()

                    // Get the download URL
                    imageRef.downloadUrl.await().toString()
                } catch (e: Exception) {
                    throw Exception("Failed to upload image: ${e.message}")
                }
            }
        }.awaitAll() // Wait for all uploads to complete
    }





}