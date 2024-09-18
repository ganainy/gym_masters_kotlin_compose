data class User(
    val userId: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val joinDate: Long = 0L,
    val bio: String? = null,
    val profilePictureUrl: String? = null,
)

// wrapper around the user class to add extra fields only locally
data class LocalUser(
    val user: User?,
    var exerciseCount: Int,
    val workoutCount: Int,
    var followersCount: Int,
    val averageRating: String?
)


