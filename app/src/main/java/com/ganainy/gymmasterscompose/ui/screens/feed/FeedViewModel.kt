package com.ganainy.gymmasterscompose.ui.screens.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.repository.FeedRepository
import com.ganainy.gymmasterscompose.ui.repository.IFeedRepository
import com.ganainy.gymmasterscompose.ui.repository.ILikeRepository
import com.ganainy.gymmasterscompose.ui.repository.ISocialRepository
import com.ganainy.gymmasterscompose.ui.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.repository.ResultWrapper
import com.ganainy.gymmasterscompose.ui.repository.onError
import com.ganainy.gymmasterscompose.ui.room.LikeType
import com.ganainy.gymmasterscompose.ui.screens.post_details.FeedPostWithLikesAndComments
import com.ganainy.gymmasterscompose.utils.UiText
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// todo create the feedgeneration cloud function to be automatically called when a post is created to
// add it to the feed of the following users
data class FeedUiData(
    val postList: List<FeedPostWithLikesAndComments> = emptyList(),
    val followingUserIds: Set<String> = emptySet(), // Keep track of who is followed
    val lastLoadedPostTimestamp: Timestamp? = null, // Used for pagination cursor
    val lastPostId: String? = null, // Used for pagination cursor (tie-breaker)
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
            if (_feedUiData.value.isLoading || _feedUiData.value.isRefreshing) return@launch
            _feedUiData.update {
                it.copy(
                    // Reset pagination cursors for initial load
                    lastPostId = null,
                    lastLoadedPostTimestamp = null,
                    postList = emptyList(),
                    isLoading = true,
                    isRefreshing = false, // Ensure refreshing is false
                    hasReachedEnd = false, // Reset end flag
                    error = null
                )
            }
            // Pass true for isInitialLoad to loadFeedPage
            loadFeedPage(isInitialLoad = true)
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {
            if (_feedUiData.value.isLoading ||
                _feedUiData.value.isRefreshing ||
                _feedUiData.value.hasReachedEnd // Check if already at the end
            ) return@launch

            _feedUiData.update { it.copy(isLoading = true, error = null) } // Set loading true for next page
            // Pass false for isInitialLoad
            loadFeedPage(isInitialLoad = false)
        }
    }


    private suspend fun loadFeedPage(isInitialLoad: Boolean) {
        // Set loading state based on whether it's initial load or pagination
        /* _feedUiData.update {
             if (isInitialLoad) it.copy(isLoading = true, error = null)
             else it.copy(isLoading = true, error = null) // Keep isLoading true for pagination too
         }*/

        try {
            val currentUserId = userRepository.getCurrentUserId()
            var relevantUserIds: List<String>

            // Fetch followed users (only needed once or on refresh)
            // Avoid refetching if we already have them and are just paginating
            if (_feedUiData.value.followingUserIds.isEmpty() || isInitialLoad || _feedUiData.value.isRefreshing) {
                when (val followingResult = socialRepository.getFollowedUsers(currentUserId)) {
                    is ResultWrapper.Success -> {
                        relevantUserIds = followingResult.data + currentUserId // Add current user
                        _feedUiData.update { it.copy(followingUserIds = relevantUserIds.toSet()) }
                    }
                    is ResultWrapper.Error -> {
                        throw followingResult.exception // Throw exception to be caught below
                    }
                    is ResultWrapper.Loading -> {
                        // Should ideally not happen with suspend fun, but handle defensively
                        _feedUiData.update { it.copy(isLoading = false, error = UiText.String("Error: Unexpected loading state")) }
                        return
                    }
                }
            } else {
                relevantUserIds = _feedUiData.value.followingUserIds.toList()
            }


            // If no users to get feed from, stop loading
            if (relevantUserIds.isEmpty()) {
                _feedUiData.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        postList = emptyList(),
                        hasReachedEnd = true,
                        error = UiText.String("Follow users to see their posts")
                    )
                }
                return
            }

            val result = feedRepository.getPaginatedFeed(
                relevantUserIds = relevantUserIds,
                lastPostTimestamp = if (isInitialLoad) null else _feedUiData.value.lastLoadedPostTimestamp,
                lastPostId = if (isInitialLoad) null else _feedUiData.value.lastPostId
            )

            when (result) {
                is ResultWrapper.Success -> {
                    val newPosts = result.data
                    val reachedEnd = newPosts.isEmpty() || newPosts.size < FeedRepository.PAGE_SIZE // Check if fewer posts than requested were returned

                    // Fetch like status for the newly fetched posts
                    val postsWithLikes = newPosts.map { post ->
                        val isLikedResult = likeRepository.getLikeStatus(
                            targetId = post.id,
                            type = LikeType.POST,
                            postId = null // postId not needed for POST type likes
                        )
                        // Handle potential error when fetching like status, default to false
                        val isLiked = (isLikedResult as? ResultWrapper.Success<Boolean>)?.data ?: false
                        FeedPostWithLikesAndComments(post = post, isLiked = isLiked)
                    }

                    // Update state: Append new posts or set initial list
                    _feedUiData.update { currentState ->
                        // Merge new posts with existing ones, ensuring no duplicates
                        val existingPosts = if (isInitialLoad) emptyList() else currentState.postList
                        val allPosts = (existingPosts + postsWithLikes)
                            .distinctBy { it.post.id } // Ensure uniqueness

                        currentState.copy(
                            postList = allPosts,
                            // Update pagination cursors with the last item from the *newly fetched* list
                            lastLoadedPostTimestamp = newPosts.lastOrNull()?.createdAt ?: currentState.lastLoadedPostTimestamp,
                            lastPostId = newPosts.lastOrNull()?.id ?: currentState.lastPostId,
                            isLoading = false, // Loading finished
                            isRefreshing = false, // Refreshing finished if it was a refresh
                            hasReachedEnd = reachedEnd, // Update end flag
                            error = if (isInitialLoad && allPosts.isEmpty()) UiText.String("No posts found in feed.") else null // Clear error on success
                        )
                    }
                }
                is ResultWrapper.Error -> {
                    // Throw exception to be caught by the outer try-catch
                    throw result.exception
                }
                is ResultWrapper.Loading -> { /* Handled by initial isLoading update */ }
            }
        } catch (e: Exception) {
            Log.e("FeedViewModel", "Error loading feed page (isInitialLoad=$isInitialLoad)", e)
            _feedUiData.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = UiText.String(e.message ?: "Failed to load feed")
                )
            }
        }
    }



    fun toggleReaction(postId: String) {
        viewModelScope.launch {
            val currentUserId = try { userRepository.getCurrentUserId() } catch (e: Exception) { null }
            if(currentUserId == null) {
                _feedUiData.update { it.copy(error = UiText.StringResource(R.string.error_must_be_logged_in)) }
                return@launch
            }

            val originalList = _feedUiData.value.postList
            val postIndex = originalList.indexOfFirst { it.post.id == postId }
            if (postIndex == -1) return@launch // Post not found

            val targetPostWithLikes = originalList[postIndex]
            val isCurrentlyLiked = targetPostWithLikes.isLiked
            val newLikeStatus = !isCurrentlyLiked
            val currentLikeCount = targetPostWithLikes.post.postMetrics.likes
            // Determine new like count based on the action (like or unlike)
            val newLikeCount = (if (newLikeStatus) currentLikeCount + 1 else currentLikeCount - 1).coerceAtLeast(0)

            // Optimistically update the specific post in the list
            val optimisticList = originalList.toMutableList()
            optimisticList[postIndex] = targetPostWithLikes.copy(
                isLiked = newLikeStatus,
                post = targetPostWithLikes.post.copy(
                    postMetrics = targetPostWithLikes.post.postMetrics.copy(
                        likes = newLikeCount
                    )
                )
            )
            _feedUiData.update { it.copy(postList = optimisticList) }

            // Perform backend toggle
            val result = likeRepository.toggleLike(
                targetId = postId,
                type = LikeType.POST
            )

            if (result is ResultWrapper.Error) {
                Log.e("FeedViewModel", "Failed to toggle like for $postId", result.exception)
                // Revert UI on failure
                _feedUiData.update { it.copy(postList = originalList) } // Revert to original list
                _feedUiData.update { it.copy(error = UiText.StringResource(R.string.error_updating_reaction)) }
            }
            // No else needed, UI is already updated optimistically
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            if (_feedUiData.value.isLoading || _feedUiData.value.isRefreshing ) return@launch
            _feedUiData.update {
                it.copy(
                    isRefreshing = true,
                    // Reset pagination and end flag for refresh
                    lastPostId = null,
                    lastLoadedPostTimestamp = null,
                    hasReachedEnd = false,
                    followingUserIds = emptySet() // Force refetch of followed users
                )
            }
            loadFeedPage(isInitialLoad = true) // Load page 1 after setting refresh state
            // The loadFeedPage function will set isRefreshing back to false on completion/error
        }
    }


}
