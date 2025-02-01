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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
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

    private val _feedUiData =
        MutableStateFlow(FeedUiData(emptySet(), emptyList()))
    val feedUiData: StateFlow<FeedUiData> = _feedUiData.asStateFlow()


    init {
        loadFeed()
        listenToFeedPosts()
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
                                _feedUiData.update { currentState ->
                                    currentState.copy(followingUserIds = (result.data + userId).toSet())
                                }
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


    /**
     * Listens to feed posts and updates the UI state accordingly.
     *
     * This function observes changes in the followed user IDs and fetches the posts for those users.
     * It also checks the like status of each post and updates the feed UI data.
     * The UI state is updated based on the presence of posts.
     */
    private fun listenToFeedPosts() {
        viewModelScope.launch {
            _feedUiData.map { it.followingUserIds }
                .distinctUntilChanged() // Ensure only distinct changes trigger updates
                .filter { it.isNotEmpty() } // Ensure we don't fetch with empty userIds
                .flatMapLatest { userIds ->
                    postRepository.getPostsForUsers(userIds)
                }
                .map { posts ->
                    // Get post IDs and check like status
                    val postIds = posts.map { it.id }.toSet()
                    val likedPostIds = postRepository.checkPostListLikeStatus(
                        userId = userRepository.getCurrentUserId(),
                        postIds = postIds
                    )

                    // Update feed UI data
                    val updatedPosts = posts.map { post ->
                        FeedPostWithLikeStatus(
                            post = post,
                            isLiked = likedPostIds.filter { it.key == post.id }
                                .map { it.value }.firstOrNull() ?: false
                        )
                    }

                    // Update feed UI data
                    _feedUiData.update { currentState ->
                        currentState.copy(
                            postList = updatedPosts
                        )
                    }

                    // Determine UI state
                    if (updatedPosts.isNotEmpty()) FeedUiState.NonEmptyFeed
                    else FeedUiState.EmptyFeed
                }
                .catch { error ->
                    FeedUiState.Error.StringError(error.message ?: "Unknown error")
                }
                .collect { state ->
                    _uiState.value = state
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
