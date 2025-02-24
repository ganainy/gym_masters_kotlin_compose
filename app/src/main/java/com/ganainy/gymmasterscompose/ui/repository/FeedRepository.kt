package com.ganainy.gymmasterscompose.ui.repository

import com.ganainy.gymmasterscompose.ui.models.Follow
import com.ganainy.gymmasterscompose.ui.models.Follow.Companion.FOLLOWER_ID
import com.ganainy.gymmasterscompose.ui.models.Follow.Companion.FOLLOWS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost.Companion.POSTS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.post.PostByUserEntry.Companion.POSTS_BY_USER_COLLECTION
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import kotlinx.coroutines.tasks.await

interface IFeedRepository {
    suspend fun getPaginatedFeed(lastPostId: String? = null): ResultWrapper<List<FeedPost>>
}

class FeedRepository(
    private val database: FirebaseDatabase,
    private val hashtagRepository: IHashtagRepository,
    private val userRepository: IUserRepository,  // Add this to get current user ID
) : IFeedRepository {
    companion object {
        private const val PAGE_SIZE = 10
    }

    private val currentUserUid = userRepository.getCurrentUserId()

    override suspend fun getPaginatedFeed(lastPostId: String?): ResultWrapper<List<FeedPost>> {
        return try {
            // Get the list of users ids the current user is following (including themselves)
            val followedUsersIds = getFollowedUsersIds()

            if (followedUsersIds.isEmpty()) {
                return ResultWrapper.Success(emptyList())
            }

            // Query posts from all followed users
            val posts = fetchPostsFromUsers(followedUsersIds, lastPostId)

            ResultWrapper.Success(posts)
        } catch (e: Exception) {
            ResultWrapper.Error(Exception("Error fetching feed: ${e.message}"))
        }
    }

    private suspend fun getFollowedUsersIds(): List<String> {
        val followedUsers = mutableListOf(currentUserUid) // Include self

        try {
            // Query follows where current user is the follower
            val snapshot = database.reference
                .child(FOLLOWS_COLLECTION)
                .orderByChild(FOLLOWER_ID)
                .equalTo(currentUserUid)
                .get()
                .await()

            snapshot.children.forEach { followSnapshot ->
                val follow = followSnapshot.getValue(Follow::class.java)
                follow?.followedId?.let { followedUserId ->
                    followedUsers.add(followedUserId)
                }
            }
        } catch (e: Exception) {
            println("Error fetching followed users: ${e.message}")
        }

        return followedUsers
    }

    private suspend fun fetchPostsFromUsers(userIds: List<String>, lastPostId: String?): List<FeedPost> {
        val posts = mutableListOf<FeedPost>()

        userIds.forEach { userId ->
            try {
                var query: Query = database.reference
                    .child(POSTS_BY_USER_COLLECTION)
                    .child(userId)

                if (lastPostId != null) {
                    query = query.orderByKey().startAfter(lastPostId)
                }

                query = query.limitToFirst(PAGE_SIZE)

                val snapshot = query.get().await()

                snapshot.children.forEach { postSnapshot ->
                    val postId = postSnapshot.key
                    if (postId != null) {
                        val post = fetchPostById(postId)
                        post?.let { posts.add(it) }
                    }
                }
            } catch (e: Exception) {
                println("Error fetching posts for user $userId: ${e.message}")
            }
        }

        return posts.sortedByDescending { it.createdAt }
            .take(PAGE_SIZE)
    }

    private suspend fun fetchPostById(postId: String): FeedPost? {
        return try {
            val snapshot = database.reference
                .child(POSTS_COLLECTION)
                .child(postId)
                .get()
                .await()

            snapshot.getValue(FeedPost::class.java)
        } catch (e: Exception) {
            println("Error fetching post $postId: ${e.message}")
            null
        }
    }
}

