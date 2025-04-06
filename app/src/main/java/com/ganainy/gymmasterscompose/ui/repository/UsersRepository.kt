package com.ganainy.gymmasterscompose.ui.repository


import com.ganainy.gymmasterscompose.ui.models.Follow
import com.ganainy.gymmasterscompose.ui.models.Follow.Companion.FOLLOWER_ID
import com.ganainy.gymmasterscompose.ui.models.Follow.Companion.FOLLOWS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.User
import com.ganainy.gymmasterscompose.ui.models.User.Companion.USERS_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

interface IUsersRepository {
    fun listenForUsersUpdates(): Flow<ResultWrapper<List<User>>>
    fun listenForFollowingUpdates(userId: String?): Flow<ResultWrapper<List<Follow>>>
    fun getUserFollowingIdsFlow(): Flow<ResultWrapper<List<String>>>
    fun getAllUsers(): Flow<List<User>>
}

class UsersRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : IUsersRepository {
    private val currentUserId = auth.currentUser?.uid

    /**
     * Listens for updates to the users collection in Firestore.
     */
    override fun listenForUsersUpdates(): Flow<ResultWrapper<List<User>>> = callbackFlow {
        if (currentUserId == null) {
            trySend(ResultWrapper.Error(Exception("User not logged in")))
            close()
            return@callbackFlow
        }

        val usersCollection = firestore.collection(USERS_COLLECTION)
        val listenerRegistration = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(ResultWrapper.Error(error))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val userList = snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(User::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }.filter { it.id != currentUserId } // Filter out the current user
                trySend(ResultWrapper.Success(userList))
            } else {
                trySend(ResultWrapper.Success(emptyList()))
            }
        }

        // Unregister listener when flow is closed
        awaitClose { listenerRegistration.remove() }
    }

    override fun listenForFollowingUpdates(userId: String?): Flow<ResultWrapper<List<Follow>>> =
        callbackFlow {
            val effectiveUserId = userId ?: currentUserId

            if (effectiveUserId == null) {
                trySend(ResultWrapper.Error(Exception("User ID not available")))
                close()
                return@callbackFlow
            }

            val followsCollection = firestore.collection(FOLLOWS_COLLECTION)
            var listenerRegistration: ListenerRegistration? = null

            try {
                // Query where the followerId field matches the effectiveUserId
                val query = followsCollection.whereEqualTo(FOLLOWER_ID, effectiveUserId)

                listenerRegistration = query.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(ResultWrapper.Error(error))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val followingList = snapshot.documents.mapNotNull { document ->
                            try {
                                document.toObject(Follow::class.java)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(ResultWrapper.Success(followingList))
                    } else {
                        trySend(ResultWrapper.Success(emptyList()))
                    }
                }
            } catch (e: Exception) {
                trySend(ResultWrapper.Error(e))
                close(e)
            }
            awaitClose { listenerRegistration?.remove() }
        }


    override fun getUserFollowingIdsFlow(): Flow<ResultWrapper<List<String>>> = callbackFlow {
        val effectiveUserId = currentUserId ?: return@callbackFlow

        val followsCollection = firestore.collection(FOLLOWS_COLLECTION)
        val query = followsCollection.whereEqualTo(FOLLOWER_ID, effectiveUserId)

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(ResultWrapper.Error(error))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val followingList = snapshot.documents.mapNotNull { it.getString("followedId") }
                trySend(ResultWrapper.Success(followingList))
            } else {
                trySend(ResultWrapper.Success(emptyList()))
            }
        }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        val usersCollection = firestore.collection(USERS_COLLECTION)

        val listenerRegistration = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val users = snapshot.documents.mapNotNull { document ->
                    document.toObject(User::class.java)?.takeIf { it.id != currentUserId }
                }
                trySend(users)
            } else {
                trySend(emptyList())
            }
        }

        awaitClose { listenerRegistration.remove() }
    }

}

