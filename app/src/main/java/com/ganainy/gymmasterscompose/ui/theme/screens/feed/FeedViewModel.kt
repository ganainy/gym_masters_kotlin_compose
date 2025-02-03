package com.ganainy.gymmasterscompose.ui.theme.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.repository.IAuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IPostRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ISocialRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
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
    val postList: List<FeedPostWithLikeStatus>,
    var lastLoadedPostTimestamp: Long? = null // used for pagination, first most recent 20 posts are loaded then on load more older posts are loaded

)

data class FeedPostWithLikeStatus(
    val post: FeedPost,
    val isLiked: Boolean = false
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val socialRepository: ISocialRepository,
    private val userRepository: IUserRepository,
    private val postRepository: IPostRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _feedUiData = MutableStateFlow(FeedUiData(emptySet(), emptyList()))
    val feedUiData: StateFlow<FeedUiData> = _feedUiData.asStateFlow()

    init {
        listenToFeedPosts()
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            try {
                val userId = userRepository.getCurrentUserId()

                // Load following users
                socialRepository.getUserFollowing(userId)
                    .onStart {
                        _uiState.value = FeedUiState.Loading
                    }
                    .catch { e ->
                        _uiState.value = FeedUiState.Error.IntError(R.string.error_loading_feed)
                    }
                    .collect { result ->
                        when (result) {
                            is ResultWrapper.Success -> {
                                // Update followed users including current user
                                val updatedFollowingIds = (result.data + userId).toSet()
                                _feedUiData.update { currentState ->
                                    currentState.copy(followingUserIds = updatedFollowingIds)
                                }

                                // Immediately fetch posts for the updated following list
                                fetchPostsForUsers(updatedFollowingIds)
                            }

                            is ResultWrapper.Error -> {
                                _uiState.value =
                                    FeedUiState.Error.IntError(R.string.error_loading_feed)
                            }
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = FeedUiState.Error.IntError(R.string.error_loading_feed)
            }
        }
    }

    private suspend fun fetchPostsForUsers(userIds: Set<String>) {
        try {
            val posts = postRepository.getPostsOfCertainUsers(
                userIds,
                lastPostTimestamp = null
            ).first()
            val postIds = posts.map { it.id }.toSet()
            val likedPostIds = postRepository.checkPostListLikeStatus(
                userId = userRepository.getCurrentUserId(),
                postIds = postIds
            ).first()

            val updatedPosts = posts.map { post ->
                FeedPostWithLikeStatus(
                    post = post,
                    isLiked = likedPostIds[post.id] ?: false
                )
            }

            _feedUiData.update { currentState ->
                currentState.copy(postList = updatedPosts,
                    lastLoadedPostTimestamp = posts.lastOrNull()?.createdAt)
            }

            _uiState.value = if (updatedPosts.isNotEmpty()) {
                FeedUiState.NonEmptyFeed
            } else {
                FeedUiState.EmptyFeed
            }
        } catch (error: Exception) {
            _uiState.value = FeedUiState.Error.StringError(error.message ?: "Unknown error")
        }
    }

    private fun listenToFeedPosts() {
        viewModelScope.launch {
            _feedUiData
                .map { it.followingUserIds }
                .distinctUntilChanged()
                .filter { it.isNotEmpty() }
                .collect { userIds ->
                    fetchPostsForUsers(userIds)
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


    /**
     * Loads more posts for the feed by combining the like status and the posts of the following users.
     * updates lastLoadedPostTimestamp to be the timestamp of the last post in the new posts
     * Updates the feed UI data with the new posts and their like status.
     */
    fun loadMorePosts() {
        viewModelScope.launch {
            combine(
                postRepository.checkPostListLikeStatus(
                    userId = userRepository.getCurrentUserId(),
                    postIds = _feedUiData.value.postList.map { it.post.id }.toSet()
                ),
                postRepository.getPostsOfCertainUsers(
                    userIds = _feedUiData.value.followingUserIds,
                    lastPostTimestamp = _feedUiData.value.lastLoadedPostTimestamp
                )
            ) { likeMap: Map<String, Boolean>, posts: List<FeedPost> ->
                // Update the feed UI data with the new posts and their like status
                _feedUiData.update { currentState ->
                    val updatedPosts = posts.map { post ->
                        FeedPostWithLikeStatus(
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
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            when (val result = authRepository.signOut()) {
                is ResultWrapper.Success -> {
                    onSignedOut()
                    _uiState.value = FeedUiState.EmptyFeed
                }

                is ResultWrapper.Error -> {
                    _uiState.value = FeedUiState.Error.IntError(R.string.error_signing_out)
                }
            }
        }
    }

}
