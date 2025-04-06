package com.ganainy.gymmasterscompose.ui.models.post

import com.google.firebase.Timestamp

/**
 * Data class representing an entry of a post made by a user.
 *
 * @property createdAt The timestamp when the post was created.
 */
data class PostByUserEntry(
    val createdAt: Timestamp = Timestamp.now(),
)
{
    companion object {
        const val POSTS_BY_USER_COLLECTION = "posts_by_user"
        const val CREATED_AT = "createdAt"

}
}