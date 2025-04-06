package com.ganainy.gymmasterscompose.ui.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

/**
 * Database entities for caching different types of likes
 */
@Entity(tableName = "cached_likes")
data class CachedLike(
    @PrimaryKey
    val id: String,
    val userId: String,
    val targetId: String, // postId, workoutId, or commentId
    val postId: String?, // Only used for comment likes
    val likeType: LikeType,
    val timestamp: Timestamp = Timestamp.now(),
    val isPending: Boolean = false,
    val isLiked: Boolean = true
)

enum class LikeType {
    POST,
    COMMENT,
    WORKOUT
}