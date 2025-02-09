package com.ganainy.gymmasterscompose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.ui.theme.repository.AuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ILikeRepository
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
    }
}


sealed class AuthUiState {
    data object Loading : AuthUiState()
    data object Authenticated : AuthUiState()
    data object Unauthenticated : AuthUiState()
}