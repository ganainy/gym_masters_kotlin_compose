package com.ganainy.gymmasterscompose.ui.theme.screens.post_details

import Comment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.repository.ICommentsRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ILikeRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IPostRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.theme.room.LikeType
import com.ganainy.gymmasterscompose.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


data class PostDetailsUiData(
    val loadingPost: Boolean,
    val loadingComments: Boolean,
    val error: UiText?,
    val feedPostWithLikesAndComments: FeedPostWithLikesAndComments?,
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


    fun setLikeStatus(isLiked: Boolean) {
        _postDetailsUiData.update { currentState ->
            _postDetailsUiData.value.copy(
                loadingPost = false,
                feedPostWithLikesAndComments = currentState.feedPostWithLikesAndComments?.copy(
                    isLiked = isLiked
                )
            )
        }
    }



    /**
     * Observes the comments of a specific post and updates the UI state with the fetched data.
     *
     * This function performs the following actions:
     * 1. Sets the `loadingComments` flag to `true` in the UI state to indicate that comment loading is in progress.
     * 2. Retrieves the post ID from the current `feedPostWithLikesAndComments` in the UI state.
     * 3. Observes the comments associated with the post ID using `commentsRepository.observePostComments()`.
     * 4. Uses `distinctUntilChanged()` to prevent redundant updates if the comment list hasn't changed.
     * 5. Employs `flatMapLatest` to handle potential updates to the comment list efficiently.
     *    - If the comment list is empty, it emits an empty list immediately.
     *    - If comments exist, it uses `combine` to fetch and merge the like status for each comment.
     *      - It calls `likeRepository.observeLikeStatus()` for each comment to determine if it's liked by the current user.
     *      - It maps each comment and its like status to a `CommentWithLikeStatus` object.
     *      - It combines all `CommentWithLikeStatus` objects into a list.
     * 6. Uses `catch` to handle potential errors during the observation process.
     *    - If an error occurs, it updates the UI state by setting `loadingComments` to `false` and populating the `error` field with the error message.
     * 7. Uses `collectLatest` to collect the final stream of `List<CommentWithLikeStatus>` objects.
     *    - Updates the UI state by setting `loadingComments` to `false` and updating the `commentList` within the `feedPostWithLikesAndComments` object.
     * 8. A global `try-catch` block handles potential errors at the top level, updating the UI state accordingly if exceptions occur outside the flow */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observePostComments() {
        viewModelScope.launch {
            try {
                _postDetailsUiData.update { it.copy(loadingComments = true) }

                _postDetailsUiData.value.feedPostWithLikesAndComments?.post?.let { post ->
                    commentsRepository.observePostComments(post.id)
                        .distinctUntilChanged()
                        .flatMapLatest { comments ->
                            // If no comments, emit empty list immediately
                            if (comments.isEmpty()) {
                                flow { emit(emptyList()) }
                            } else {
                                // Combine like status for each comment
                                combine(
                                    comments.map { comment ->
                                        likeRepository.observeLikeStatus(
                                            targetId = comment.id,
                                            type = LikeType.COMMENT,
                                            postId = post.id
                                        ).map { isLiked ->
                                            CommentWithLikeStatus(
                                                comment = comment,
                                                isLiked = isLiked
                                            )
                                        }
                                    }
                                ) { commentsWithLikes -> commentsWithLikes.toList() }
                            }
                        }
                        .catch { error ->
                            _postDetailsUiData.update { currentState ->
                                currentState.copy(
                                    loadingComments = false,
                                    error = UiText.String(error.message ?: "Unknown error")
                                )
                            }
                        }
                        .collectLatest { commentsWithLikes ->
                            _postDetailsUiData.update { currentState ->
                                currentState.copy(
                                    loadingComments = false,
                                    feedPostWithLikesAndComments = currentState.feedPostWithLikesAndComments?.copy(
                                        commentList = commentsWithLikes
                                    )
                                )
                            }
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


    fun observePostMetricsUpdates() {
        viewModelScope.launch {
            try {
                postRepository.observePostMetricsUpdates(
                    _postDetailsUiData.value.feedPostWithLikesAndComments?.post?.id
                        ?: throw IllegalStateException("Cannot observePostMetrics without a post ID")
                )
                    .distinctUntilChanged()
                    .collectLatest { metrics ->
                        _postDetailsUiData.update {
                            _postDetailsUiData.value.copy(
                                feedPostWithLikesAndComments = _postDetailsUiData.value.feedPostWithLikesAndComments?.post?.copy(
                                    postMetrics = metrics
                                )?.let { it1 ->
                                    _postDetailsUiData.value.feedPostWithLikesAndComments?.copy(
                                        post = it1
                                    )
                                }
                            )
                        }
                    }
            } catch (e: Exception) {
                _postDetailsUiData.update {
                    _postDetailsUiData.value.copy(
                        error = e.message?.let { errorMessage -> UiText.String(errorMessage) }
                    )
                }
            }

        }
    }


    fun togglePostLike() {
        val postId = _postDetailsUiData.value.feedPostWithLikesAndComments?.post?.id
            ?: throw IllegalStateException("Cannot toggle post like without a post ID")
        viewModelScope.launch {
            runCatching {
                likeRepository.toggleLike(
                    userId = userRepository.getCurrentUserId(),
                    targetId = postId,
                    type = LikeType.POST
                )
                setLikeStatus(
                    !(_postDetailsUiData.value.feedPostWithLikesAndComments?.isLiked ?: false)
                )
            }.onFailure {
                _postDetailsUiData.update { currentState ->
                    currentState.copy(
                        error = UiText.StringResource(R.string.error_updating_reaction)
                    )
                }
            }
        }

    }


    fun toggleCommentLike(commentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                likeRepository.toggleLike(
                    targetId = commentId,
                    type = LikeType.COMMENT,
                    postId = _postDetailsUiData.value.feedPostWithLikesAndComments?.post?.id
                )
            }.onFailure { error ->
                _postDetailsUiData.update { currentState ->
                    currentState.copy(
                        error = UiText.StringResource(R.string.error_updating_reaction)
                    )
                }
            }
        }
    }


    fun submitComment(commentContent: String) {

        val postId = _postDetailsUiData.value.feedPostWithLikesAndComments?.post?.id
            ?: throw IllegalStateException("Cannot submit comment without a post ID")

        // Update UI immediately with local new comment
        val tempComment = CommentWithLikeStatus(
            Comment(
                id = UUID.randomUUID().toString(), // Fake ID
                content = commentContent,
                isPending = true,
                userDisplayInfo = userRepository.getUserDisplayInfo()
            )
        )

        viewModelScope.launch {
            try {
                _postDetailsUiData.update { currentState ->
                    currentState.copy(
                        feedPostWithLikesAndComments = currentState.feedPostWithLikesAndComments?.copy(
                            commentList = currentState.feedPostWithLikesAndComments.commentList + tempComment
                        )
                    )
                }

                // Add comment to Firebase
                commentsRepository.addComment(postId = postId, content = commentContent)

            } catch (e: Exception) {
                _postDetailsUiData.update { currentState ->
                    currentState.copy(
                        error = e.message?.let { UiText.String(it) }
                            ?: UiText.StringResource(R.string.error_adding_comment),
                        feedPostWithLikesAndComments = currentState.feedPostWithLikesAndComments?.copy(
                            commentList = currentState.feedPostWithLikesAndComments.commentList - tempComment
                        )
                    )
                }
            }
        }
    }


}