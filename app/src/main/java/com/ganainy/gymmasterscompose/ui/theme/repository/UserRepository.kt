package com.ganainy.gymmasterscompose.ui.theme.repository

import com.ganainy.gymmasterscompose.Constants
import com.ganainy.gymmasterscompose.Constants.ID
import com.ganainy.gymmasterscompose.Constants.USERS
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.CustomException
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject


// User profile and relationship management
interface IUserRepository {
    suspend fun createUser(email: String, password: String): ResultWrapper<String>
    suspend fun getUser(userId: String?): Flow<ResultWrapper<User>>
    suspend fun getUserPosts(userId: String): Flow<ResultWrapper<List<FeedPost>>>
    suspend fun updateUser(updates: Map<String, Any>): ResultWrapper<Unit>
    fun getCurrentUserId(): String
    suspend fun getUserDetails(userId: String?): ResultWrapper<User>
}


class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : IUserRepository {


    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)

    init {
        // Initialize current user
        _currentUser.value = auth.currentUser

        // Listen for auth changes
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

/**
     * Retrieves the current user's ID.
     *
     * @return The unique identifier of the current user.
     * @throws Exception if the user is not authenticated.
     */
    override fun getCurrentUserId(): String = _currentUser.value?.uid ?: throw Exception("User auth not found")

    /**
     * Retrieves the details of a user from the database based on the provided user ID.
     *
     * @param userId The ID of the user whose details are to be retrieved, if null, the currently authenticated user's details are retrieved.
     * @return A Result containing the User object if successful, or an exception if an error occurs.
     */
    override suspend fun getUserDetails(userId: String?): ResultWrapper<User> {
        val userId=userId?:getCurrentUserId()
        return withContext(Dispatchers.IO) {
            try {
                // Reference to the user in the database
                val userRef = database.getReference(USERS).child(userId)
                // Retrieve the user data snapshot
                val snapshot = userRef.get().await()
                // Get the user details from the snapshot
                val userDetails = snapshot.getValue(User::class.java)
                // Return success result with user details or throw an exception if not found
                ResultWrapper.Success(userDetails ?: throw Exception("User details not found"))
            } catch (e: Exception) {
                // Return failure result with the exception
                ResultWrapper.Error(e)
            }
        }
    }

    override suspend fun createUser(email: String, password: String): ResultWrapper<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            ResultWrapper.Success(
                result.user?.uid
                    ?: throw Exception("com.ganainy.gymmasterscompose.ui.theme.models.User ID is null")
            )
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    /**
     * Retrieves a user from the database based on the provided user ID.
     * If the user ID is null, it retrieves the currently authenticated user.
     *
     * @param userId The ID of the user to retrieve. If null, retrieves the currently authenticated user.
     * @return A Flow emitting ResultWrapper containing the User object or an error.
     */
    override suspend fun getUser(userId: String?): Flow<ResultWrapper<User>> = callbackFlow {
        // Determine the reference to the user in the database
        val userRef = if (userId == null) {
            database.getReference(USERS).orderByChild(ID).equalTo(auth.uid!!).limitToFirst(1)
        } else {
            database.getReference(Constants.USERS).child(userId)
        }

        // Listener to handle data changes and errors
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Attempt to retrieve the user object from the snapshot
                val user = runCatching {
                    snapshot.children.firstOrNull()?.getValue(User::class.java)
                        ?: snapshot.getValue(User::class.java)
                }.getOrNull()
                if (user != null) {
                    trySend(ResultWrapper.Success(user))
                } else {
                    trySend(ResultWrapper.Error(CustomException(R.string.user_not_found)))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(ResultWrapper.Error(error.toException()))
            }
        }

        // Add the listener to the user reference
        userRef.addValueEventListener(listener)
        // Remove the listener when the flow is closed
        awaitClose { userRef.removeEventListener(listener) }
    }

    override suspend fun getUserPosts(userId: String): Flow<ResultWrapper<List<FeedPost>>> =
        callbackFlow {
            val userRef = database.getReference("users").child(userId).child(Constants.POSTS)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val posts = snapshot.children.mapNotNull { postSnapshot ->
                        postSnapshot.getValue(FeedPost::class.java)
                    }
                    trySend(ResultWrapper.Success(posts))
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(ResultWrapper.Error(error.toException()))
                }
            }

            userRef.addValueEventListener(listener)
            awaitClose { userRef.removeEventListener(listener) }
        }


    override suspend fun updateUser(updates: Map<String, Any>): ResultWrapper<Unit> {
        val usersCollection = database.getReference(USERS)
        return auth.currentUser?.uid?.let { uid ->
            try {
                usersCollection.child(uid).updateChildren(updates).await()
                ResultWrapper.Success(Unit)
            } catch (e: Exception) {
                ResultWrapper.Error(e)
            }
        }
            ?: ResultWrapper.Error(AuthRepository.UserNotAuthenticatedException("User is not authenticated"))
    }




    /*todo
    // Update last active periodically
    viewModelScope.launch {
        while (true) {
            userRepository.updateUserLastActive()
            delay(5 * 60 * 1000) // Update every 5 minutes
        }
    }*/
    suspend fun updateUserLastActive() {
        auth.currentUser?.uid?.let { uid ->
            val userRef = database.getReference(Constants.USERS).child(uid)

            userRef.child("lastActive").setValue(System.currentTimeMillis()).await()
        }
    }


}