package com.ganainy.gymmasterscompose.ui.theme.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.repository.AuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class FeedData(
    val uid: String = "",
)

sealed class FeedUiState {
    object Initial : FeedUiState()
    object Loading : FeedUiState()
    object LogoutSuccess : FeedUiState()
    data class Error(val messageStringResource: Int) : FeedUiState()
}

@HiltViewModel
class FeedViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Initial)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _feedData = MutableStateFlow(FeedData())
    val feedData: StateFlow<FeedData> = _feedData.asStateFlow()


    fun signOut() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            repository.signOut().onSuccess {
                _uiState.value = FeedUiState.LogoutSuccess
            }.onFailure {
                _uiState.value = FeedUiState.Error(R.string.error_logging_out)
            }
        }
    }

}
