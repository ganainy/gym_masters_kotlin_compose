package com.ganainy.gymmasterscompose.ui.theme.repository

import UserDisplayInfo
import android.net.Uri
import android.util.Log
import com.ganainy.gymmasterscompose.Constants.ID
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.models.User.Companion.PROFILE_PICTURE_URL
import com.ganainy.gymmasterscompose.ui.theme.models.User.Companion.USERS_COLLECTION
import com.ganainy.gymmasterscompose.ui.theme.models.User.Companion.USER_IMAGES
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost.Companion.POSTS_COLLECTION
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost.Companion.POST_CREATOR
import com.ganainy.gymmasterscompose.utils.CustomException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
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
    suspend fun getUserPosts(userId: String): Flow<ResultWrapper<List<FeedPost>>>
    suspend fun updateUser(updates: Map<String, Any>): ResultWrapper<Unit>
    fun getCurrentUserId(): String
    suspend fun getUserFlow(userId: String?): Flow<ResultWrapper<User>> // get notified when user changes
    suspend fun getUser(userId: String?): ResultWrapper<User> //read user only once, not notified if user changes
    suspend fun updateUserProfileImage(imagePath: String): ResultWrapper<String>
    abstract fun getUserDisplayInfo(): UserDisplayInfo
}


class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
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
    override fun getCurrentUserId(): String =
        _currentUser.value?.uid ?: throw Exception("User auth not found")

    /**
     * Retrieves the details of a user from the database based on the provided user ID.
     *
     * @param userId The ID of the user whose details are to be retrieved, if null, the currently authenticated user's details are retrieved.
     * @return A Result containing the User object if successful, or an exception if an error occurs.
     */
    override suspend fun getUser(userId: String?): ResultWrapper<User> {
        val userId = userId ?: getCurrentUserId()
        return withContext(Dispatchers.IO) {
            try {
                // Reference to the user in the database
                val userRef = database.getReference(USERS_COLLECTION).child(userId)
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
    override suspend fun getUserFlow(userId: String?): Flow<ResultWrapper<User>> = callbackFlow {
        val currentUserId = getCurrentUserId()
        // Determine the reference to the user in the database
        val userRef = database.getReference(USERS_COLLECTION).child(userId ?: currentUserId)

        // Listener to handle data changes and errors
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Attempt to retrieve the user object from the snapshot
                val user = runCatching {
                    snapshot.getValue(User::class.java)
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


            val postsRef = database.getReference(POSTS_COLLECTION)
                .orderByChild("$POST_CREATOR/$ID")
                .equalTo(userId)


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

            postsRef.addValueEventListener(listener)
            awaitClose { postsRef.removeEventListener(listener) }
        }


    override suspend fun updateUser(updates: Map<String, Any>): ResultWrapper<Unit> {
        val usersCollection = database.getReference(USERS_COLLECTION)
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

    /**
     * Updates the user's profile image by uploading a new image to Firebase Storage and updating the user's profile image URL in the database.
     *
     * @param imagePath The local file path of the new profile image.
     * @return A ResultWrapper containing the download URL of the uploaded image if successful, or an error if the operation fails.
     */

    override suspend fun updateUserProfileImage(imagePath: String): ResultWrapper<String> {
        val userImageStorageRef = storage.reference
            .child(USER_IMAGES).child(imagePath.substringAfterLast("/"))

        return try {
            // First upload the file and get the URL
            userImageStorageRef.putFile(Uri.parse(imagePath)).await()
            val downloadUrl = userImageStorageRef.downloadUrl.await().toString()

            try {
                // Try to update the database
                val userImageDatabaseRef = database
                    .getReference(USERS_COLLECTION)
                    .child(getCurrentUserId())
                    .child(PROFILE_PICTURE_URL)

                userImageDatabaseRef.setValue(downloadUrl).await()
                ResultWrapper.Success(downloadUrl)
            } catch (dbError: Exception) {
                // If database update fails, delete the uploaded file
                try {
                    userImageStorageRef.delete().await()
                } catch (deleteError: Exception) {
                    // Log the delete error but throw the original database error
                    Log.e("ProfileUpdate", "Failed to delete image after db error", deleteError)
                }
                throw dbError // Re-throw the database error to be caught by outer catch
            }
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }


    /**
     * Retrieves the display information of the current user.
     *
     * @return A UserDisplayInfo object containing the display name and profile image URL of the current user.
     *         If the current user is null, returns a UserDisplayInfo object with default values.
     */
    override fun getUserDisplayInfo(): UserDisplayInfo {
        val currentUser = _currentUser.value
        return if (currentUser != null) {
            UserDisplayInfo(
                displayName = currentUser.displayName ?: "Unknown",
                profileImageUrl = currentUser.photoUrl.toString()
            )
        } else {
            UserDisplayInfo()
        }
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
            val userRef = database.getReference(USERS_COLLECTION).child(uid)

            userRef.child("lastActive").setValue(System.currentTimeMillis()).await()
        }
    }


}