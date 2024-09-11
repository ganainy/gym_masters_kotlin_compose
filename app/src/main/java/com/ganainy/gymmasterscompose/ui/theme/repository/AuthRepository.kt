package com.ganainy.gymmasterscompose.ui.theme.repository

import com.ganainy.gymmasterscompose.AppConstants
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await


interface IAuthRepository {
    suspend fun createUser(email: String, password: String): Result<String>
    suspend fun saveUserInfo(user: User): Result<Unit>
    fun getCurrentUser(): FirebaseUser?
    suspend fun signInUser(email: String, password: String): Result<FirebaseUser>
    suspend fun signOut(): Result<Unit>
    suspend fun getDatabase(): FirebaseDatabase
}


class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : IAuthRepository {

    override suspend fun createUser(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(
                result.user?.uid
                    ?: throw Exception("com.ganainy.gymmasterscompose.ui.theme.models.User ID is null")
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDatabase(): FirebaseDatabase {
        return database
    }

    override suspend fun saveUserInfo(user: User): Result<Unit> {
        return try {
            val userRef = database.getReference(AppConstants.USERS).child(user.id!!)
            userRef.setValue(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
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