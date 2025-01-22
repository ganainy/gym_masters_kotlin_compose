data class User(
    val profile: Profile = Profile(),
    val stats: Stats = Stats()
)

data class Profile(
    val id: String = "",
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val joinDate: Long = 0, // Timestamp
    val profilePictureUrl: String? = null,
    val bio: String? = null,
    val lastActive: Long? = null // Timestamp
)

data class Stats(
    val postCount: Int = 0,
    val workoutCount: Int = 0,
    val exerciseCount: Int = 0,
    var followersCount: Int = 0,
    val followingCount: Int = 0
)
