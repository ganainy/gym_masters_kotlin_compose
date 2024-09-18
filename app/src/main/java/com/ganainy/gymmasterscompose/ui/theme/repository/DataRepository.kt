package com.ganainy.gymmasterscompose.ui.theme.repository

import LocalUser
import User
import android.util.Log
import com.ganainy.gymmasterscompose.AppConstants.FIREBASE_DATABASE_NAME
import com.ganainy.gymmasterscompose.AppConstants.FOLLOWERS
import com.ganainy.gymmasterscompose.AppConstants.FOLLOWING
import com.ganainy.gymmasterscompose.AppConstants.RATED_USER_ID
import com.ganainy.gymmasterscompose.AppConstants.RATINGS
import com.ganainy.gymmasterscompose.AppConstants.SCORE
import com.ganainy.gymmasterscompose.AppConstants.USERS
import com.ganainy.gymmasterscompose.AppConstants.USER_ID
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.AppUtils.generateRandomUsername
import com.ganainy.gymmasterscompose.ui.theme.models.CustomException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


interface IDataRepository {
    //function related to logged in user
    suspend fun createUser(
        email: String, password: String, name: String, joinDate: Long
    ): Result<User>

    fun getLoggedUser(listener: (Result<User>) -> Unit)
    suspend fun followUnfollowUser(
        userToFollowOrUnfollow: User, onSuccess: () -> Unit, onFailure: (Int) -> Unit,
    )

    fun getCurrentUserId(): String?
    fun getUserFollowing(onSuccess: (Map<String, String>?) -> Unit, onFailure: (Int) -> Unit)

    //function related to other users
    suspend fun getUsers(): StateFlow<List<LocalUser>>
    suspend fun listenForUsersUpdates()
    suspend fun listenForFollowersUpdates()
}

class DataRepository(
    private val authRepo: AuthRepository,
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_DATABASE_NAME)
) : IDataRepository {

    // firebase Id of current logged in user
    private val currentUserUid: String? = authRepo.getCurrentUserId()

    // List to store all users (except logged user)
    private val _userListFlow = MutableStateFlow<List<LocalUser>>(emptyList())
    private val userList = mutableListOf<LocalUser>()


    override suspend fun createUser(
        email: String, password: String, name: String, joinDate: Long
    ): Result<User> {
        return try {
            // First, create the user using Firebase authentication
            val result = authRepo.createUser(email, password)

            // If the creation was successful, map the result to a User object
            result.fold(onSuccess = { uid: String ->
                try {
                    // Create a User object
                    val user = User(
                        userId = uid, name = name, email = email,
                        joinDate = joinDate, username = generateRandomUsername(),
                    )

                    // Save the User object to the database
                    val userRef = database.getReference(USERS).child(user.userId)
                    userRef.setValue(user).await()

                    // Return the success result with the User object
                    Result.success(user)
                } catch (e: Exception) {
                    // Handle any exception that occurs while saving the user to the database
                    Result.failure<User>(e)
                }
            }, onFailure = { createException ->
                // If there was a failure in creating the user, return a failure result
                Result.failure(createException)
            })
        } catch (e: Exception) {
            // Handle any unexpected exceptions
            Result.failure(e)
        }
    }


    override fun getLoggedUser(listener: (Result<User>) -> Unit) {

        try {
            // Set up a real-time listener to continuously listen for changes to the logged user data
            val userRef = database.getReference(USERS).orderByChild(USER_ID)
                .equalTo(authRepo.getCurrentUserId()!!).limitToFirst(1)

            userRef.addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.children.first().getValue(User::class.java)
                    if (user != null) {
                        listener(Result.success(user))
                    } else {
                        listener(
                            Result.failure(
                                CustomException(R.string.user_not_found)
                            )
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle any errors that occur while listening to changes
                    listener(
                        Result.failure(
                            CustomException(R.string.user_not_found)
                        )
                    )
                }
            })
        } catch (e: Exception) {
            listener(
                Result.failure(
                    CustomException(R.string.user_not_found)
                )
            )
        }
    }

    override suspend fun listenForUsersUpdates() {
        // Set up the real-time listener for user updates
        val usersRef = database.getReference(USERS)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Launch a new coroutine to handle suspension functions
                CoroutineScope(Dispatchers.IO).launch {

                    snapshot.children.forEach { userSnapshot ->
                        val userId = userSnapshot.child(USER_ID).value.toString()

                        // Exclude the logged-in user from the list
                        if (userId == currentUserUid) {
                            return@forEach
                        }

                        val user = userSnapshot.getValue(User::class.java) ?: return@forEach

                        /*  val exerciseCount =
                              database.getReference(EXERCISES).orderByChild(USER_ID).equalTo(userId)
                                  .get().await().childrenCount.toInt()
                          val workoutCount =
                              database.getReference(WORKOUTS).orderByChild(USER_ID).equalTo(userId)
                                  .get().await().childrenCount.toInt()
                          val averageRating = getAverageRating(userId)
                          val followersCount = database.getReference(FOLLOWERS).child(userId).get()
                              .await().childrenCount.toInt()*/

                        val userAlreadyInList =
                            userList.any { localUser -> localUser.user?.userId == user.userId }

                        if (!userAlreadyInList) {
                            userList.add(
                                LocalUser(
                                    user = user,
                                    exerciseCount = 0,
                                    workoutCount = 0,
                                    averageRating = null,
                                    followersCount = 0
                                )
                            )
                            _userListFlow.value = userList
                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching user data", error.toException())
            }
        })
    }

    override suspend fun listenForFollowersUpdates() {
        // Set up the real-time listener for user followers updates
        val followersRef = database.getReference(FOLLOWERS)
        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    // Map to store the number of followers for each user
                    val followersCountMap = mutableMapOf<String, Int>()

                    // Populate the map with the follower counts from the snapshot
                    snapshot.children.forEach { followSnapshot ->
                        val followersMap = followSnapshot.getValue<Map<String, String>>()
                        val followersKey = followSnapshot.key
                        if (followersKey != null) {
                            followersCountMap[followersKey] = followersMap?.size ?: 0
                        }
                    }

                    // Update the userList with the new follower counts
                    userList.forEach { localUser ->
                        localUser.followersCount = followersCountMap[localUser.user?.userId] ?: 0
                    }
                    _userListFlow.value = userList


                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching user data", error.toException())
            }
        })
    }

    private suspend fun getAverageRating(userId: String): String? {
        // Reference to the ratings node in the database
        val ratingsRef = database.getReference(RATINGS)

        // Initialize variables to store the sum of ratings and the count
        var totalRating = 0.0
        var ratingCount = 0

        // Fetch the ratings for the specified user
        val ratingsSnapshot = ratingsRef.get().await()

        // Iterate through all ratings
        for (ratingSnapshot in ratingsSnapshot.children) {
            val ratedUserId = ratingSnapshot.child(RATED_USER_ID).getValue(String::class.java)

            // Check if the rating is for the specified user
            if (ratedUserId == userId) {
                val score = ratingSnapshot.child(SCORE).getValue(Double::class.java) ?: 0.0
                totalRating += score
                ratingCount++
            }
        }

        // Calculate the average rating
        val averageRating = if (ratingCount > 0) {
            totalRating / ratingCount
        } else {
            return null // Return null if there are no ratings
        }

        // Return the formatted average rating
        return averageRating.let {
            if (it % 1.0 == 0.0) {
                // Return only the integer part if there is no decimal
                it.toInt().toString()
            } else {
                // Return the full decimal value
                it.toString()
            }
        }
    }


    override suspend fun followUnfollowUser(
        userToFollowOrUnfollow: User,
        onSuccess: () -> Unit,
        onFailure: (Int) -> Unit
    ) {
        val userToFollowUid = userToFollowOrUnfollow.userId
        val followersRef = database.getReference("$FOLLOWERS/$userToFollowUid")
        val followingRef = database.getReference("$FOLLOWING/$currentUserUid")

        try {
            // Use withContext to switch to IO dispatcher for Firebase operations
            val followingSnapshot = withContext(Dispatchers.IO) {
                followingRef.get().await()
            }
            val followersSnapshot = withContext(Dispatchers.IO) {
                followersRef.get().await()
            }

            val isFollowing = followingSnapshot.children.any { it.value == userToFollowUid }

            val updates = mutableMapOf<String, Any?>()

            if (isFollowing) {
                // Unfollow: Remove entries from both 'following' and 'followers'
                followingSnapshot.children.firstOrNull { it.value == userToFollowUid }?.key?.let {
                    updates["$FOLLOWING/$currentUserUid/$it"] = null
                }
                followersSnapshot.children.firstOrNull { it.value == currentUserUid }?.key?.let {
                    updates["$FOLLOWERS/$userToFollowUid/$it"] = null
                }
            } else {
                // Follow: Add entries to both 'following' and 'followers'
                updates["$FOLLOWING/$currentUserUid/${followingRef.push().key}"] = userToFollowUid
                updates["$FOLLOWERS/$userToFollowUid/${followersRef.push().key}"] = currentUserUid
            }

            // Perform the updates
            withContext(Dispatchers.IO) {
                database.reference.updateChildren(updates).await()
            }

            withContext(Dispatchers.Main) {
                onSuccess()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onFailure(R.string.error_follow_unfollow)
            }
        }
    }


    override fun getUserFollowing(
        onSuccess: (Map<String, String>?) -> Unit,
        onFailure: (Int) -> Unit,
    ) {
        getCurrentUserId()?.let {
            database.reference.child(FOLLOWING).child(it)
        }?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val followingList = dataSnapshot.getValue<Map<String, String>>()
                onSuccess(followingList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                onFailure(R.string.error_loading_following_list)
            }
        })
    }

    override suspend fun getUsers(): StateFlow<List<LocalUser>> {
        return _userListFlow
    }

    override fun getCurrentUserId(): String? {
        return currentUserUid
    }


}
