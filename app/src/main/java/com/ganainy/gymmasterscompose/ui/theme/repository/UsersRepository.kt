package com.ganainy.gymmasterscompose.ui.theme.repository


import Profile
import Stats
import User
import com.ganainy.gymmasterscompose.AppConstants.FOLLOWERS
import com.ganainy.gymmasterscompose.AppConstants.FOLLOWING
import com.ganainy.gymmasterscompose.AppConstants.USERS
import com.ganainy.gymmasterscompose.ui.theme.models.FeedPost
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
    fun listenForUsersUpdates(): Flow<Result<List<User>>>
    fun listenForFollowersUpdates(): Flow<Result<Unit>>
    fun getUserFollowing(): Flow<Result<List<String>>>
    fun listenToPostsByUsers(userIds: Set<String>): Flow<Result<List<FeedPost>>>
}

class UsersRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : IUsersRepository {
    private val currentUserId= auth.currentUser?.uid
    private val userList = mutableListOf<User>()
    private val _userListFlow = MutableStateFlow<List<User>>(emptyList())

    override fun listenForUsersUpdates(): Flow<Result<List<User>>> = callbackFlow {
        val usersRef = database.getReference(USERS)

        val listener = usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val tempUserList = mutableListOf<User>()

                        snapshot.children.forEach { userSnapshot ->
                            val userId = userSnapshot.key ?: return@forEach
                            if (userId == currentUserId) return@forEach

                            val profileSnapshot = userSnapshot.child("profile")
                            val statsSnapshot = userSnapshot.child("stats")

                            val profile = profileSnapshot.getValue(Profile::class.java) ?: return@forEach
                            val stats = statsSnapshot.getValue(Stats::class.java) ?: Stats()

                            val user = User(profile = profile, stats = stats)
                            if (userList.none { it.profile.id == user.profile.id }) {
                                tempUserList.add(user)
                            }
                        }
                        userList.addAll(tempUserList)
                        _userListFlow.value = userList
                        trySend(Result.success(userList))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        })

        awaitClose { usersRef.removeEventListener(listener) }
    }

    override fun listenForFollowersUpdates(): Flow<Result<Unit>> = callbackFlow {
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
                            localUser.stats.followersCount = followersCountMap[localUser.profile.id] ?: 0
                        }
                        _userListFlow.value = userList
                        trySend(Result.success(Unit))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        })

        awaitClose { followersRef.removeEventListener(listener) }
    }

    override fun getUserFollowing(): Flow<Result<List<String>>> = callbackFlow {
        val followingRef = currentUserId?.let {
            database.reference.child(FOLLOWING).child(it)
        } ?: return@callbackFlow

        val listener = followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val followingList = snapshot.children.mapNotNull { it.key }
                trySend(Result.success(followingList))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure<List<String>>(error.toException()))
            }
        })

        awaitClose { followingRef.removeEventListener(listener) }
    }

    override fun listenToPostsByUsers(userIds: Set<String>): Flow<Result<List<FeedPost>>> = callbackFlow {
        val postsRef = database.getReference("posts")

        val listener = postsRef
            .orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val posts = snapshot.children.mapNotNull { postSnapshot ->
                            val post = postSnapshot.getValue(FeedPost::class.java)
                            if (post?.authorId in userIds) post else null
                        }
                        trySend(Result.success(posts))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Result.failure(error.toException()))
                }
            })

        awaitClose { postsRef.removeEventListener(listener) }
    }
}

