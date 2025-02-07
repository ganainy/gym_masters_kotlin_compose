package com.ganainy.gymmasterscompose.ui.theme.models.post


/**
 * Data class representing a like on a post.
 *
 * @property id The unique identifier for the post like.
 * @property userId The unique identifier of the user who liked the post.
 * @property postId The unique identifier of the post that was liked.
 * @property timestamp The timestamp when the post was liked.
 */
data class PostLike(
    val id: String = "", // Will be "$userId_$postId"
    val userId: String = "",
    val postId: String = "",
    val timestamp: Long = 0
)
 {
    companion object {
        fun createId(userId: String, postId: String) = "${userId}_${postId}"
        const val POST_LIKES = "post_likes" // Collection name for post likes
    }
}