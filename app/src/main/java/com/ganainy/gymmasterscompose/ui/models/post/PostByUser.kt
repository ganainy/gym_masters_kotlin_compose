package com.ganainy.gymmasterscompose.ui.models.post

/**
 * Data class representing an entry of a post made by a user.
 *
 * @property createdAt The timestamp when the post was created.
 */
data class PostByUserEntry(
    val createdAt: Long
)
{
    companion object {
        const val POSTS_BY_USER_COLLECTION = "posts_by_user"
        const val CREATED_AT = "createdAt"

}
}