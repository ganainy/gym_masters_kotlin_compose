package com.ganainy.gymmasterscompose.ui.theme.repository

import com.ganainy.gymmasterscompose.AppConstants.CREATOR_ID
import com.ganainy.gymmasterscompose.AppConstants.EXERCISES
import com.ganainy.gymmasterscompose.AppConstants.ID
import com.ganainy.gymmasterscompose.AppConstants.USERS
import com.ganainy.gymmasterscompose.AppConstants.WORKOUTS
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

interface IDataRepository {
    fun getCurrentUser(): FirebaseUser?
    fun loadUsers(onSuccess: (List<User>) -> Unit, onFailure: (Int) -> Unit)
    fun setExercisesCount(onSuccess: (List<User>) -> Unit, onFailure: (Int) -> Unit)
    fun setWorkoutsCount(onSuccess: (List<User>) -> Unit, onFailure: (Int) -> Unit)
}

class DataRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : IDataRepository {
    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // List to store all users (except logged user)
    val users = mutableListOf<User>()

    override fun loadUsers(
        onSuccess: (List<User>) -> Unit,
        onFailure: (Int) -> Unit
    ) {
        database.getReference(USERS).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val loggedUserId = getCurrentUser()?.uid

                for (ds in dataSnapshot.children) {
                    val userId = ds.child(ID).getValue(String::class.java)
                    if (userId != loggedUserId) {

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

}
