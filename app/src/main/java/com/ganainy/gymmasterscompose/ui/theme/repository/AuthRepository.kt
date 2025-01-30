package com.ganainy.gymmasterscompose.ui.theme.repository

import com.ganainy.gymmasterscompose.Constants
import com.ganainy.gymmasterscompose.Constants.USERS
import com.ganainy.gymmasterscompose.di.IoDispatcher
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.room.AppDatabase
import com.ganainy.gymmasterscompose.utils.Utils.generateRandomId
import com.ganainy.gymmasterscompose.utils.Utils.generateRandomUsername
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject


interface IAuthRepository {
    suspend fun signInUser(email: String, password: String): ResultWrapper<FirebaseUser>
    suspend fun signOut(): ResultWrapper<Unit>
    fun getCurrentUserId(): String
    suspend fun createUserAuth(email: String, password: String): Result<FirebaseUser>
    suspend fun createUserProfile(
        email: String,
        displayName: String,
    ): ResultWrapper<User>

    abstract fun isAlreadySignedIn(): Boolean
    suspend fun getCurrentUser(): ResultWrapper<User>
}


class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val appDatabase: AppDatabase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : IAuthRepository {


    /*  override suspend fun saveUserInfo(user: User): Result<Unit> {
          return try {
              val userRef = database.getReference(AppConstants.USERS).child(user.profile.id)
              userRef.setValue(user).await()
              Result.success(Unit)
          } catch (e: Exception) {
              Result.failure(e)
          }
      }*/

    override fun getCurrentUserId(): String {
        return auth.currentUser?.uid
            ?: throw UserNotAuthenticatedException("User is not authenticated")
    }

    // Custom Exception
    class UserNotAuthenticatedException(message: String) : Exception(message)

    override suspend fun createUserAuth(
        email: String, password: String
    ): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.let { firebaseUser ->
                Result.success(firebaseUser)
            } ?: Result.failure(Exception("User creation successful but user is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Updated createUser function
    override suspend fun createUserProfile(
        email: String, displayName: String
    ): ResultWrapper<User> {

        return try {

            val user = User(
                username = generateRandomUsername(),
                email = email,
                displayName = displayName,
                joinDate = Date().time,
                id = generateRandomId(Constants.USER),
                profilePictureUrl = null,
                bio = null,
                lastActive = null,
            )

            // Save the User object to the database
            val userRef = database.getReference(USERS).child(user.id)
            userRef.setValue(user).await()

            // Return the success result with the User object
            ResultWrapper.Success(user)
        } catch (e: Exception) {
            // Handle any exception that occurs while saving the user to the database
            ResultWrapper.Error(e)
        }

    }

    override fun isAlreadySignedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Retrieves the current user, using a caching approach with Room.
     *
     * This method first attempts to fetch the user from the local database (cache).
     * If the user is not found in the cache, it fetches the user from the remote database (Firebase),
     * caches it locally, and then returns the user.
     * If an error occurs during the process, it returns the cached user if available, otherwise returns an error.
     *
     * @return [ResultWrapper] containing the current [User] or an error.
     */
    override suspend fun getCurrentUser(): ResultWrapper<User> = withContext(ioDispatcher) {
        try {
            // Get the current user ID
            val userId = getCurrentUserId()

            // Attempt to fetch the user from the local database (cache)
            val cachedUser = appDatabase.userDao().getUserById(userId)

            // If the user is found in the cache, return it
            if (cachedUser != null) {
                return@withContext ResultWrapper.Success(cachedUser)
            }

            // Fetch the user from the remote database (Firebase)
            val user =
                database.getReference(USERS).child(userId).get().await().getValue(User::class.java)

            // If the user is found in the remote database, cache it locally and return it
            if (user != null) {
                appDatabase.userDao().insert(user)
                return@withContext ResultWrapper.Success(user)
            } else {
                // If the user is not found in the remote database, return an error
                return@withContext ResultWrapper.Error(Exception("User not found"))
            }
        } catch (e: Exception) {
            // If an error occurs, attempt to fetch the user from the local database (cache)
            val cachedUser = appDatabase.userDao().getUserById(getCurrentUserId())

            // If the user is found in the cache, return it
            if (cachedUser != null) {
                ResultWrapper.Success(cachedUser)
            } else {
                // If the user is not found in the cache, return the error
                ResultWrapper.Error(e)
            }
        }
    }

    override suspend fun signInUser(email: String, password: String): ResultWrapper<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.let { firebaseUser ->
                ResultWrapper.Success(firebaseUser)
            } ?: ResultWrapper.Error(Exception("Sign in successful but user is null"))
        } catch (e: Exception) {
            when (e) {
                is FirebaseAuthInvalidUserException -> ResultWrapper.Error(Exception("com.ganainy.gymmasterscompose.ui.theme.models.User not found"))
                is FirebaseAuthInvalidCredentialsException -> ResultWrapper.Error(Exception("Invalid credentials"))
                else -> ResultWrapper.Error(e)
            }
        }
    }

    override suspend fun signOut(): ResultWrapper<Unit> {
        return try {
            auth.signOut()
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

}