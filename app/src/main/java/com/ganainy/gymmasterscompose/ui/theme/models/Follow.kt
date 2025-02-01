package com.ganainy.gymmasterscompose.ui.theme.models

/**
 * Data class representing a Follow relationship between users.
 *
 * @property id The unique identifier for the follow relationship.
 * @property followerId The unique identifier of the user who is following.
 * @property followedId The unique identifier of the user being followed.
 * @property timestamp The timestamp when the follow relationship was created.
 */
data class Follow(
    val id: String = "", // Will be "$followerId_$followedId"
    val followerId: String = "",
    val followedId: String = "",
    val timestamp: Long = 0
){
    companion object {
        fun createId(followerId: String, followedId: String) = "${followerId}_${followedId}"
        const val FOLLOWER_ID = "followerId"
    const val FOLLOWS = "follows" // Collection name for follows
    }

}


