package com.ganainy.gymmasterscompose.ui.screens.signup


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


data class SignUpFormData(
    val uid: String = "", // id of the user in the database
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isUsernameValid: Boolean = false,
    val isEmailValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val username: String? = null
) {
}

sealed class SignUpUiState {
    object Initial : SignUpUiState()
    object Loading : SignUpUiState()
    data class Error(val messageStringResource: Int) : SignUpUiState()
    object Success : SignUpUiState() // this state means either user signed up successfully or
    // account already exists on device
}

@HiltViewModel
class SignUpViewModel @Inject constructor(private val authRepository: AuthRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Initial)
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _signUpFormData = MutableStateFlow(SignUpFormData())
    val signUpFormData: StateFlow<SignUpFormData> = _signUpFormData.asStateFlow()

    fun createAccount() {
        _uiState.value = SignUpUiState.Loading
        viewModelScope.launch {
            when {
                !_signUpFormData.value.isUsernameValid ->
                    _uiState.value = SignUpUiState.Error(R.string.invalid_username)

                !_signUpFormData.value.isEmailValid ->
                    _uiState.value = SignUpUiState.Error(R.string.invalid_email)

                !_signUpFormData.value.isPasswordValid ->
                    _uiState.value = SignUpUiState.Error(R.string.invalid_password)

                else -> authenticateWithFirebase()
            }
        }
    }

    private suspend fun authenticateWithFirebase() {

        authRepository.createUserAuth(
            _signUpFormData.value.email,
            _signUpFormData.value.password
        ).onSuccess {
            // createUser success, save user data in db and update ui
            val userId: String = authRepository.getCurrentUserId()
                ?: throw Exception("authenticateWithFirebase: com.ganainy.gymmasterscompose.ui.theme.models.User ID is null")

            _signUpFormData.update {
                it.copy(
                    uid = userId
                )
            }
            saveUserInfo()
        }.onFailure {
            //  sign up fails
            _uiState.value = SignUpUiState.Error((R.string.authentication_failed))
        }
    }

    private suspend fun saveUserInfo(): ResultWrapper<Unit> {
        return authRepository.createUserProfile(
            email = _signUpFormData.value.email,
            displayName = _signUpFormData.value.displayName
        ).let {
            if (it is ResultWrapper.Success) {
                _uiState.value = SignUpUiState.Success
                ResultWrapper.Success(Unit)
            } else {
                _uiState.value = SignUpUiState.Error((R.string.create_account_failed))
                ResultWrapper.Error(Exception("saveUserInfo: failed to save user info"))
            }
        }
    }

    fun updatePassword(password: String) {
        _signUpFormData.update {
            it.copy(
                password = password, isPasswordValid =
                Utils.isValidFieldLength(password, Constants.MINIMUM_PASSWORD_LENGTH)
            )
        }
    }


    fun updateDisplayName(displayName: String) {
        _signUpFormData.update {
            it.copy(
                displayName = displayName,
                isUsernameValid = Utils.isValidFieldLength(
                    displayName,
                    Constants.MINIMUM_NAME_LENGTH
                )
            )
        }
    }

    fun updateEmail(email: String) {
        _signUpFormData.update {
            it.copy(
                email = email,
                isEmailValid = Utils.isValidEmail(email)
            )
        }
    }


}


