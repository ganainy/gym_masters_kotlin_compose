package com.ganainy.gymmasterscompose.ui.screens.signin


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.Constants
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.repository.AuthRepository
import com.ganainy.gymmasterscompose.ui.repository.ResultWrapper
import com.ganainy.gymmasterscompose.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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

@HiltViewModel
class SignInViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

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
                Utils.isValidFieldLength(password, Constants.MINIMUM_PASSWORD_LENGTH)
            )
        }
    }


    fun updateEmail(email: String) {
        _formData.update { it.copy(email = email, isEmailValid = Utils.isValidEmail(email)) }
    }

    fun logIntoAccount() {
        viewModelScope.launch {
            val validationResult = validateForm()
            if (validationResult != null) {
                _uiState.value = SignInUiState.Error(validationResult)
                return@launch
            }

            val result = repository.signInUser(_formData.value.email, _formData.value.password)
            _uiState.value = when (result) {
                is ResultWrapper.Success -> SignInUiState.Success
                is ResultWrapper.Error -> SignInUiState.Error(R.string.authentication_failed)
                is ResultWrapper.Loading -> SignInUiState.Loading
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

