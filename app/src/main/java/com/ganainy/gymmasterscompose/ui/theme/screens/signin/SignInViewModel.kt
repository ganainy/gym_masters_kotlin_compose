package com.ganainy.gymmasterscompose.ui.theme.screens.signin


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.AppConstants
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.AppUtils
import com.ganainy.gymmasterscompose.ui.theme.repository.IAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInFormData(
    val email: String = "",
    val password: String = "",
    val isEmailValid: Boolean = false,
    val isPasswordValid: Boolean = false
)

sealed class SignInUiState {
    object Initial : SignInUiState()
    object Loading : SignInUiState()
    data class Error(val messageStringResource: Int) : SignInUiState()
    object Success : SignInUiState()
}

class SignInViewModel(private val repository: IAuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.Initial)
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    private val _formData = MutableStateFlow(SignInFormData())
    val formData: StateFlow<SignInFormData> = _formData.asStateFlow()

    init {
    }

    fun updatePassword(password: String) {
        _formData.update {
            it.copy(
                password = password, isPasswordValid =
                AppUtils.isValidFieldLength(password, AppConstants.MINIMUM_PASSWORD_LENGTH)
            )
        }
    }


    fun updateEmail(email: String) {
        _formData.update { it.copy(email = email, isEmailValid = AppUtils.isValidEmail(email)) }
    }

    fun logIntoAccount() {
        viewModelScope.launch {
            _uiState.value = SignInUiState.Loading

            val validationResult = validateForm()
            if (validationResult != null) {
                _uiState.value = SignInUiState.Error(validationResult)
                return@launch
            }

            repository.signInUser(_formData.value.email, _formData.value.password).onSuccess {
                _uiState.value = SignInUiState.Success
            }.onFailure {
                _uiState.value = SignInUiState.Error(R.string.authentication_failed)
            }
        }
    }

    private fun validateForm(): Int? {
        return when {
            !_formData.value.isEmailValid -> R.string.invalid_email
            !_formData.value.isPasswordValid -> R.string.invalid_password
            else -> null
        }
    }

}


class SignInViewModelFactory(private val repository: IAuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignInViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SignInViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

