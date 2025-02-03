package com.ganainy.gymmasterscompose.repository

import Profile
import Stats
import User
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.repository.IDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.internal.NopCollector.emit


class FakeDataRepository: IDataRepository {
    override suspend fun createUser(
        email: String,
        password: String,
        name: String,
        joinDate: Long
    ): Result<User> {
        TODO("Not yet implemented")
    }

    override fun getLoggedUser(listener: (Result<User>) -> Unit) {
        listener.invoke(
            Result.success(
                User(
                    Profile(
                        id = "fake_user_id",
                        displayName = "",
                        username = "",
                        email = "",
                        joinDate = 0,
                        profilePictureUrl = null,
                        bio = null,
                        lastActive = null
                    ),
                    Stats(
                        postCount = null,
                        workoutCount = null,
                        exerciseCount = null,
                        followersCount = null,
                        followingCount = null
                    )
                )
            )
        )
    }

    override suspend fun followUnfollowUser(
        userToFollowOrUnfollow: User,
        onSuccess: () -> Unit,
        onFailure: (Int) -> Unit
    ) {
        onSuccess.invoke()
    }

    override fun getCurrentUserId(): String? {
        return "123456789"
    }

    override fun getUserFollowing(onSuccess: (List<String>?) -> Unit, onFailure: (Int) -> Unit) {
        onSuccess.invoke(listOf("user1", "user2"))
    }

    override suspend fun getUsers(): StateFlow<List<User>> {
        return MutableStateFlow(
            listOf(
                User(
                    Profile(
                        id = "user1",
                        displayName = "User One",
                        username = "user1",
                        email = "user1@example.com",
                        joinDate = 0
                    ),
                    Stats()
                ),
                User(
                    Profile(
                        id = "user2",
                        displayName = "User Two",
                        username = "user2",
                        email = "user2@example.com",
                        joinDate = 0
                    ),
                    Stats()
                )
            )
        )
    }


    override suspend fun listenForUsersUpdates(){
        // Simulate user updates
        val users = listOf(
            User(
                Profile(
                    id = "user1",
                    displayName = "User One",
                    username = "user1",
                    email = "user1@example.com",
                    joinDate = System.currentTimeMillis()
                ),
                Stats()
            ),
            User(
                Profile(
                    id = "user2",
                    displayName = "User Two",
                    username = "user2",
                    email = "user2@example.com",
                    joinDate = System.currentTimeMillis()
                ),
                Stats()
            )
        )
        // todo Emit updates to the StateFlow
    }
        override suspend fun listenForFollowersUpdates() {
        // Simulate follower updates
        val followers = listOf("user3", "user4")
        // Emit updates to the StateFlow
        emit(followers)
    }
    override suspend fun listenToPostsByUsers(userIds: Set<String>): Flow<List<FeedPost>> {
        return flow {
            emit(
                listOf(
                    FeedPost(
                        id = "post1",
                        creatorId = "fake_user_id",
                        content = "This is a test post",
                        linkedExerciseId = null,
                        linkedWorkoutId = null
                    )
                )
            )
        }
    }

    override suspend fun createPost(
        post: FeedPost,
        onSuccess: () -> Unit,
        onFailure: (Int) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun togglePostReaction(
        postId: String,
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Int) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getUserReactionForPost(postId: String, userId: String): String? {
        TODO("Not yet implemented")
    }

    override suspend fun getUser(userId: String): User? {
        TODO("Not yet implemented")
    }

    override suspend fun getExercise(exerciseId: String): Exercise? {
        TODO("Not yet implemented")
    }

    override suspend fun getWorkout(workoutId: String): Workout? {
        TODO("Not yet implemented")
    }
}