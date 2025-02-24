package com.ganainy.gymmasterscompose.ui.repository


import com.ganainy.gymmasterscompose.Constants.FOLLOWING
import com.ganainy.gymmasterscompose.ui.models.Follow
import com.ganainy.gymmasterscompose.ui.models.Follow.Companion.FOLLOWER_ID
import com.ganainy.gymmasterscompose.ui.models.Follow.Companion.FOLLOWS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.User
import com.ganainy.gymmasterscompose.ui.models.User.Companion.USERS_COLLECTION
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
    fun listenForFollowingUpdates(userId: String?): Flow<ResultWrapper<List<Follow>>>
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
        val usersRef = database.getReference(USERS_COLLECTION)

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

    /**
     * Listens for updates to the following list of a user.
     *
     * This function sets up a listener on the Firebase database to monitor changes to the list of users
     * that the specified user is following. It returns a Flow that emits a ResultWrapper containing a list
     * of Follow objects representing the users being followed or an error if the operation fails.
     *
     * @param userId The ID of the user whose following list is to be monitored. If null, the current user's ID is used.
     * @return A Flow emitting a ResultWrapper containing a list of Follow objects or an error.
     */
    override fun listenForFollowingUpdates(userId: String?): Flow<ResultWrapper<
            List<Follow>>> = callbackFlow {
        val userId = userId ?: currentUserId ?: return@callbackFlow
        val followingRef = database.getReference(FOLLOWS_COLLECTION).orderByChild(FOLLOWER_ID).equalTo(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val followingList = snapshot.children.mapNotNull { postSnapshot ->
                    postSnapshot.getValue(Follow::class.java)
                }
                trySend(ResultWrapper.Success(followingList))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(ResultWrapper.Error(error.toException()))
            }
        }

        followingRef.addValueEventListener(listener)
        awaitClose { followingRef.removeEventListener(listener) }
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
        val userRef = database.getReference(USERS_COLLECTION)

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

