package com.ganainy.gymmasterscompose.ui.models

/**
 * Data class representing a notification.
 *
 * @property id The unique identifier for the notification.
 * @property userId The unique identifier of the user receiving the notification.
 * @property type The type of the notification (e.g., LIKE, COMMENT, FOLLOW, etc.).
 * @property fromUserId The unique identifier of the user who triggered the notification.
 * @property entityId The unique identifier of the entity associated with the notification (e.g., postId, workoutId, commentId).
 * @property timestamp The timestamp when the notification was created.
 * @property read A boolean indicating whether the notification has been read.
 */
data class Notification(
    val id: String = "",
    val userId: String = "",
    val type: NotificationType = NotificationType.LIKE,
    val fromUserId: String = "",
    val entityId: String = "", // postId/workoutId/commentId
    val timestamp: Long = 0,
    val read: Boolean = false
)

/**
 * Enum class representing the type of notification.
 */
enum class NotificationType {
    LIKE,
    COMMENT,
    FOLLOW,
    WORKOUT_SAVE,
    MENTION
}
