package com.ganainy.gymmasterscompose.ui.screens.post_details

import Comment
import UserDisplayInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.repository.ICommentsRepository
import com.ganainy.gymmasterscompose.ui.repository.ILikeRepository
import com.ganainy.gymmasterscompose.ui.repository.IPostRepository
import com.ganainy.gymmasterscompose.ui.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.repository.ResultWrapper
import com.ganainy.gymmasterscompose.ui.repository.onError
import com.ganainy.gymmasterscompose.ui.room.LikeType
import com.ganainy.gymmasterscompose.utils.UiText
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


data class PostDetailsUiData(
    val loadingPost: Boolean,
    val loadingComments: Boolean,
    val error: UiText? = null,
    val feedPostWithLikesAndComments: FeedPostWithLikesAndComments? = null,
)

/**
 * Represents a feed post along with its associated likes and comments, including the user's interaction status.
 *
 * This data class combines a [FeedPost] with additional information about whether the current user has liked the post,
 * whether the comment section should be displayed, and a list of comments with their respective like statuses.
 *
 * @property post The [FeedPost] object containing the core post information (e.g., content, author, timestamp).
 * @property isLiked `true` if the current user has liked the post, `false` otherwise. This is used to determine whether
 *                   to display the like icon as filled or empty.
 * @property commentList A list of [CommentWithLikeStatus] objects, representing the comments associated with this post.
 *                       Each comment includes information about the comment itself and whether the current user has
 *                       liked it. Defaults to an empty list.
 */
data class FeedPostWithLikesAndComments(
    val post: FeedPost,
    val isLiked: Boolean = false, // used to show like icon as filled or empty based on if user liked the post or not
    val commentList: List<CommentWithLikeStatus> = emptyList()
)

data class CommentWithLikeStatus(
    val comment: Comment,
    val isLiked: Boolean = false
)

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val postRepository: IPostRepository,
    private val commentsRepository: ICommentsRepository,
    private val likeRepository: ILikeRepository
) : ViewModel() {


    private val _postDetailsUiData =
        MutableStateFlow(
            PostDetailsUiData(
                loadingPost = true,
                loadingComments = true,
                error = null,
                null
            )
        )
    val postDetailsUiData: StateFlow<PostDetailsUiData> = _postDetailsUiData.asStateFlow()

    fun setPost(post: FeedPost) {
        _postDetailsUiData.update {
            _postDetailsUiData.value.copy(
                loadingPost = false,
                feedPostWithLikesAndComments = FeedPostWithLikesAndComments(
                    post = post,
                )
            )
        }
    }


    fun loadPostComments() {
        viewModelScope.launch {
            try {
                // Set loading state
                _postDetailsUiData.update { it.copy(loadingComments = true) }

                _postDetailsUiData.value.feedPostWithLikesAndComments?.post?.let { post ->
                    // Fetch comments (one-time read)
                    when (val commentsResult = commentsRepository.getPostComments(post.id)) {
                        is ResultWrapper.Success -> {
                            val comments = commentsResult.data
                            if (comments.isEmpty()) {
                                // If no comments, update UI with empty list
                                _postDetailsUiData.update { currentState ->
                                    currentState.copy(
                                        loadingComments = false,
                                        feedPostWithLikesAndComments = currentState.feedPostWithLikesAndComments?.copy(
                                            commentList = emptyList()
                                        )
                                    )
                                }
                                return@let
                            }

                            // Fetch like status for each comment (one-time read)
                            val commentsWithLikes = comments.map { comment ->
                                val isLikedResult = likeRepository.getLikeStatus(
                                    targetId = comment.id,
                                    type = LikeType.COMMENT,
                                    postId = post.id
                                )
                                val isLiked =
                                    (isLikedResult as? ResultWrapper.Success)?.data ?: false
                                CommentWithLikeStatus(
                                    comment = comment,
                                    isLiked = isLiked
                                )
                            }

                            // Update UI with comments and like statuses
                            _postDetailsUiData.update { currentState ->
                                currentState.copy(
                                    loadingComments = false,
                                    feedPostWithLikesAndComments = currentState.feedPostWithLikesAndComments?.copy(
                                        commentList = commentsWithLikes
                                    )
                                )
                            }
                        }

                        is ResultWrapper.Error -> {
                            _postDetailsUiData.update { currentState ->
                                currentState.copy(
                                    loadingComments = false,
                                    error = UiText.String(
                                        commentsResult.exception.message ?: "Unknown error"
                                    )
                                )
                            }
                        }

                        is ResultWrapper.Loading -> {
                            // Already handled by loadingComments = true at the start
                        }
                    }
                } ?: run {
                    // Post is null, update error state
                    _postDetailsUiData.update { currentState ->
                        currentState.copy(
                            loadingComments = false,
                            error = UiText.String("Post not found")
                        )
                    }
                }
            } catch (e: Exception) {
                _postDetailsUiData.update { currentState ->
                    currentState.copy(
                        loadingComments = false,
                        error = UiText.String(e.message ?: "Unknown error")
                    )
                }
            }
        }
    }

    fun loadPostMetrics() {
        viewModelScope.launch {
            try {
                val postId = _postDetailsUiData.value.feedPostWithLikesAndComments?.post?.id
                    ?: run {
                        _postDetailsUiData.update {
                            it.copy(error = UiText.String("Cannot load post metrics without a post ID"))
                        }
                        return@launch
                    }

                // Fetch post metrics (one-time read)
                when (val metricsResult = postRepository.getPostMetrics(postId)) {
                    is ResultWrapper.Success -> {
                        val metrics = metricsResult.data
                        likeRepository.observeLikeStatus(targetId = postId, type = LikeType.POST).collectLatest { isLiked ->
                            _postDetailsUiData.update { currentState ->
                                currentState.copy(
                                    feedPostWithLikesAndComments = currentState.feedPostWithLikesAndComments?.copy(
                                        post = currentState.feedPostWithLikesAndComments.post.copy(
                                            postMetrics = metrics
                                        ),
                                        isLiked = isLiked
                                    )
                                )
                            }
                        }
                    }

                    is ResultWrapper.Error -> {
                        _postDetailsUiData.update { currentState ->
                            currentState.copy(
                                error = UiText.String(
                                    metricsResult.exception.message ?: "Failed to load post metrics"
                                )
                            )
                        }
                    }

                    is ResultWrapper.Loading -> {
                        // Handle loading state if needed (optional)
                    }
                }
            } catch (e: Exception) {
                _postDetailsUiData.update { currentState ->
                    currentState.copy(
                        error = UiText.String(e.message ?: "Unknown error")
                    )
                }
            }
        }
    }

    fun togglePostLike() {
        val postWithLikes = _postDetailsUiData.value.feedPostWithLikesAndComments
            ?: return
        viewModelScope.launch {
            try {
                // Get current states
                val currentLikeStatus = postWithLikes.isLiked
                val currentLikeCount = postWithLikes.post.postMetrics.likes

                // Calculate new states
                val newLikeStatus = !currentLikeStatus
                val newLikeCount = if (currentLikeStatus) {
                    currentLikeCount - 1
                } else {
                    currentLikeCount + 1
                }

                // Optimistically update UI state
                _postDetailsUiData.update { currentState ->
                    currentState.copy(
                        feedPostWithLikesAndComments = currentState.feedPostWithLikesAndComments?.copy(
                            isLiked = newLikeStatus,
                            post = currentState.feedPostWithLikesAndComments.post.copy(
                                postMetrics = currentState.feedPostWithLikesAndComments.post.postMetrics.copy(
                                    likes = newLikeCount.coerceAtLeast(0)
                                )
                            )
                        )
                    )
                }
                // Perform actual like toggle in the backend
                likeRepository.toggleLike(
                    userId = userRepository.getCurrentUserId(),
                    targetId = postWithLikes.post.id,
                    type = LikeType.POST
                ).onError {
                    // Revert UI state on failure
                    _postDetailsUiData.update { currentState ->
                        currentState.copy(
                            feedPostWithLikesAndComments = currentState.feedPostWithLikesAndComments?.copy(
                                isLiked = currentLikeStatus,
                                post = currentState.feedPostWithLikesAndComments.post.copy(
                                    postMetrics = currentState.feedPostWithLikesAndComments.post.postMetrics.copy(
                                        likes = currentLikeCount
                                    )
                                )
                            ),
                            error = UiText.StringResource(R.string.error_updating_reaction)
                        )
                    }
                }
            } catch (e: Exception) {
                _postDetailsUiData.update { currentState ->
                    currentState.copy(
                        error = UiText.StringResource(R.string.error_updating_reaction)
                    )
                }
            }
        }
    }

    fun toggleCommentLike(commentId: String) {
        viewModelScope.launch {
            try {
                // Find the target comment to update
                val targetComment =
                    _postDetailsUiData.value.feedPostWithLikesAndComments?.commentList?.find {
                        it.comment.id == commentId
                    } ?: return@launch

                // Optimistically update UI state
                val updatedComments =
                    _postDetailsUiData.value.feedPostWithLikesAndComments?.commentList?.map { commentWithLikes ->
                        if (commentWithLikes.comment.id == commentId) {
                            val isCurrentlyLiked = commentWithLikes.isLiked
                            val newLikeStatus = !isCurrentlyLiked
                            val currentLikeCount = commentWithLikes.comment.likesCount
                            val newLikeCount = if (isCurrentlyLiked) {
                                // If currently liked, decrease like count (unlike)
                                currentLikeCount - 1
                            } else {
                                // If not liked, increase like count (like)
                                currentLikeCount + 1
                            }

                            // Update the comment with new like status and count
                            commentWithLikes.copy(
                                isLiked = newLikeStatus,
                                comment = commentWithLikes.comment.copy(
                                    likesCount = newLikeCount.coerceAtLeast(0) // Ensure like count doesn't go negative
                                )
                            )
                        } else {
                            commentWithLikes
                        }
                    }

                // Update UI state with optimistic changes
                _postDetailsUiData.update { currentState ->
                    currentState.copy(
                        feedPostWithLikesAndComments = currentState.feedPostWithLikesAndComments?.copy(
                            commentList = updatedComments ?: emptyList()
                        )
                    )
                }

                // Perform actual like toggle in the backend
                likeRepository.toggleLike(
                    targetId = commentId,
                    type = LikeType.COMMENT,
                    postId = _postDetailsUiData.value.feedPostWithLikesAndComments?.post?.id
                ).onError {
                    // Revert UI state on failure
                    _postDetailsUiData.update { currentState ->
                        currentState.copy(
                            feedPostWithLikesAndComments = currentState.feedPostWithLikesAndComments?.copy(
                                commentList = currentState.feedPostWithLikesAndComments.commentList.map { commentWithLikes ->
                                    if (commentWithLikes.comment.id == commentId) {
                                        commentWithLikes.copy(
                                            isLiked = !commentWithLikes.isLiked,
                                            comment = commentWithLikes.comment.copy(
                                                likesCount = if (commentWithLikes.isLiked) {
                                                    commentWithLikes.comment.likesCount - 1
                                                } else {
                                                    commentWithLikes.comment.likesCount + 1
                                                }
                                            )
                                        )
                                    } else {
                                        commentWithLikes
                                    }
                                }
                            ),
                            error = UiText.StringResource(R.string.error_updating_reaction)
                        )
                    }
                }
            } catch (e: Exception) {
                _postDetailsUiData.update { currentState ->
                    currentState.copy(
                        error = UiText.StringResource(R.string.error_updating_reaction)
                    )
                }
            }
        }
    }


    fun addComment(content: String) {
        viewModelScope.launch {
            try {
                // Get current state
                val currentState = _postDetailsUiData.value
                val currentPost = currentState.feedPostWithLikesAndComments?.post
                if (currentPost == null) {
                    _postDetailsUiData.update { it.copy(error = UiText.String("Post not found")) }
                    return@launch
                }

                // Get current user info
                val userId = userRepository.getCurrentUserId()
                when (val userResult = userRepository.getUser(null)) {
                    is ResultWrapper.Error -> {
                        _postDetailsUiData.update {
                            it.copy(
                                error = UiText.String(
                                    userResult.exception.message ?: "Failed to get user info"
                                )
                            )
                        }
                        return@launch
                    }

                    is ResultWrapper.Success -> {
                        val user = userResult.data
                        val userDisplayInfo = UserDisplayInfo(
                            displayName = user.displayName,
                            profileImageUrl = user.profilePictureUrl
                        )

                        // Create a temporary comment with a unique ID (e.g., UUID)
                        val tempCommentId = UUID.randomUUID().toString()
                        val newComment = Comment(
                            id = tempCommentId,
                            postId = currentPost.id,
                            userId = userId,
                            content = content,
                            userDisplayInfo = userDisplayInfo,
                            timestamp = Timestamp.now(),
                            isPending = true // Mark as pending for optimistic UI update
                        )
                        val newCommentWithLikeStatus = CommentWithLikeStatus(
                            comment = newComment,
                            isLiked = false // New comment is not liked by default
                        )

                        // Optimistically update UI state with the new comment
                        val currentComments =
                            currentState.feedPostWithLikesAndComments?.commentList ?: emptyList()
                        val updatedComments = currentComments + newCommentWithLikeStatus
                        _postDetailsUiData.update { currentState ->
                            currentState.copy(
                                feedPostWithLikesAndComments = currentState.feedPostWithLikesAndComments?.copy(
                                    commentList = updatedComments,
                                    post = currentState.feedPostWithLikesAndComments.post.copy(
                                        postMetrics = currentState.feedPostWithLikesAndComments.post.postMetrics.copy(
                                            comments = updatedComments.size // Update comment count
                                        )
                                    )
                                )
                            )
                        }

                        // Perform actual comment upload to backend
                        when (val uploadResult = commentsRepository.addComment(newComment)) {
                            is ResultWrapper.Success -> {
                                // Backend upload successful, refresh comments to get updated data
                                loadPostComments()
                            }

                            is ResultWrapper.Error -> {
                                // Revert UI state on failure
                                _postDetailsUiData.update { currentState ->
                                    currentState.copy(
                                        feedPostWithLikesAndComments = currentState.feedPostWithLikesAndComments?.copy(
                                            commentList = currentComments,
                                            post = currentState.feedPostWithLikesAndComments.post.copy(
                                                postMetrics = currentState.feedPostWithLikesAndComments.post.postMetrics.copy(
                                                    comments = currentComments.size // Revert comment count
                                                )
                                            )
                                        ),
                                        error = UiText.String(
                                            uploadResult.exception.message
                                                ?: "Failed to add comment"
                                        )
                                    )
                                }
                            }

                            is ResultWrapper.Loading -> {
                                // Already handled by loadingComments = true in loadPostComments
                            }
                        }
                    }

                    is ResultWrapper.Loading -> {
                        // Handle loading state if needed
                    }
                }
            } catch (e: Exception) {
                _postDetailsUiData.update { currentState ->
                    currentState.copy(
                        error = UiText.String(e.message ?: "Unknown error")
                    )
                }
            }
        }
    }


}