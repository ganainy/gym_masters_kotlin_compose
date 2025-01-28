package com.ganainy.gymmasterscompose.ui.theme.screens.feed

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.repository.IAuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IPostRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ISocialRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IUsersRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IWorkoutRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ResultWrapper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class FeedUiState(
    val posts: List<FeedPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val application: Application,
    private val auth: FirebaseAuth,
    private val authRepository: IAuthRepository,
    private val socialRepository: ISocialRepository,
    private val userRepository: IUserRepository,
    private val usersRepository: IUsersRepository,
    private val workoutRepository: IWorkoutRepository,
    private val postRepository: IPostRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var followingUserIds = mutableSetOf<String>()
    private val currentUserId = auth.uid

    private val context = application

    init {
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            socialRepository.getUserFollowing(currentUserId)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            error = context.getString(R.string.error_loading_feed),
                            isLoading = false
                        )
                    }
                }
                .collect { result ->
                    when (result) {
                        is ResultWrapper.Error -> {
                            _uiState.update { it.copy(error = result.exception.message, isLoading = false) }
                        }

                        is ResultWrapper.Success -> {
                            followingUserIds = result.data.toMutableSet()
                            if (currentUserId != null) followingUserIds.add(currentUserId)
                            listenToFeedPosts()
                        }
                    }
                }
        }
    }

    private fun listenToFeedPosts() {
        viewModelScope.launch {
            usersRepository.listenToPostsByUsers(followingUserIds)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            error = context.getString(R.string.error_loading_posts),
                            isLoading = false
                        )
                    }
                }
                .collect { result ->
                    when (result) {
                        is ResultWrapper.Error -> {
                            _uiState.update { it.copy(error = result.exception.message, isLoading = false) }
                        }
                        is ResultWrapper.Success -> {
                            // Enrich posts with author, exercise, and workout data
                            val enrichedPosts = result.data.map { post ->
                                enrichPost(post)
                            }

                            // Sort by creation time, newest first
                            val sortedPosts = enrichedPosts.sortedByDescending { it.createdAt }

                            _uiState.update {
                                it.copy(
                                    posts = sortedPosts,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                    }
                }
        }
    }

    private suspend fun enrichPost(post: FeedPost): FeedPost {
        return coroutineScope {
            // Launch parallel requests for author and linked content
            val authorDeferred = async { userRepository.getUser(post.authorId).first() }
            val exerciseDeferred = post.linkedExerciseId?.let {
                async { workoutRepository.getExercise(it) }
            }
            val workoutDeferred = post.linkedWorkoutId?.let {
                async { workoutRepository.getWorkout(it) }
            }
            val reactionDeferred = async {
                postRepository.getUserReactionForPost(post.id, currentUserId ?: "")
            }

            // Wait for all requests to complete
            post.copy(
                author = (authorDeferred.await() as? ResultWrapper.Success)?.data,
                linkedExercise = exerciseDeferred?.await(),
                linkedWorkout = workoutDeferred?.await(),
                currentUserReaction = reactionDeferred.await()
            )
        }
    }


    fun toggleReaction(postId: String) {
        viewModelScope.launch {
            when (val result = postRepository.togglePostReaction(postId = postId, reactionType = "LIKE")) {
                is ResultWrapper.Success -> {
                    // Handle success if needed
                }
                is ResultWrapper.Error -> {
                    _uiState.update { it.copy(error = result.exception.message) }
                }
            }
        }
    }

    fun refreshFeed() {
        _uiState.update { it.copy(isLoading = true) }
        loadFeed()
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = authRepository.signOut()) {
                is ResultWrapper.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSignedOut()
                }
                is ResultWrapper.Error -> {
                    _uiState.update { it.copy(error = result.exception.message, isLoading = false) }
                }
            }
        }
    }

}
