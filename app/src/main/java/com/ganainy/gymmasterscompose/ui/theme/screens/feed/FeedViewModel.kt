package com.ganainy.gymmasterscompose.ui.theme.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.PostStats
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.repository.IAuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IPostRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ISocialRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IUsersRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ResultWrapper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class FeedUiState {
    object EmptyFeed : FeedUiState()
    object NonEmptyFeed : FeedUiState()
    object Loading : FeedUiState()
    data class Error(val messageStringResource: Int) : FeedUiState()
}


data class FeedUiData(
    val followingUserIds: Set<String>,
    val postList: List<FeedPost>,
    val postAuthorList: List<User>,
    val postStatsList: List<PostStats>,
)


@HiltViewModel
class FeedViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val authRepository: IAuthRepository,
    private val socialRepository: ISocialRepository,
    private val userRepository: IUserRepository,
    private val usersRepository: IUsersRepository,
    private val postRepository: IPostRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _feedUiData =
        MutableStateFlow(FeedUiData(emptySet(), emptyList(), mutableListOf(), mutableListOf()))
    val feedUiData: StateFlow<FeedUiData> = _feedUiData.asStateFlow()

    private val currentUserId: String?
        get() = auth.currentUser?.uid


    init {
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            currentUserId?.let { userId ->
                socialRepository.getUserFollowing(userId)
                    .catch { e ->
                        _uiState.value = FeedUiState.Error(R.string.error_loading_feed)
                    }
                    .collect { result ->
                        when (result) {
                            is ResultWrapper.Error -> {
                                _uiState.value = FeedUiState.Error(R.string.error_loading_feed)
                            }

                            is ResultWrapper.Success -> {
                                _feedUiData.update { uiData ->
                                    uiData.copy(
                                        followingUserIds = result.data.toMutableSet().apply {
                                            currentUserId?.let { add(it) }
                                        }
                                    )
                                }
                                listenToFeedPosts()
                            }
                        }
                    }
            }
        }
    }

    private fun listenToFeedPosts() {
        viewModelScope.launch {
            val usersToGetPostsFrom=_feedUiData.value.followingUserIds+currentUserId
            usersRepository.listenToPostsByUsers(usersToGetPostsFrom)
                .catch { e ->
                    _uiState.value = FeedUiState.Error(R.string.error_loading_posts)

                }
                .collect { result ->
                    when (result) {
                        is ResultWrapper.Error -> {
                            _uiState.value = FeedUiState.Error(R.string.error_loading_posts)
                        }

                        is ResultWrapper.Success -> {

                            if (result.data.isNotEmpty()) {
                                _uiState.value = FeedUiState.NonEmptyFeed
                                result.data.forEach { post ->
                                    enrichPost(post.authorId, post.id)
                                }

                                _feedUiData.update {
                                    it.copy(
                                        postList = (it.postList + result.data).distinctBy { post -> post.id }
                                    )
                                }
                            } else {
                                _uiState.value = FeedUiState.EmptyFeed
                            }
                        }
                    }
                }
        }
    }

    private suspend fun enrichPost(authorId: String, postId: String) {
        coroutineScope {
            val authorDeferred = async {
                userRepository.getUser(authorId).firstOrNull() // Collects first emitted value
            }
            val statsDeferred = async {
                postRepository.getPostStats(postId).firstOrNull() // Collects first emitted value
            }

            val authorResult = authorDeferred.await()
            val statsResult = statsDeferred.await()

            if (authorResult != null ) {
                _feedUiData.update {
                    it.copy(
                        postAuthorList = (authorResult as? ResultWrapper.Success<User>)?.data
                            ?.let { user -> it.postAuthorList + user } ?: it.postAuthorList,

                    )
                }
            }
            if (statsResult != null) {
                _feedUiData.update {
                    it.copy(
                        postStatsList = (statsResult as? ResultWrapper.Success<PostStats>)?.data
                            ?.let { stats -> it.postStatsList + stats } ?: it.postStatsList
                    )
                }
            }
        }
    }



    fun toggleReaction(postId: String) {
        viewModelScope.launch {
            runCatching {
                postRepository.togglePostReaction(postId = postId, reactionType = "LIKE")
            }.onFailure {
                _uiState.value = FeedUiState.Error(R.string.error_updating_reaction)
            }
        }

    }

    fun isPostLikedByCurrentUser(postId: String): Boolean {
        return (_feedUiData.value.postStatsList.firstOrNull { it.postId == postId }?.likes ?: 0) > 0
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
                    _uiState.value = FeedUiState.Error(R.string.error_signing_out)
                }
            }
        }
    }

}
