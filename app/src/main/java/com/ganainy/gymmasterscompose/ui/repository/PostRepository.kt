package com.ganainy.gymmasterscompose.ui.repository

import android.net.Uri
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
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
    val userRepository: IUserRepository, val database: FirebaseDatabase,
    private val hashtagRepository: IHashtagRepository,
    private val storage: FirebaseStorage
) :
    IPostRepository {
    private val postsRef = database.reference.child(POSTS_COLLECTION)
    override suspend fun getPostMetrics(postId: String): ResultWrapper<PostMetrics> {
        return try {
            val metricsSnapshot = postsRef.child(postId)
                .child(FeedPost.POST_METRICS)
                .get()
                .await()

            if (metricsSnapshot.exists()) {
                val metrics = metricsSnapshot.getValue(PostMetrics::class.java)
                    ?: return ResultWrapper.Error(Exception("Failed to parse post metrics"))
                ResultWrapper.Success(metrics)
            } else {
                ResultWrapper.Error(Exception("Post metrics not found"))
            }
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

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