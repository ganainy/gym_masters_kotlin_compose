package com.ganainy.gymmasterscompose.ui.theme.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class User(
    val id: String = "",
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val joinDate: Long = 0, // Timestamp
    val profilePictureUrl: String? = null,
    val bio: String? = null,
    val lastActive: Long? = null, // Timestamp
    val stats: UserStats = UserStats(),
    val settings: UserSettings = UserSettings()
){
    companion object {
        const val USERS_COLLECTION = "users"
        const val USER_STATS = "stats"
        const val POST_COUNT = "postCount"
        const val FOLLOWING_COUNT = "followingCount"
        const val FOLLOWERS_COUNT = "followersCount"
        const val FOLLOWER_ID = "followerId"
        const val FOLLOWED_ID = "followedId"
        const val USER_IMAGES = "userImages"
        const val PROFILE_PICTURE_URL = "profilePictureUrl"
    }
}

data class UserSettings(
    val notifications: Boolean = true,
    val privacy: String = "public", // public, private, friends
    val language: String = "en"
)


@Entity(tableName = "stats")
data class UserStats(
    @PrimaryKey val userId: String = "",
    @ColumnInfo(name = "post_count") val postCount: Int = 0,
    @ColumnInfo(name = "workout_count") val workoutCount: Int = 0,
    @ColumnInfo(name = "followers_count") var followersCount: Int = 0,
    @ColumnInfo(name = "following_count") val followingCount: Int = 0
)