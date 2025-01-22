package com.ganainy.gymmasterscompose.repository

import User
import com.ganainy.gymmasterscompose.ui.theme.repository.IAuthRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class FakeAuthRepository : IAuthRepository {
    override suspend fun createUser(email: String, password: String): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun saveUserInfo(user: User): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun getCurrentUserId(): String {
        return "FakeUserId"
    }

    override suspend fun signInUser(email: String, password: String): Result<FirebaseUser> {
        TODO("Not yet implemented")
    }

    override suspend fun signOut(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getDatabase(): FirebaseDatabase {
        TODO("Not yet implemented")
    }
}