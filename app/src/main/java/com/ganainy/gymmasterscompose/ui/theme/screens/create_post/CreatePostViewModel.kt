package com.ganainy.gymmasterscompose.ui.theme.screens.create_post

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class CreatePostUiState {
    object Success : CreatePostUiState()
    object Loading : CreatePostUiState()
    data class Error(val messageStringResource: Int) : CreatePostUiState()
}

@HiltViewModel
class CreatePostViewModel @Inject constructor() : ViewModel() {



    var selectedExerciseId: String?=null
    var selectedWorkoutId: String?=null


    private val _postContent = MutableStateFlow("")
    val postContent: StateFlow<String> = _postContent.asStateFlow()

    fun updatePostContent(newContent: String) {
        _postContent.value = newContent
    }

    private val _uiState = MutableStateFlow<CreatePostUiState>(CreatePostUiState.Loading)
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()



    init {
        _uiState.value = CreatePostUiState.Loading


    }


    fun submitPost() {
        /* postContent,
         emptyList(),
         selectedExerciseId,
         selectedWorkoutId*/
    }
    fun createPost(
        content: String,
        mediaUrls: List<String> = emptyList(),
        linkedExerciseId: String? = null,
        linkedWorkoutId: String? = null
    ) {
      /*  viewModelScope.launch {
            try {
                if (currentUserId == null) {
                    throw IllegalStateException("Current user ID is null")
                }

                val newPost = FeedPost(
                    id = UUID.randomUUID().toString(),
                    authorId = currentUserId,
                    content = content,
                    mediaUrls = mediaUrls,
                    createdAt = System.currentTimeMillis(),
                    linkedExerciseId = linkedExerciseId,
                    linkedWorkoutId = linkedWorkoutId
                )

                dataRepository.createPost(
                    newPost,
                    onSuccess = { *//* Post will appear via listener *//* },
                    onFailure = { messageRes ->
                        _uiState.update { it.copy(error = messageRes) }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = R.string.error_creating_post) }
            }
        }*/
    }




}