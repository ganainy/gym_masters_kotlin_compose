package com.ganainy.gymmasterscompose.ui.theme.repository

import com.ganainy.gymmasterscompose.AppConstants.CREATOR_ID
import com.ganainy.gymmasterscompose.AppConstants.EXERCISES
import com.ganainy.gymmasterscompose.AppConstants.FOLLOWERS_UID
import com.ganainy.gymmasterscompose.AppConstants.FOLLOWING_UID
import com.ganainy.gymmasterscompose.AppConstants.ID
import com.ganainy.gymmasterscompose.AppConstants.USERS
import com.ganainy.gymmasterscompose.AppConstants.WORKOUTS
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.CustomException
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

interface IDataRepository {
    suspend fun createUser(email: String, password: String, name: String): Result<User>
    fun getLoggedUser(listener: (Result<User>) -> Unit)
    fun loadUsers(onSuccess: (List<User>) -> Unit, onFailure: (Int) -> Unit)
    fun setExercisesCount(onSuccess: (List<User>) -> Unit, onFailure: (Int) -> Unit)
    fun setWorkoutsCount(onSuccess: (List<User>) -> Unit, onFailure: (Int) -> Unit)
    suspend fun followUnfollowUser(
        userToFollowOrUnfollow: User, onSuccess: (List<User>) -> Unit
    )

    fun getCurrentUserId(): String?
}

class DataRepository(
    private val authRepo: AuthRepository,
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : IDataRepository {

    // firebase Id of current logged in user
    private val currentUserUid: String? = authRepo.getCurrentUserId()

    // List to store all users (except logged user)
    val users = mutableListOf<User>()


    override suspend fun createUser(
        email: String,
        password: String,
        name: String
    ): Result<User> {
        return try {
            // First, create the user using Firebase authentication
            val result = authRepo.createUser(email, password)

            // If the creation was successful, map the result to a User object
            result.fold(
                onSuccess = { uid: String ->
                    try {
                        // Create a User object
                        val user = User(id = uid, name = name, email = email)

                        // Save the User object to the database
                        val userRef = database.getReference(USERS).child(user.id!!)
                        userRef.setValue(user).await()

                        // Return the success result with the User object
                        Result.success(user)
                    } catch (e: Exception) {
                        // Handle any exception that occurs while saving the user to the database
                        Result.failure<User>(e)
                    }
                },
                onFailure = { createException ->
                    // If there was a failure in creating the user, return a failure result
                    Result.failure(createException)
                }
            )
        } catch (e: Exception) {
            // Handle any unexpected exceptions
            Result.failure(e)
        }
    }


    override fun getLoggedUser(listener: (Result<User>) -> Unit) {

        try {
            // Set up a real-time listener to continuously listen for changes to the logged user data
            val userRef = database.getReference(USERS).child(authRepo.getCurrentUserId()!!)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
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


    override fun loadUsers(
        onSuccess: (List<User>) -> Unit,
        onFailure: (Int) -> Unit
    ) {
        database.getReference(USERS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    for (ds in dataSnapshot.children) {
                        val userId = ds.child(ID).getValue(String::class.java)
                        if (userId != authRepo.getCurrentUserId()) {

                            // Deserialize the User object from Firebase
                            val user = ds.getValue(User::class.java)

                            // Let Kotlin handle null safety and add the user if not already present
                            user?.let {
                                // Check if the user is already in the list before adding
                                if (!users.contains(user)) {
                                    users.add(user)
                                }
                            }
                        }
                    }
                    onSuccess(users) // Pass the list of users to the success callback
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure(R.string.error_loading_data) // Pass the error message to the failure callback
                }
            })
    }

    // Helper function to set count for each user (workouts or exercises)
    private fun setCount(
        reference: String,
        countField: (User) -> Int?,
        updateCount: (User, Int) -> Unit,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Int) -> Unit
    ) {
        if (users.isEmpty()) return

        var processedUsers = 0
        val totalUsers = users.size

        for (user in users) {
            val database: DatabaseReference = database.getReference(reference)

            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var count = countField(user) ?: 0
                        for (itemSnapshot in snapshot.children) {
                            val creatorId = itemSnapshot.child(CREATOR_ID)
                            if (creatorId.exists() && creatorId.value == user.id) {
                                count++
                            }
                        }
                        updateCount(user, count)
                    }
                    // Increment the processed users count
                    processedUsers++

                    // If all users have been processed, call onSuccess
                    if (processedUsers == totalUsers) {
                        onSuccess(users)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onFailure(R.string.error_loading_data) // Generic error message
                }
            })
        }
    }

    override fun setExercisesCount(onSuccess: (List<User>) -> Unit, onFailure: (Int) -> Unit) {
        setCount(
            reference = EXERCISES,
            countField = { it.exercisesCount },
            updateCount = { user, count -> user.exercisesCount = count },
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    override fun setWorkoutsCount(onSuccess: (List<User>) -> Unit, onFailure: (Int) -> Unit) {
        setCount(
            reference = WORKOUTS,
            countField = { it.workoutsCount },
            updateCount = { user, count -> user.workoutsCount = count },
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }


    override suspend fun followUnfollowUser(
        userToFollowOrUnfollow: User,
        onSuccess: (List<User>) -> Unit
    ) {
        val userToFollowUid = userToFollowOrUnfollow.id
        val followingUIDs = mutableMapOf<String, String>()
        val followersUIDs = mutableMapOf<String, String>()

        runBlocking {
            try {
                // First read operation
                val dataSnapshot =
                    database.getReference("$USERS/$currentUserUid/$FOLLOWING_UID").get().await()
                if (dataSnapshot.exists()) {
                    // Retrieve the list of following UIDs
                    followingUIDs.clear()
                    for (childSnapshot in dataSnapshot.children) {
                        val value_uid = childSnapshot.getValue(String::class.java)
                        val key_uid = childSnapshot.key
                        followingUIDs[key_uid!!] = value_uid!!
                    }
                }

                val dataSnapshot2 =
                    database.getReference("$USERS/$userToFollowUid/$FOLLOWERS_UID").get().await()
                if (dataSnapshot2.exists()) {
                    // Retrieve the list of followers UIDs
                    followersUIDs.clear()
                    for (childSnapshot in dataSnapshot2.children) {
                        val value_uid = childSnapshot.getValue(String::class.java)
                        val key_uid = childSnapshot.key
                        followersUIDs[key_uid!!] = value_uid!!
                    }
                }

                if (followingUIDs.values.contains(userToFollowUid)) {
                    // Unfollow: Remove the following entry and the corresponding follower entry
                    removeByValue(followingUIDs, userToFollowUid!!)
                    database.getReference("$USERS/$currentUserUid/$FOLLOWING_UID")
                        .setValue(followingUIDs)

                    removeByValue(followersUIDs, currentUserUid!!)
                    database.getReference("$USERS/$userToFollowUid/$FOLLOWERS_UID")
                        .setValue(followersUIDs)

                    // remove logged user from the followers
                    users.find { it.id == userToFollowUid }?.followersUID=followersUIDs
                    onSuccess(users)

                } else {
                    // Follow: Add the new following entry and the corresponding follower entry
                    database.getReference("$USERS/$currentUserUid/$FOLLOWING_UID")
                        .push()
                        .setValue(
                            userToFollowUid
                        )

                    database.getReference("$USERS/$userToFollowUid/$FOLLOWERS_UID")
                        .push()
                        .setValue(
                            currentUserUid
                        )

                    // add logged user to the followers
                    users.find { it.id == userToFollowUid }?.followersUID=followersUIDs.plus(currentUserUid to currentUserUid) as MutableMap<String, String>
                    onSuccess(users)
                }

            } catch (e: Exception) {
                println("Error during operations: ${e.message}")
            }
        }
    }

    //extension function for the map to remove by value instead of key
    fun removeByValue(map: MutableMap<String, String>, valueToRemove: String) {
        val entriesToRemove = map.entries.filter { it.value == valueToRemove }
        entriesToRemove.forEach { map.remove(it.key) }
    }

    override fun getCurrentUserId(): String? {
        return currentUserUid
    }


}
