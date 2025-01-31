package com.ganainy.gymmasterscompose.ui.theme.repository


import com.ganainy.gymmasterscompose.Constants.FOLLOWERS
import com.ganainy.gymmasterscompose.Constants.FOLLOWING
import com.ganainy.gymmasterscompose.Constants.USERS
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IUsersRepository {
    fun listenForUsersUpdates(): Flow<ResultWrapper<List<User>>>
    fun listenForFollowersUpdates(): Flow<ResultWrapper<Unit>>
    fun getUserFollowing(): Flow<ResultWrapper<List<String>>>
    fun getAllUsers(): Flow<List<User>>
}

class UsersRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
) : IUsersRepository {
    private val currentUserId = auth.currentUser?.uid
    private val userList = mutableListOf<User>()
    private val _userListFlow = MutableStateFlow<List<User>>(emptyList())

    override fun listenForUsersUpdates(): Flow<ResultWrapper<List<User>>> = callbackFlow {
        val usersRef = database.getReference(USERS)

        val listener = usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val tempUserList = mutableListOf<User>()

                        snapshot.children.forEach { userSnapshot ->
                            val userId = userSnapshot.key ?: return@forEach
                            if (userId == currentUserId) return@forEach

                            val user = userSnapshot.getValue(User::class.java) ?: return@forEach

                            if (userList.none { it.id == user.id }) {
                                tempUserList.add(user)
                            }
                        }
                        userList.addAll(tempUserList)
                        _userListFlow.value = userList
                        trySend(ResultWrapper.Success(userList))
                    } catch (e: Exception) {
                        trySend(ResultWrapper.Error(e))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(ResultWrapper.Error(error.toException()))
            }
        })

        awaitClose { usersRef.removeEventListener(listener) }
    }

    override fun listenForFollowersUpdates(): Flow<ResultWrapper<Unit>> = callbackFlow {
        val followersRef = database.getReference(FOLLOWERS)

        val listener = followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val followersCountMap = snapshot.children.associate { followSnapshot ->
                            val userId = followSnapshot.key ?: return@associate null to 0
                            userId to (followSnapshot.childrenCount.toInt())
                        }.filterKeys { it != null }

                        userList.forEach { localUser ->
                            localUser.stats.followersCount = followersCountMap[localUser.id] ?: 0
                        }
                        _userListFlow.value = userList
                        trySend(ResultWrapper.Success(Unit))
                    } catch (e: Exception) {
                        trySend(ResultWrapper.Error(e))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(ResultWrapper.Error(error.toException()))
            }
        })

        awaitClose { followersRef.removeEventListener(listener) }
    }


    override fun getUserFollowing(): Flow<ResultWrapper<List<String>>> = callbackFlow {
        val followingRef = currentUserId?.let {
            database.reference.child(FOLLOWING).child(it)
        } ?: return@callbackFlow

        val listener = followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val followingList = snapshot.children.mapNotNull { it.key }
                trySend(ResultWrapper.Success(followingList))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(ResultWrapper.Error(error.toException()))
            }
        })

        awaitClose { followingRef.removeEventListener(listener) }
    }

    override fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val currentUserId: String? = auth.uid
        val userRef = database.getReference(USERS)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    userSnapshot.getValue(User::class.java)?.let { user ->
                        if (user.id == currentUserId) return@let // don't add the current user
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


}

