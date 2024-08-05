package com.ganainy.gymmasterscompose.ui.theme.screens.signup


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.AppConstants
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.AppUtils
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.repository.IAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class SignUpFormData(
    val uid: String = "", // id of the user in the database
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val isUsernameValid: Boolean = false,
    val isEmailValid: Boolean = false,
    val isPasswordValid: Boolean = false
)

sealed class SignUpUiState {
    object Initial : SignUpUiState()
    object Loading : SignUpUiState()
    data class Error(val messageStringResource: Int) : SignUpUiState()
    object Success : SignUpUiState() // this state means either user signed up successfully or
    // account already exists on device
}


class SignUpViewModel(private val repository: IAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Initial)
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _signUpFormData = MutableStateFlow(SignUpFormData())
    val signUpFormData: StateFlow<SignUpFormData> = _signUpFormData.asStateFlow()

    init {
        // Check if user is signed in (non-null)
        val currentUser = repository.getCurrentUser()
        if (currentUser != null) {
            // User is signed already in, redirect to feed screen
            _uiState.value = SignUpUiState.Success
        }
    }

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

        repository.createUser(
            _signUpFormData.value.email,
            _signUpFormData.value.password
        ).onSuccess {
            // createUser success, save user data in db and update ui
            val userId: String = repository.getCurrentUser()?.uid
                ?: throw Exception("authenticateWithFirebase: User ID is null")

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

    private suspend fun saveUserInfo() {

        val newUser: User = User(
            id = _signUpFormData.value.uid, name = _signUpFormData.value.username,
            email = _signUpFormData.value.email
        )

        repository.saveUserInfo(newUser).onSuccess {
            _uiState.value = SignUpUiState.Success
        }.onFailure {
            _uiState.value = SignUpUiState.Error((R.string.create_account_failed))
        }

    }


    fun updatePassword(password: String) {
        _signUpFormData.update {
            it.copy(
                password = password, isPasswordValid =
                AppUtils.isValidFieldLength(password, AppConstants.MINIMUM_PASSWORD_LENGTH)
            )
        }
    }


    fun updateUsername(username: String) {
        _signUpFormData.update {
            it.copy(
                username = username,
                isUsernameValid = AppUtils.isValidFieldLength(
                    username,
                    AppConstants.MINIMUM_USERNAME_LENGTH
                )
            )
        }
    }

    fun updateEmail(email: String) {
        _signUpFormData.update {
            it.copy(
                email = email,
                isEmailValid = AppUtils.isValidEmail(email)
            )
        }
    }


}


class SignUpViewModelFactory(private val repository: IAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SignUpViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

