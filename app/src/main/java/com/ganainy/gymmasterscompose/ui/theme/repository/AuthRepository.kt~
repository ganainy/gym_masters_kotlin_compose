package com.ganainy.gymmasterscompose.ui.theme.repository

import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.models.User.Companion.USERS
import com.ganainy.gymmasterscompose.utils.Utils.generateRandomUsername
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
    fun isUserLoggedIn(): Flow<Boolean>
}


class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
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
                id = auth.currentUser?.uid ?: throw Exception("User ID not found"),
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



    override fun isUserLoggedIn(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
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
        return withContext(Dispatchers.IO) {
             try {
                auth.signOut()
                ResultWrapper.Success(Unit)
            } catch (e: Exception) {
                ResultWrapper.Error(e)
            }
        }
    }

}