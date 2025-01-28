package com.ganainy.gymmasterscompose.ui.theme.repository

import User
import com.ganainy.gymmasterscompose.Constants.USERS
import com.ganainy.gymmasterscompose.Constants.USER_ID
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.CustomException
import com.ganainy.gymmasterscompose.ui.theme.models.FeedPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


// User profile and relationship management
interface IUserRepository {
    suspend fun createUser(email: String, password: String): ResultWrapper<String>
    suspend fun getLoggedUser(): Flow<ResultWrapper<User>>
    suspend fun getUser(userId: String): Flow<ResultWrapper<User>>
    suspend fun getUserPosts(userId: String): Flow<ResultWrapper<List<FeedPost>>>
    fun getAllUsers(): Flow<List<User>>
    suspend fun updateUser(user: User): ResultWrapper<Unit>
}


class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : IUserRepository {

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

    override suspend fun getUser(userId: String): Flow<ResultWrapper<User>> = callbackFlow {
        val userRef = database.getReference("users").child(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
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

        userRef.addValueEventListener(listener)
        awaitClose { userRef.removeEventListener(listener) }
    }

    override suspend fun getUserPosts(userId: String): Flow<ResultWrapper<List<FeedPost>>> = callbackFlow {
        val userRef = database.getReference("users").child(userId).child("posts")

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

    override fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val currentUserId:String?= auth.uid
        val userRef = database.getReference(USERS)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    userSnapshot.getValue(User::class.java)?.let { user ->
                        if (user.profile.id == currentUserId) return@let // don't add the current user
                        users.add(user)
                    }
                }
                trySend(users)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        userRef.addValueEventListener(listener)

        // Cleanup when Flow collection is cancelled
        awaitClose {
            userRef.removeEventListener(listener)
        }
    }

    override suspend fun updateUser(user: User): ResultWrapper<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getLoggedUser(): Flow<ResultWrapper<User>> = callbackFlow {
        try {
            // Set up a real-time listener to continuously listen for changes to the logged user data
            val userRef = database.getReference(USERS).orderByChild(USER_ID)
                .equalTo(auth.uid!!).limitToFirst(1)

            val snapshot = userRef.get().await()
            val user = snapshot.children.first().getValue<User>()
            if (user != null) {
                trySend(ResultWrapper.Success(user)).isSuccess
            } else {
                trySend(ResultWrapper.Error(CustomException(R.string.user_not_found))).isSuccess
            }
        } catch (e: Exception) {
            trySend(ResultWrapper.Error(CustomException(R.string.user_not_found))).isSuccess
        }
        awaitClose { }
    }


}