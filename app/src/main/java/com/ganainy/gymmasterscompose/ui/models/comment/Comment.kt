import com.google.firebase.Timestamp

// Comment data class with embedded user info
data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val likesCount: Int = 0,
    /*val parentId: String? = null,    // For future reply support
    val replyCount: Int = 0,         // For future reply support*/
    // Embedded user display data
    val userDisplayInfo: UserDisplayInfo = UserDisplayInfo(),
    val isPending: Boolean = false, // True if the comment is pending (e.g. local creation) to show comment
    // immediately greyed out until it's added to the database
)
{
    companion object
    {
        const val TIMESTAMP_FIELD = "timestamp"
        const val POST_ID_FIELD = "postId"
        const val COMMENTS_COLLECTION = "comments"
        const val COMMENTS_LIKES_COUNT = "likesCount"
    }
}

// User display info data class with display name and profile image url
data class UserDisplayInfo(
    val displayName: String = "",
    val profileImageUrl: String? = null,
)
