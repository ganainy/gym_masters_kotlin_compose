package com.ganainy.gymmasterscompose.ui.models

import com.google.firebase.Timestamp

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
    val timestamp: Timestamp = Timestamp.now()
){
    companion object {
        fun createId(followerId: String, followedId: String) = "${followerId}_${followedId}"
        const val FOLLOWER_ID = "followerId"
        const val FOLLOWED_ID = "followedId"
    const val FOLLOWS_COLLECTION = "follows" // Collection name for follows
    }

}


