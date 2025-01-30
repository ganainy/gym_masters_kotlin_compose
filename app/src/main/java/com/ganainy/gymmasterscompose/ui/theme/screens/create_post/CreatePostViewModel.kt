package com.ganainy.gymmasterscompose.ui.theme.screens.create_post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.Constants
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.repository.IAuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IPostRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ResultWrapper
import com.ganainy.gymmasterscompose.utils.Utils.generateRandomId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CreatePostUiState {
    object Success : CreatePostUiState()
    object Loading : CreatePostUiState()
    data class Error(val messageStringResource: Int) : CreatePostUiState()
}

data class CreatePostUiData(
    val user: User? = null,
    val feedPost: FeedPost,
    val isPostButtonEnabled: Boolean = false
)


@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val postRepository: IPostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreatePostUiState>(CreatePostUiState.Loading)
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()


    private val _createPostUiData =
        MutableStateFlow<CreatePostUiData>(
            CreatePostUiData(
                feedPost = FeedPost(
                    id = generateRandomId(
                        Constants.POST
                    )
                )
            )
        )
    val createPostUiData: StateFlow<CreatePostUiData> = _createPostUiData.asStateFlow()


    init {
        _uiState.value = CreatePostUiState.Loading
        fetchCurrentUser()
        monitorPostContentChanges()
        monitorUserChanges()
    }


    /**
     * Observes changes to the feedPost content field and updates the
     * isPostButtonEnabled state based on whether the content is empty.
     */
    private fun monitorPostContentChanges() {
        // Observe the content of the feedPost for changes
        createPostUiData.map { data -> data.feedPost.content }
            .distinctUntilChanged() // Only emit when the content changes
            .onEach { content ->
                // Enable the post button if content is not empty, disable otherwise
                _createPostUiData.update { it.copy(isPostButtonEnabled = content.isNotEmpty()) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Observes changes to the user field and updates the authorId of the feedPost
     * based on the current user's ID.
     */
    private fun monitorUserChanges() {
        createPostUiData.map { data -> data.user }
            .distinctUntilChanged() // Only emit when the user changes
            .onEach { user ->
                // Update the feedPost's authorId with the current user's ID
                _createPostUiData.update {
                    it.copy(
                        feedPost = it.feedPost.copy(
                            authorId = user?.id ?: ""
                        ),
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            val result = authRepository.getCurrentUser()
            when (result) {
                is ResultWrapper.Success -> {
                    _createPostUiData.update { it.copy(user = result.data) }
                }

                is ResultWrapper.Error -> {
                    // Handle the error
                    _uiState.value = CreatePostUiState.Error(R.string.error_fetching_user)
                }
            }
        }
    }


    fun publishPost() {
        val feedPost = createPostUiData.value.feedPost
        viewModelScope.launch {
            val result = postRepository.createPost(feedPost)
            when (result) {
                is ResultWrapper.Success -> {
                    _uiState.value = CreatePostUiState.Success
                }

                is ResultWrapper.Error -> {
                    // Handle the error
                    _uiState.value = CreatePostUiState.Error(R.string.error_creating_post)
                }
            }
        }
    }


    fun updatePostContent(newPostContent: String) {
        _createPostUiData.update { it.copy(feedPost = it.feedPost.copy(content = newPostContent)) }
    }


}