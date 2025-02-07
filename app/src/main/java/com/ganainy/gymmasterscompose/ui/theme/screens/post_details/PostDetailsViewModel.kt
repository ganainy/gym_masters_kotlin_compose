package com.ganainy.gymmasterscompose.ui.theme.screens.post_details

import Comment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.repository.ICommentsRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IPostRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IUserRepository
import com.ganainy.gymmasterscompose.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
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

data class FeedPostWithLikesAndComments(
    val post: FeedPost,
    val isLiked: Boolean = false, // used to show like icon as filled or empty based on if user liked the post or not
    val showCommentSection: Boolean = false, // used to show comment section when user clicks on comment icon
    val commentList: List<Comment> = emptyList()
)

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val postRepository: IPostRepository,
    private val commentsRepository: ICommentsRepository,
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


    fun isCommentLiked(commentId: String, postId: String): Flow<Boolean> {
        return commentsRepository.observeIsCommentLikedByCurrentUser(commentId, postId)
            .stateIn(viewModelScope, SharingStarted.Lazily, false) // Default false if no data
    }



    fun observePostComments() {
        _postDetailsUiData.update {
            _postDetailsUiData.value.copy(
                loadingComments = true
            )
        }
        viewModelScope.launch {
            try {
                _postDetailsUiData.value.feedPostWithLikesAndComments?.post?.let {
                    commentsRepository.observePostComments(it.id)
                        .distinctUntilChanged()
                        .collectLatest { comments ->
                            _postDetailsUiData.update {
                                _postDetailsUiData.value.copy(
                                    loadingComments = false,
                                    feedPostWithLikesAndComments = _postDetailsUiData.value.feedPostWithLikesAndComments?.copy(
                                        commentList = comments,
                                    )
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                _postDetailsUiData.update {
                    _postDetailsUiData.value.copy(
                        loadingComments = false,
                        error = e.message?.let { errorMessage -> UiText.String(errorMessage) }
                    )
                }
            }
        }
    }


    fun observePostMetricsUpdates() {
        viewModelScope.launch {
            try {
                postRepository.observePostMetricsUpdates(_postDetailsUiData.value.feedPostWithLikesAndComments?.post?.id?:
                throw IllegalStateException("Cannot observePostMetrics without a post ID"))
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
                postRepository.togglePostReaction(userId = null, postId = postId)
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
        val comment = _postDetailsUiData.value.feedPostWithLikesAndComments?.commentList?.find { it.id == commentId }
            ?: throw IllegalStateException("Cannot toggle comment like without a comment ID")
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                commentsRepository.toggleCommentLike(comment)
            }.onFailure {
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
        val tempComment = Comment(
            id = UUID.randomUUID().toString(), // Fake ID
            content = commentContent,
            isPending = true,
            userDisplayInfo = userRepository.getUserDisplayInfo()
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