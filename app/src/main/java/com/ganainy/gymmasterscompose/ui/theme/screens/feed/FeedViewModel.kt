package com.ganainy.gymmasterscompose.ui.theme.screens.feed

import Comment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.repository.IAuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ICommentsRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IPostRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ISocialRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FeedUiState {
    object EmptyFeed : FeedUiState()
    object NonEmptyFeed : FeedUiState()
    object Loading : FeedUiState()
    sealed class Error : FeedUiState() {
        data class IntError(val messageStringResource: Int) : Error()
        data class StringError(val message: String) : Error()
    }
}


data class FeedUiData(
    val followingUserIds: Set<String>,
    val postList: List<FeedPostWithLikesAndComments>,
    var lastLoadedPostTimestamp: Long? = null // used for pagination, first most recent 20 posts are loaded then on load more older posts are loaded

)

data class FeedPostWithLikesAndComments(
    val post: FeedPost,
    val isLiked: Boolean = false, // used to show like icon as filled or empty based on if user liked the post or not
    val commentList: List<Comment> = emptyList(),
    val showCommentSection: Boolean = false // used to show comment section when user clicks on comment icon
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val socialRepository: ISocialRepository,
    private val userRepository: IUserRepository,
    private val postRepository: IPostRepository,
    private val commentsRepository: ICommentsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _feedUiData = MutableStateFlow(FeedUiData(emptySet(), emptyList()))
    val feedUiData: StateFlow<FeedUiData> = _feedUiData.asStateFlow()

    init {
        loadFeed()
    }

    /**
     * Loads the feed of the current user.
     *
     * It first starts observing the followed users in real-time, and for each new list of followed users,
     * it fetches the posts of those users and updates the feed UI data.
     */
    private fun loadFeed() {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUserId()

                // Start observing the followed users in real-time.
                // When the list of followed users changes, fetch the posts of the new list of users.
                socialRepository.observeFollowedUsers(currentUserId)
                    // Show the loading state while the data is being fetched.
                    .onStart {
                        _uiState.value = FeedUiState.Loading
                    }
                    // If any error occurs while fetching the list of followed users,
                    // show an error state with the given error message.
                    .catch { e ->
                        _uiState.value = FeedUiState.Error.IntError(R.string.error_loading_feed)
                    }
                    // For each new list of followed users,
                    // fetch the posts of those users and update the feed UI data.
                    .collect { followingIds ->
                        // Include the current user in the list of followed users,
                        // since the feed should include the current user's posts.
                        val updatedFollowingIds = (followingIds + currentUserId).toSet()
                        _feedUiData.update { currentState ->
                            currentState.copy(followingUserIds = updatedFollowingIds)
                        }
                        // Fetch the posts of the new list of followed users.
                        observePostsOfUsers(updatedFollowingIds)
                    }
            } catch (e: Exception) {
                // If any error occurs while fetching the posts,
                // show an error state with the given error message.
                _uiState.value = FeedUiState.Error.IntError(R.string.error_loading_feed)
            }
        }
    }


    /**
     * Fetches posts for the given set of user IDs and updates the feed UI data.
     * It first fetches the posts and then combines the likes and comments for those posts.
     * If any error occurs, it updates the UI state to an error state.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observePostsOfUsers(userIds: Set<String>) {
        // Fetch posts for the given set of user IDs
        postRepository.observePostsOfUsers(userIds, lastPostTimestamp = null)
            // Catch any error that occurs during the fetching of posts
            .catch { error ->
                _uiState.value = FeedUiState.Error.StringError(error.message ?: "Unknown error")
            }
            // If no posts, emit empty list immediately
            .flatMapLatest { posts ->
                if (posts.isEmpty()) {
                    flow { emit(emptyList<FeedPostWithLikesAndComments>()) }
                } else {
                    // Get the IDs of the posts
                    val postIds = posts.map { it.id }.toSet()

                    // Combine the likes and comments flows
                    combine(
                        // Fetch the likes for the posts
                        postRepository.observePostsLikes(postIds, userId = null),
                        // Fetch the comments for the posts
                        commentsRepository.observeCommentsForPosts(postIds)
                    ) { likedPostIds, commentsMap ->
                        // Combine the posts, likes and comments into a single list
                        posts.map { post ->
                            FeedPostWithLikesAndComments(
                                post = post,
                                isLiked = likedPostIds[post.id] ?: false,
                                commentList = commentsMap[post.id] ?: emptyList()
                            )
                        }
                    }
                }
            }
            // Catch any error that occurs during the fetching of likes and comments
            .catch { error ->
                _uiState.value = FeedUiState.Error.StringError(error.message ?: "Unknown error")
            }
            // Update the feed UI data and the UI state
            .collect { updatedPosts ->
                _feedUiData.update { currentState ->
                    currentState.copy(
                        postList = updatedPosts,
                        lastLoadedPostTimestamp = updatedPosts.lastOrNull()?.post?.createdAt
                    )
                }

                _uiState.value = if (updatedPosts.isNotEmpty()) {
                    FeedUiState.NonEmptyFeed
                } else {
                    FeedUiState.EmptyFeed
                }
            }
    }

    /**
     * Listens to changes in the feed posts by observing the following user IDs.
     * When the following user IDs change, it fetches posts for the updated list of users.
     */
    private fun observeFollowedUsers() {
        viewModelScope.launch {
            _feedUiData
                .map { it.followingUserIds }
                .distinctUntilChanged()
                .filter { it.isNotEmpty() }
                .collect { userIds ->
                    observePostsOfUsers(userIds)
                }
        }
    }

    fun toggleReaction(postId: String) {
        viewModelScope.launch {
            runCatching {
                postRepository.togglePostReaction(userId = null, postId = postId)
            }.onFailure {
                _uiState.value = FeedUiState.Error.IntError(R.string.error_updating_reaction)
            }
        }

    }


    fun refreshFeed() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading

            // Temporarily clear followingUserIds to force refresh
            _feedUiData.update { it.copy(followingUserIds = emptySet()) }

            // Reload feed
            loadFeed()
        }
    }


    //TODO: fix this
    /**
     * Loads more posts for the feed by combining the like status and the posts of the following users.
     * updates lastLoadedPostTimestamp to be the timestamp of the last post in the new posts
     * Updates the feed UI data with the new posts and their like status.
     */
    fun loadMorePosts() {
        viewModelScope.launch {
            try {
                combine(
                    postRepository.observePostsLikes(
                        userId = userRepository.getCurrentUserId(),
                        postIds = _feedUiData.value.postList.map { it.post.id }.toSet()
                    ),
                    postRepository.observePostsOfUsers(
                        userIds = _feedUiData.value.followingUserIds,
                        lastPostTimestamp = _feedUiData.value.lastLoadedPostTimestamp
                    )
                ) { likeMap: Map<String, Boolean>, posts: List<FeedPost> ->
                    // Update the feed UI data with the new posts and their like status
                    _feedUiData.update { currentState ->
                        val updatedPosts = posts.map { post ->
                            FeedPostWithLikesAndComments(
                                post = post,
                                isLiked = likeMap[post.id] ?: false
                            )
                        }
                        currentState.copy(
                            postList = updatedPosts,
                            lastLoadedPostTimestamp = posts.lastOrNull()?.createdAt
                        )
                    }
                }.collect { }
            } catch (e: Exception) {
                _uiState.value = FeedUiState.Error.StringError(e.message ?: "Unknown error")
            }
        }
    }

    fun openComments(postId: String) {
        _feedUiData.update { currentState ->
            val updatedPostList = currentState.postList.map { postWithLikesAndComments ->
                if (postWithLikesAndComments.post.id == postId) {
                    postWithLikesAndComments.copy(showCommentSection = !postWithLikesAndComments.showCommentSection)
                } else {
                    postWithLikesAndComments
                }
            }
            currentState.copy(postList = updatedPostList)
        }
    }


    fun addComment(commentContent: String, postId: String) {
        viewModelScope.launch {
            try {
                commentsRepository.addComment(postId= postId, content = commentContent)
            } catch (e: Exception) {
                _uiState.value = FeedUiState.Error.StringError(e.message ?: "Unknown error")
            }
        }
    }

}
