package com.ganainy.gymmasterscompose.ui.screens.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.repository.ILikeRepository
import com.ganainy.gymmasterscompose.ui.repository.IPostRepository
import com.ganainy.gymmasterscompose.ui.repository.ISocialRepository
import com.ganainy.gymmasterscompose.ui.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.room.LikeType
import com.ganainy.gymmasterscompose.ui.screens.discover.DiscoverData
import com.ganainy.gymmasterscompose.ui.screens.post_details.FeedPostWithLikesAndComments
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FeedUiState {
    data object EmptyFeed : FeedUiState()
    data class NonEmptyFeed(val postList: List<FeedPostWithLikesAndComments>) : FeedUiState()
    data object Loading : FeedUiState()
    sealed class Error : FeedUiState() {
        data class IntError(val messageStringResource: Int) : Error()
        data class StringError(val message: String) : Error()
    }

}


data class FeedUiData(
    val postIds: Set<String> = emptySet(),
    val followingUserIds: Set<String> = emptySet(),
    var lastLoadedPostTimestamp: Long? = null // used for pagination, first most recent 20 posts are
    // loaded then on load more older posts are loaded
)


@HiltViewModel
class FeedViewModel @Inject constructor(
    private val socialRepository: ISocialRepository,
    private val userRepository: IUserRepository,
    private val postRepository: IPostRepository,
    private val likeRepository: ILikeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _feedUiData = MutableStateFlow(FeedUiData(emptySet()))
    val feedUiData: StateFlow<FeedUiData> = _feedUiData.asStateFlow()

    init {
        loadFeed()

        // Update the feed UI data with the post IDs of the feed.
        _uiState.asStateFlow()
            .map { uiState ->
                if (uiState is FeedUiState.NonEmptyFeed) {
                    _feedUiData.update { currentState ->
                        currentState.copy(
                            postIds = uiState.postList.map { it.post.id }.toSet()
                        )
                    }
                }
                uiState
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                DiscoverData()
            )
    }

    /**
     * Loads the feed of the current user.
     *
     * It first starts observing the followed users in real-time, and for each new list of followed users,
     * it fetches the posts of those users and updates the feed UI data.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadFeed() {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUserId()

                socialRepository.observeFollowedUsers(currentUserId)
                    .onStart {
                        _uiState.value = FeedUiState.Loading
                    }
                    .catch { e ->
                        _uiState.value = FeedUiState.Error.StringError(e.message ?: "Unknown error")
                    }
                    .flatMapLatest { followingIds ->
                        val updatedFollowingIds = (followingIds + currentUserId).toSet()
                        _feedUiData.update { currentState ->
                            currentState.copy(followingUserIds = updatedFollowingIds)
                        }

                        if (updatedFollowingIds.isEmpty()) {
                            flowOf(emptyList<FeedPostWithLikesAndComments>())
                        } else {
                            observePostsOfUsers(updatedFollowingIds)
                                .flatMapLatest { posts ->
                                    if (posts.isEmpty()) {
                                        flowOf(emptyList<FeedPostWithLikesAndComments>())
                                    } else {
                                        combine(
                                            posts.map { feedPostWithLikesAndComments ->
                                                likeRepository.observeLikeStatus(
                                                    targetId = feedPostWithLikesAndComments.post.id,
                                                    type = LikeType.POST
                                                ).map { isLiked ->
                                                    FeedPostWithLikesAndComments(
                                                        post = feedPostWithLikesAndComments.post,
                                                        isLiked = isLiked
                                                    )
                                                }
                                            }
                                        ) { it.toList() }
                                    }
                                }
                        }
                    }
                    .collect { posts ->
                        _feedUiData.update { currentState ->
                            _uiState.value = FeedUiState.NonEmptyFeed(posts)
                            currentState.copy(
                                lastLoadedPostTimestamp = posts.lastOrNull()?.post?.createdAt
                            )
                        }
                        _uiState.value = if (posts.isNotEmpty()) {
                            FeedUiState.NonEmptyFeed(posts)
                        } else {
                            FeedUiState.EmptyFeed
                        }
                    }

            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error loading feed", e)
                _uiState.value = FeedUiState.Error.StringError(e.message ?: "Unknown error")
            }
        }
    }


    /**
     * Observes the posts of the specified users and updates the feed UI data.
     *
     * @param userIds The set of user IDs whose posts are to be observed.
     * @return Flow of posts with their like status
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observePostsOfUsers(userIds: Set<String>): Flow<List<FeedPostWithLikesAndComments>> {
        return postRepository.observePostsOfUsers(userIds, lastPostTimestamp = null)
            // Handle errors from post repository
            .catch { error ->
                _uiState.value = FeedUiState.Error.StringError(error.message ?: "Unknown error")
                emit(emptyList()) // Emit empty list to continue the flow
            }
            // Process posts and their like status
            .flatMapLatest { posts: List<FeedPost> ->
                if (posts.isEmpty()) {
                    // Return empty list flow for empty posts
                    // WHY: Ensures type consistency with non-empty case
                    flowOf(emptyList<FeedPostWithLikesAndComments>())
                } else {
                    // Create flows for each post's like status
                    val postLikeFlows = posts.map { post ->
                        likeRepository.observeLikeStatus(
                            targetId = post.id,
                            type = LikeType.POST
                        ).map { isLiked ->
                            FeedPostWithLikesAndComments(
                                post = post,
                                isLiked = isLiked
                            )
                        }
                    }

                    // Combine all like status flows
                    // WHY: Simplifies flow combination and ensures type safety
                    combine(postLikeFlows) { it.toList() }
                }
            }
            // Handle errors in like status processing
            .catch { error ->
                _uiState.value = FeedUiState.Error.StringError(error.message ?: "Unknown error")
                emit(emptyList()) // Emit empty list to continue the flow
            }
            // Update UI state with collected posts
            .onEach { updatedPosts ->
                _uiState.value = FeedUiState.NonEmptyFeed(updatedPosts)
                _feedUiData.update { currentState ->
                    currentState.copy(
                        lastLoadedPostTimestamp = updatedPosts.lastOrNull()?.post?.createdAt
                    )
                }
            }
    }

    fun toggleReaction(postId: String) {
        viewModelScope.launch {
            runCatching {
                likeRepository.toggleLike(
                    userId = userRepository.getCurrentUserId(),
                    targetId = postId,
                    type = LikeType.POST
                )
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
                        postIds = _feedUiData.value.postIds
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
                        _uiState.value = FeedUiState.NonEmptyFeed(updatedPosts)
                        currentState.copy(
                            lastLoadedPostTimestamp = posts.lastOrNull()?.createdAt
                        )
                    }
                }.collect { }
            } catch (e: Exception) {
                _uiState.value = FeedUiState.Error.StringError(e.message ?: "Unknown error")
            }
        }
    }


}
