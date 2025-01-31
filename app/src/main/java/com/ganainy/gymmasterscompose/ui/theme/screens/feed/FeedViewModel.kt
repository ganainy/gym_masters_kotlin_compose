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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
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
        MutableStateFlow(FeedUiData(emptySet(), emptyList(),))
    val feedUiData: StateFlow<FeedUiData> = _feedUiData.asStateFlow()


    // Represents the IDs of users whose posts we want to show
    private val followedUsers = MutableStateFlow<Set<String>>(emptySet())



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
                                followedUsers.value = (result.data + userId).toSet()
                            }
                            is ResultWrapper.Error -> {
                                _uiState.value = FeedUiState.Error.IntError(R.string.error_loading_feed)
                            }
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = FeedUiState.Error.IntError(R.string.error_loading_feed)
            }
        }
    }


    private fun listenToFeedPosts() {
        viewModelScope.launch {
            followedUsers
                .flatMapLatest { userIds ->
                    postRepository.getPostsForUsers(userIds)
                }
                .transform<List<FeedPost>, FeedUiState> { posts ->
                    // Get post IDs and check like status
                    val postIds = posts.map { it.id }.toSet()
                    val likedPostIds = postRepository.checkPostsLikeStatus(
                        userId = userRepository.getCurrentUserId(),
                        postIds = postIds
                    )

                    // Emit the combined result
                    emit(
                        if (posts.isNotEmpty()) {
                            _feedUiData.update { currentState ->
                                currentState.copy(
                                    postList = posts.map { post ->
                                        FeedPostWithLikeStatus(
                                            post = post,
                                            isLiked = likedPostIds.contains(post.id)
                                        )
                                    }
                                )
                            }
                            FeedUiState.NonEmptyFeed
                        } else {
                            FeedUiState.EmptyFeed
                        }
                    )
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
                postRepository.togglePostReaction(userId=null,postId = postId)
            }.onFailure {
                _uiState.value = FeedUiState.Error.IntError(R.string.error_updating_reaction)
            }
        }

    }



    fun refreshFeed() {
        _uiState.value = FeedUiState.Loading
        loadFeed()
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
