package com.ganainy.gymmasterscompose.ui.theme.repository

import Profile
import Stats
import User
import com.ganainy.gymmasterscompose.AppConstants.USERS
import com.ganainy.gymmasterscompose.ui.theme.Utils.generateRandomId
import com.ganainy.gymmasterscompose.ui.theme.Utils.generateRandomUsername
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject


interface IAuthRepository {
    suspend fun signInUser(email: String, password: String): Result<FirebaseUser>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUserId(): String
    suspend fun createUserAuth(email: String, password: String): Result<FirebaseUser>
    suspend fun createUserProfile(
        email: String,
        displayName: String,
    ): Result<User>

    abstract fun isAlreadySignedIn(): Boolean
}


class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
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
        return auth.currentUser?.uid ?: throw UserNotAuthenticatedException("User is not authenticated")
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
    ): Result<User> {

               return try {
                    // Create Profile and Stats objects
                    val profile = Profile(
                        username = generateRandomUsername(),
                        email = email,
                        displayName = displayName,
                        joinDate = Date().time,
                        id = generateRandomId(),
                        profilePictureUrl = null,
                        bio = null,
                        lastActive = null
                    )

                    val user = User(
                        profile = profile,
                        stats = Stats(
                            postCount = 0,
                            workoutCount = 0,
                            exerciseCount = 0,
                            followersCount = 0,
                            followingCount = 0
                        )
                    )

                    // Save the User object to the database
                    val userRef = database.getReference(USERS).child(user.profile.id)
                    userRef.setValue(user).await()

                    // Return the success result with the User object
                    Result.success(user)
                } catch (e: Exception) {
                    // Handle any exception that occurs while saving the user to the database
                    Result.failure<User>(e)
                }

    }

    override fun isAlreadySignedIn(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun signInUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.let { firebaseUser ->
                Result.success(firebaseUser)
            } ?: Result.failure(Exception("Sign in successful but user is null"))
        } catch (e: Exception) {
            when (e) {
                is FirebaseAuthInvalidUserException -> Result.failure(Exception("com.ganainy.gymmasterscompose.ui.theme.models.User not found"))
                is FirebaseAuthInvalidCredentialsException -> Result.failure(Exception("Invalid credentials"))
                else -> Result.failure(e)
            }
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}