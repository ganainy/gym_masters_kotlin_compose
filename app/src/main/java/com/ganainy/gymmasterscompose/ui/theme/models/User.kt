package com.ganainy.gymmasterscompose.ui.theme.models

data class User(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var about_me: String? = null,
    var photo: String? = null,

    // list containing the rating of the user from different users
    private var _Ratings: Map<String, Int>? = null,// Private backing field
    val followersUID: Map<String, String>? = null, //list containing the id of the followers
    val followingUID: Map<String, String>? = null, //list containing the id of the following

    //values below are not directly saved in the user node
    var exercisesCount: Int? = null,
    var workoutsCount: Int? = null,
) {
    // Custom getter for followers count
    val followers: Long
        get() = followersUID?.size?.toLong() ?: 0

    // Custom getter for following count
    val following: Long
        get() = followingUID?.size?.toLong() ?: 0

    // Public setter for Ratings
    var Ratings: Map<String, Int>?
        set(value) {
            _Ratings = value
            // Additional logic can be added here if needed
        }
        get() = _Ratings // Private getter to restrict direct access

    // Custom getter for ratings' average
    val ratingsAverage: Double?
        get() {
            val ratings = _Ratings ?: return null
            if (ratings.isEmpty()) return null

            val totalSum = ratings.values.sum()
            return totalSum.toDouble() / ratings.size
        }

}
