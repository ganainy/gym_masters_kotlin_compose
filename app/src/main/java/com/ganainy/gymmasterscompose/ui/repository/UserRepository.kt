package com.ganainy.gymmasterscompose.ui.repository

import UserDisplayInfo
import android.net.Uri
import android.util.Log
import com.ganainy.gymmasterscompose.Constants.ID
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.models.User
import com.ganainy.gymmasterscompose.ui.models.User.Companion.PROFILE_PICTURE_URL
import com.ganainy.gymmasterscompose.ui.models.User.Companion.USERS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.User.Companion.USER_IMAGES
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost.Companion.POSTS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost.Companion.POST_CREATOR
import com.ganainy.gymmasterscompose.utils.CustomException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.core.net.toUri
import com.google.firebase.firestore.FieldValue


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
    private val firestore: FirebaseFirestore,
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
     */
    override fun getCurrentUserId(): String =
        _currentUser.value?.uid ?: throw Exception("User auth not found")

    // --- getUser (One-time read) ---
    override suspend fun getUser(userId: String?): ResultWrapper<User> {
        // Determine the effective user ID
        val effectiveUserId = userId ?: try {
            getCurrentUserId()
        } catch (e: AuthRepository.UserNotAuthenticatedException) {
            return ResultWrapper.Error(e) // Return error if not logged in and no ID provided
        }

        return withContext(Dispatchers.IO) { // Keep IO dispatcher for network/DB access
            try {
                // Reference to the user document in Firestore
                val userDocRef = firestore.collection(USERS_COLLECTION).document(effectiveUserId)

                // Retrieve the user document snapshot
                val snapshot = userDocRef.get().await()

                // Get the user details from the snapshot
                if (snapshot.exists()) {
                    val userDetails = snapshot.toObject(User::class.java)
                    if (userDetails != null) {
                        ResultWrapper.Success(userDetails)
                    } else {
                        Log.e("UserRepository", "Failed to convert Firestore document ${snapshot.id} to User object.")
                        ResultWrapper.Error(Exception("Failed to parse user details for ID: $effectiveUserId"))
                    }
                } else {
                    ResultWrapper.Error(Exception("User details not found for ID: $effectiveUserId"))
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Error fetching user $effectiveUserId", e)
                ResultWrapper.Error(e) // Return failure result with the exception
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

    // --- getUserFlow (Real-time updates) ---
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getUserFlow(userId: String?): Flow<ResultWrapper<User>> = callbackFlow {
        val effectiveUserId = userId ?: try {
            getCurrentUserId()
        } catch (e: AuthRepository.UserNotAuthenticatedException) {
            trySend(ResultWrapper.Error(e))
            close(e) // Close flow on error
            return@callbackFlow
        }

        // Reference to the user document
        val userDocRef = firestore.collection(USERS_COLLECTION).document(effectiveUserId)
        var listenerRegistration: ListenerRegistration? = null

        try {
            // Listener to handle document changes and errors
            listenerRegistration = userDocRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ResultWrapper.Error(error))
                    Log.e("UserRepository", "Error listening to user $effectiveUserId", error)
                    // Consider closing the flow here depending on error type: close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = try { snapshot.toObject(User::class.java) } catch (e: Exception) { null }
                    if (user != null) {
                        trySend(ResultWrapper.Success(user))
                    } else {
                        Log.e("UserRepository", "Failed converting user snapshot ${snapshot.id}")
                        trySend(ResultWrapper.Error(CustomException(R.string.error_parsing_user_data)))
                    }
                } else {
                    // Document doesn't exist or snapshot is null
                    trySend(ResultWrapper.Error(CustomException(R.string.user_not_found)))
                }
            }
        } catch (e: Exception) {
            // Catch exceptions during listener setup
            trySend(ResultWrapper.Error(e))
            close(e)
        }

        // Remove the listener when the flow is closed
        awaitClose {
            Log.d("UserRepository", "Removing listener for user $effectiveUserId")
            listenerRegistration?.remove()
        }
    }

    // --- getUserPosts (Real-time updates) ---
    override suspend fun getUserPosts(userId: String): Flow<ResultWrapper<List<FeedPost>>> = callbackFlow {
        val postsCollection = firestore.collection(POSTS_COLLECTION)
        var listenerRegistration: ListenerRegistration? = null

        try {
            // Query posts where the nested creator ID matches
            val query = postsCollection.whereEqualTo("$POST_CREATOR.id", userId)
                // Add ordering by creation date
                .orderBy(FeedPost.CREATED_AT, Query.Direction.DESCENDING)

            listenerRegistration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ResultWrapper.Error(error))
                    Log.e("UserRepository", "Error listening to posts for user $userId", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { doc ->
                        try { doc.toObject(FeedPost::class.java) } catch (e: Exception) {
                            Log.e("UserRepository", "Failed converting post snapshot ${doc.id}", e)
                            null
                        }
                    }
                    trySend(ResultWrapper.Success(posts))
                } else {
                    // Snapshot is null, might indicate an issue or simply no data
                    trySend(ResultWrapper.Success(emptyList()))
                }
            }
        } catch (e: Exception) {
            trySend(ResultWrapper.Error(e))
            close(e)
        }

        awaitClose {
            Log.d("UserRepository", "Removing listener for posts of user $userId")
            listenerRegistration?.remove()
        }
    }


    override suspend fun updateUser(updates: Map<String, Any>): ResultWrapper<Unit> {
        val uid = try { getCurrentUserId() } catch (e: Exception) {
            return ResultWrapper.Error(AuthRepository.UserNotAuthenticatedException("User is not authenticated"))
        }

        return try {
            val userDocRef = firestore.collection(USERS_COLLECTION).document(uid)
            userDocRef.update(updates).await()
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user $uid", e)
            ResultWrapper.Error(e)
        }
    }

    override suspend fun updateUserProfileImage(imagePath: String): ResultWrapper<String> {
        val uid = try { getCurrentUserId() } catch (e: Exception) {
            return ResultWrapper.Error(AuthRepository.UserNotAuthenticatedException("User is not authenticated"))
        }

        // Path in Firebase Storage (no change needed here)
        val userImageStorageRef = storage.reference
            .child(USER_IMAGES).child(uid + "_" + imagePath.substringAfterLast("/")) // Include UID for uniqueness

        return try {
            // Upload file to Storage (no change needed here)
            userImageStorageRef.putFile(imagePath.toUri()).await()
            val downloadUrl = userImageStorageRef.downloadUrl.await().toString()

            try {
                // --- Update Firestore ---
                val userDocRef = firestore.collection(USERS_COLLECTION).document(uid)
                // Update only the profilePictureUrl field
                userDocRef.update(PROFILE_PICTURE_URL, downloadUrl).await()
                ResultWrapper.Success(downloadUrl)
            } catch (dbError: Exception) {
                // If Firestore update fails, delete the uploaded file from Storage
                try {
                    Log.w("UserRepository", "Firestore update failed for profile pic, attempting to delete Storage file.")
                    userImageStorageRef.delete().await()
                } catch (deleteError: Exception) {
                    Log.e("UserRepository", "Failed to delete profile pic from Storage after DB error", deleteError)
                }
                throw dbError // Re-throw the database error
            }
        } catch (e: Exception) {
            // Catch exceptions from Storage upload OR the re-thrown DB error
            Log.e("UserRepository", "Error updating profile image for user $uid", e)
            ResultWrapper.Error(e)
        }
    }


    override fun getUserDisplayInfo(): UserDisplayInfo {
        val currentUser = _currentUser.value
        return if (currentUser != null) {
            UserDisplayInfo(
                displayName = currentUser.displayName ?: "Unknown",
                profileImageUrl = currentUser.photoUrl?.toString() ?: ""
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
        val uid = try { getCurrentUserId() } catch (e: Exception) { return } // Fail silently if not logged in

        try {
            val userDocRef = firestore.collection(USERS_COLLECTION).document(uid)
            // Use Firestore server timestamp for accuracy and consistency
            userDocRef.update(User.LAST_ACTIVE, FieldValue.serverTimestamp()).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to update last active time for user $uid", e)
        }
    }


}