package com.ganainy.gymmasterscompose.ui.repository

import android.net.Uri
import android.util.Log
import com.ganainy.gymmasterscompose.ui.models.User
import com.ganainy.gymmasterscompose.ui.models.User.Companion.POST_COUNT
import com.ganainy.gymmasterscompose.ui.models.User.Companion.USERS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.User.Companion.USER_STATS
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost.Companion.POSTS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.post.PostByUserEntry
import com.ganainy.gymmasterscompose.ui.models.post.PostByUserEntry.Companion.POSTS_BY_USER_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.models.post.PostMetrics
import com.ganainy.gymmasterscompose.utils.Utils.generateRandomId
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Posts and feed management
interface IPostRepository {
    suspend fun createPost(feedPost: FeedPost, postAuthor: User?, selectedImages: List<Uri>): ResultWrapper<Unit>
    suspend fun getPostMetrics(postId: String): ResultWrapper<PostMetrics>
}


class PostRepository @Inject constructor(
    val userRepository: IUserRepository, private val firestore: FirebaseFirestore,
    private val hashtagRepository: IHashtagRepository,
    private val storage: FirebaseStorage
) :
    IPostRepository {

    override suspend fun getPostMetrics(postId: String): ResultWrapper<PostMetrics> {
        return try {
            val postDocRef = firestore.collection(POSTS_COLLECTION).document(postId)
            val snapshot = postDocRef.get().await()

            if (snapshot.exists()) {
                // Option 1: Deserialize the whole post and get metrics
                // val post = snapshot.toObject<FeedPost>()
                // val metrics = post?.postMetrics

                // Option 2: Get the nested map directly (potentially more efficient if only metrics needed)
                @Suppress("UNCHECKED_CAST")
                val metricsMap = snapshot.get(FeedPost.POST_METRICS) as? Map<String, Any>

                if (metricsMap != null) {
                    // Manual conversion or use a helper/library if complex
                    val likes = (metricsMap[PostMetrics.POST_METRICS_LIKES] as? Long) ?: 0L
                    val comments = (metricsMap[PostMetrics.POST_METRICS_COMMENTS] as? Long) ?: 0L
                    val metrics = PostMetrics(likes = likes.toInt(), comments = comments.toInt())
                    ResultWrapper.Success(metrics)
                }
                else {
                    Log.w("PostRepository", "Post metrics field missing or not a map for post $postId")
                    // Return default metrics if field missing
                    ResultWrapper.Success(PostMetrics()) // Or return Error
                }

            } else {
                ResultWrapper.Error(Exception("Post not found with ID: $postId"))
            }
        } catch (e: Exception) {
            Log.e("PostRepository", "Error fetching metrics for post $postId", e)
            ResultWrapper.Error(e)
        }
    }


    override suspend fun createPost(
        feedPost: FeedPost,
        postAuthor: User?,
        selectedImages: List<Uri>
    ): ResultWrapper<Unit> {
        if (postAuthor == null) {
            Log.e("PostRepository", "Cannot create post: Post author is null.")
            return ResultWrapper.Error(Exception("Post author data is missing"))
        }

        // 1. Upload images (No change needed here)
        val imageUrls = try {
            uploadImages(postAuthor.id, selectedImages)
        } catch (e: Exception) {
            Log.e("PostRepository", "Image upload failed during post creation", e)
            return ResultWrapper.Error(Exception("Failed to upload images: ${e.message}", e))
        }

        // 2. Prepare FeedPost data
        val postId = FeedPost.createId() // Generate unique ID for the post
        val createdAt =Timestamp.now()
        val modifiedFeedPost = feedPost.copy(
            id = postId,
            postCreator = PostCreator( // Ensure PostCreator is Firestore compatible
                id = postAuthor.id,
                displayName = postAuthor.displayName,
                profilePictureUrl = postAuthor.profilePictureUrl ?: ""
            ),
            createdAt = createdAt,
            imageUrlList = imageUrls,
            tags = feedPost.tags.map { it.lowercase().trim() }.filter { it.isNotEmpty() }.distinct() // Normalize tags
            // Ensure PostMetrics() is initialized correctly
        )

        // 3. Prepare Firestore WriteBatch
        val batch = firestore.batch()
        try {
            // Define document references
            val postDocRef = firestore.collection(POSTS_COLLECTION).document(postId)
            val userDocRef = firestore.collection(USERS_COLLECTION).document(postAuthor.id)
            // Optional: Reference for PostByUserEntry if keeping that pattern
            val postByUserDocRef = firestore.collection(POSTS_BY_USER_COLLECTION)
                .document(postAuthor.id) // User ID as document ID
                .collection("user_posts") // Subcollection for user's posts
                .document(postId)        // Post ID as document ID in subcollection


            // Add operations to batch:
            // a) Set the main post document
            batch.set(postDocRef, modifiedFeedPost)

            // b) Update user's post count
            batch.update(userDocRef, "$USER_STATS.$POST_COUNT", FieldValue.increment(1))

            // c) Optional: Add entry to user's post subcollection
            batch.set(postByUserDocRef, PostByUserEntry(createdAt = createdAt))

            // d) Add hashtag updates to the batch using the refactored repository
            hashtagRepository.addHashtagUpdatesToBatch(batch, modifiedFeedPost.tags)

            // 4. Commit the batch
            batch.commit().await()
            Log.d("PostRepository", "Post created successfully with ID: $postId")
            return ResultWrapper.Success(Unit)

        } catch (e: Exception) {
            Log.e("PostRepository", "Error committing post creation batch for post ID: $postId", e)
            // TODO: Consider deleting uploaded images if batch fails?
            return ResultWrapper.Error(Exception("Error creating post: ${e.message}", e))
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