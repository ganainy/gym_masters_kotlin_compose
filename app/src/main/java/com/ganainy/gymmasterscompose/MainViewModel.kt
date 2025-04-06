package com.ganainy.gymmasterscompose

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.prefs.ExerciseDownloadPrefs
import com.ganainy.gymmasterscompose.ui.repository.AuthRepository
import com.ganainy.gymmasterscompose.ui.repository.IExerciseRepository
import com.ganainy.gymmasterscompose.ui.repository.ILikeRepository
import com.ganainy.gymmasterscompose.ui.repository.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * ViewModel for managing user authentication state.
 *
 * @property authRepository The repository for authentication operations.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val likeRepository: ILikeRepository,
    private val exerciseDownloadPrefs: ExerciseDownloadPrefs,
    private val exerciseRepository: IExerciseRepository,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val authState = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            // Start with Loading state and then update based on auth status
            authRepository.isUserLoggedIn().collect { isLoggedIn ->
                _authState.value = if (isLoggedIn) {
                    AuthUiState.Authenticated
                } else {
                    AuthUiState.Unauthenticated
                }
            }
            // Sync pending likes (local and remote) when app first starts
            likeRepository.syncPendingLikes()
        }

        //Download all exercises on the first startup of the app and cache them locally
        triggerInitialExerciseDownload()
    }

    private fun triggerInitialExerciseDownload() {
        if (!exerciseDownloadPrefs.isInitialDownloadComplete()) {
            viewModelScope.launch {
                Log.d("StartupViewModel", "Triggering initial exercise download...")
                val result = exerciseRepository.fetchAllExercisesAndCache()
                when (result) {
                    is ResultWrapper.Success -> {
                        Log.i("StartupViewModel", "Initial exercise download completed successfully.")
                        // Flag is set within the repository on success
                    }
                    is ResultWrapper.Error -> {
                        Log.e("StartupViewModel", "Initial exercise download failed.", result.exception)
                        // Handle error - maybe schedule retry with WorkManager or show user message
                    }
                    is ResultWrapper.Loading -> { /* Should not happen from suspend fun */ }
                }
            }
        } else {
            Log.d("StartupViewModel", "Initial exercise download already complete.")
        }
    }

}


sealed class AuthUiState {
    data object Loading : AuthUiState()
    data object Authenticated : AuthUiState()
    data object Unauthenticated : AuthUiState()
}