package com.ganainy.gymmasterscompose.ui.theme.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "",
    @ColumnInfo(name = "display_name") val displayName: String = "",
    @ColumnInfo(name = "username") val username: String = "",
    @ColumnInfo(name = "email") val email: String = "",
    @ColumnInfo(name = "join_date") val joinDate: Long = 0, // Timestamp
    @ColumnInfo(name = "profile_picture_url") val profilePictureUrl: String? = null,
    @ColumnInfo(name = "bio") val bio: String? = null,
    @ColumnInfo(name = "last_active") val lastActive: Long? = null // Timestamp
)

@Entity(tableName = "stats")
data class UserStats(
    @PrimaryKey val userId: String = "",
    @ColumnInfo(name = "post_count") val postCount: Int = 0,
    @ColumnInfo(name = "workout_count") val workoutCount: Int = 0,
    @ColumnInfo(name = "followers_count") var followersCount: Int = 0,
    @ColumnInfo(name = "following_count") val followingCount: Int = 0
)