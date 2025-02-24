package com.ganainy.gymmasterscompose.ui.screens.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.repository.IFeedRepository
import com.ganainy.gymmasterscompose.ui.repository.ILikeRepository
import com.ganainy.gymmasterscompose.ui.repository.ISocialRepository
import com.ganainy.gymmasterscompose.ui.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.repository.ResultWrapper
import com.ganainy.gymmasterscompose.ui.repository.onError
import com.ganainy.gymmasterscompose.ui.room.LikeType
import com.ganainy.gymmasterscompose.ui.screens.post_details.FeedPostWithLikesAndComments
import com.ganainy.gymmasterscompose.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class FeedUiData(
    val postList: List<FeedPostWithLikesAndComments> = emptyList(),
    val followingUserIds: Set<String> = emptySet(),
    val lastLoadedPostTimestamp: Long? = null,// used for pagination, first most recent 10 posts are
    // loaded then on load more older posts are loaded
    val lastPostId: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasReachedEnd: Boolean = false,
    val error: UiText? = null
)


@HiltViewModel
class FeedViewModel @Inject constructor(
    private val socialRepository: ISocialRepository,
    private val userRepository: IUserRepository,
    private val feedRepository: IFeedRepository,
    private val likeRepository: ILikeRepository
) : ViewModel() {

    private val _feedUiData = MutableStateFlow(FeedUiData())
    val feedUiData: StateFlow<FeedUiData> = _feedUiData.asStateFlow()

    init {
        loadInitialFeed()
    }


    private fun loadInitialFeed() {
        viewModelScope.launch {
            // Prevent concurrent loads
            if (_feedUiData.value.isLoading || _feedUiData.value.isRefreshing) return@launch

            // Reset state for initial load
            _feedUiData.update {
                it.copy(
                    lastPostId = null,
                    lastLoadedPostTimestamp = null,
                    postList = emptyList(),
                    isLoading = true,
                    error = null
                )
            }

            try {
                    _feedUiData.update { it.copy(isLoading = true, error = null) }
                loadFeedPage(isInitialLoad = true)
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error loading initial feed", e)
                _feedUiData.update {
                    it.copy(
                        isLoading = false,
                        error = UiText.String(e.message ?: "Unknown error")
                    )
                }
            }
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {
            // Prevent concurrent loads or if no more pages
            if (_feedUiData.value.isLoading ||
                _feedUiData.value.isRefreshing ||
                _feedUiData.value.lastPostId == null ||
                _feedUiData.value.hasReachedEnd
            ) return@launch

            _feedUiData.update { it.copy( error = null) }

            try {
                loadFeedPage(isInitialLoad = false)
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error loading next page", e)
                _feedUiData.update {
                    it.copy(
                        error = UiText.String(e.message ?: "Unknown error")
                    )
                }
            }
        }
    }

    private suspend fun loadFeedPage(isInitialLoad: Boolean) {
        try {
            val currentUserId = userRepository.getCurrentUserId()

            when (val followingResult = socialRepository.getFollowedUsers(currentUserId)) {
                is ResultWrapper.Success -> {
                    val followingIds = followingResult.data
                    val updatedFollowingIds = (followingIds + currentUserId).toSet()
                    _feedUiData.update { it.copy(followingUserIds = updatedFollowingIds) }

                    if (updatedFollowingIds.isEmpty()) {
                        _feedUiData.update {
                            it.copy(
                                isLoading = false,
                                postList = emptyList(),
                                hasReachedEnd = true,
                                error = UiText.String("No followed users found")
                            )
                        }
                        return
                    }

                    // Load paginated posts
                    when (val result = feedRepository.getPaginatedFeed(_feedUiData.value.lastPostId)) {
                        is ResultWrapper.Success -> {
                            val newPosts = result.data

                            // Check if we've reached the end of pagination
                            if (newPosts.isEmpty()) {
                                _feedUiData.update {
                                    it.copy(
                                        isLoading = false,
                                        hasReachedEnd = true,
                                        error = if (isInitialLoad && _feedUiData.value.postList.isEmpty()) {
                                            UiText.String("No posts found")
                                        } else {
                                            null
                                        }
                                    )
                                }
                                return
                            }

                            // Update lastPostId and fetch like status for new posts
                            _feedUiData.update { it.copy(lastPostId = newPosts.lastOrNull()?.id) }

                            val postsWithLikes = newPosts.map { post ->
                                val isLikedResult = likeRepository.getLikeStatus(
                                    targetId = post.id,
                                    type = LikeType.POST,
                                    postId = null
                                )
                                val isLiked = (isLikedResult as? ResultWrapper.Success)?.data ?: false
                                FeedPostWithLikesAndComments(
                                    post = post,
                                    isLiked = isLiked
                                )
                            }

                            // Deduplicate posts by ID and merge with existing posts
                            _feedUiData.update { currentState ->
                                val existingPosts = if (isInitialLoad) emptyList() else currentState.postList
                                val mergedPosts = (existingPosts + postsWithLikes)
                                    .distinctBy { it.post.id } // Deduplicate by post ID
                                currentState.copy(
                                    postList = mergedPosts,
                                    lastLoadedPostTimestamp = postsWithLikes.lastOrNull()?.post?.createdAt,
                                    isLoading = false,
                                    hasReachedEnd = postsWithLikes.isEmpty(),
                                    error = null
                                )
                            }
                        }
                        is ResultWrapper.Error -> {
                            _feedUiData.update {
                                it.copy(
                                    isLoading = false,
                                    error = UiText.String(result.exception.message ?: "Failed to load posts")
                                )
                            }
                        }
                        is ResultWrapper.Loading -> {
                            // Do nothing
                        }
                    }
                }
                is ResultWrapper.Error -> {
                    _feedUiData.update {
                        it.copy(
                            isLoading = false,
                            error = UiText.String(followingResult.exception.message ?: "Failed to load followed users")
                        )
                    }
                }
                is ResultWrapper.Loading -> {
                    /// Do nothing
                }
            }
        } catch (e: Exception) {
            Log.e("FeedViewModel", "Error loading feed page", e)
            _feedUiData.update {
                it.copy(
                    isLoading = false,
                    error = UiText.String(e.message ?: "Unknown error")
                )
            }
        }
    }


    fun toggleReaction(postId: String) {
        viewModelScope.launch {
            try {
                // Find the post to update
                val targetPost = _feedUiData.value.postList.find { it.post.id == postId }
                if (targetPost == null) return@launch

                // Optimistically update UI state
                val updatedPosts = _feedUiData.value.postList.map { postWithLikes ->
                    if (postWithLikes.post.id == postId) {
                        val isCurrentlyLiked = postWithLikes.isLiked
                        val newLikeStatus = !isCurrentlyLiked
                        val currentLikeCount = postWithLikes.post.postMetrics.likes
                        val newLikeCount = if (isCurrentlyLiked) {
                            // If currently liked, decrease like count (unlike)
                            currentLikeCount - 1
                        } else {
                            // If not liked, increase like count (like)
                            currentLikeCount + 1
                        }

                        // Update the post with new like status and like count
                        postWithLikes.copy(
                            isLiked = newLikeStatus,
                            post = postWithLikes.post.copy(
                                postMetrics = postWithLikes.post.postMetrics.copy(
                                    likes = newLikeCount.coerceAtLeast(0) // Ensure like count doesn't go negative
                                )
                            )
                        )
                    } else {
                        postWithLikes
                    }
                }
                _feedUiData.update { it.copy(postList = updatedPosts) }

                // Perform actual like toggle in the backend
                likeRepository.toggleLike(
                    userId = userRepository.getCurrentUserId(),
                    targetId = postId,
                    type = LikeType.POST
                ).onError {
                    // Revert UI state on failure
                    _feedUiData.update { it.copy(postList = it.postList.map { postWithLikes ->
                        if (postWithLikes.post.id == postId) {
                            postWithLikes.copy(
                                isLiked = !postWithLikes.isLiked,
                                post = postWithLikes.post.copy(
                                    postMetrics = postWithLikes.post.postMetrics.copy(
                                        likes = if (postWithLikes.isLiked) {
                                            postWithLikes.post.postMetrics.likes - 1
                                        } else {
                                            postWithLikes.post.postMetrics.likes + 1
                                        }
                                    )
                                )
                            )
                        } else {
                            postWithLikes
                        }
                    }) }
                    _feedUiData.update { it.copy(error = UiText.StringResource(R.string.error_updating_reaction)) }
                }
            } catch (e: Exception) {
                _feedUiData.update { it.copy(error = UiText.StringResource(R.string.error_updating_reaction)) }
            }
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            if (_feedUiData.value.isLoading || _feedUiData.value.isRefreshing ) return@launch
            _feedUiData.update { it.copy(isRefreshing = true) }

            try {
                // Reset pagination
                _feedUiData.update { it.copy(lastPostId = null) }

                // Clear followingUserIds to force refresh
                _feedUiData.update { it.copy(followingUserIds = emptySet()) }

                // Load fresh data
                loadFeedPage(true)
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error refreshing feed", e)
                _feedUiData.update { it.copy(error = UiText.String(e.message ?: "Unknown error")) }
            } finally {
                _feedUiData.update { it.copy(isRefreshing = false) }
            }
        }
    }


}
