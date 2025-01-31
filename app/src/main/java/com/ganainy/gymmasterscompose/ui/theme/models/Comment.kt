package com.ganainy.gymmasterscompose.ui.theme.models

/**
 * Data class representing a comment on a post.
 *
 * @property id The unique identifier for the comment.
 * @property postId The unique identifier of the post the comment is associated with.
 * @property userId The unique identifier of the user who made the comment.
 * @property content The content of the comment.
 * @property timestamp The timestamp when the comment was made.
 * @property likes The number of likes the comment has received.
 * @property replies A map of replies to the comment, where the key is the reply ID and the value is the CommentReply object.
 */
data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    val likes: Int = 0,
    val replies: Map<String, CommentReply> = emptyMap()
)

/**
 * Data class representing a reply to a comment.
 *
 * @property id The unique identifier for the comment reply.
 * @property userId The unique identifier of the user who made the reply.
 * @property content The content of the reply.
 * @property timestamp The timestamp when the reply was made.
 * @property likes The number of likes the reply has received.
 */
data class CommentReply(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    val likes: Int = 0
)

/**
 * Data class representing a like on a comment.
 *
 * @property id The unique identifier for the comment like.
 * @property userId The unique identifier of the user who liked the comment.
 * @property commentId The unique identifier of the comment that was liked.
 * @property postId The unique identifier of the post the comment is associated with.
 * @property timestamp The timestamp when the comment was liked.
 */
data class CommentLike(
    val id: String = "",
    val userId: String = "",
    val commentId: String = "",
    val postId: String = "",
    val timestamp: Long = 0
)