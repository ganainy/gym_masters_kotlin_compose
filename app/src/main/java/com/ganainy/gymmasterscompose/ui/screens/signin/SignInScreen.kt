package com.ganainy.gymmasterscompose.ui.screens.signin


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.shared_components.CustomPasswordTextField
import com.ganainy.gymmasterscompose.ui.shared_components.CustomTextField
import com.ganainy.gymmasterscompose.ui.shared_components.LoadingIndicator
import com.ganainy.gymmasterscompose.utils.Utils.showToast

@Composable
fun SignInScreen(
    navigateToSignUp: () -> Unit,
    onSignInSuccess: () -> Unit,
) {
    val viewModel: SignInViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val formData by viewModel.formData.collectAsState()

    val context = LocalContext.current

    // Show sign-in form with current input and validation states
    SignInScreenContent(
        formData,
        onLogIntoAccount = { -> viewModel.logIntoAccount() },
        onNavigateToSignUp = navigateToSignUp,
        onEmailChange = { email -> viewModel.updateEmail(email) },
        onPasswordChange = { password -> viewModel.updatePassword(password) },
    )


    when (uiState) {
        is SignInUiState.Initial -> {
            // Show initial state (empty form)
        }

        is SignInUiState.Loading -> {
            // Show loading indicator
            LoadingIndicator()
        }

        is SignInUiState.Error -> {
            // Show error message
            val errorMessage= stringResource((uiState as SignInUiState.Error).messageStringResource)
            showToast(context,
                errorMessage,
            )
        }

        is SignInUiState.Success -> {
            LaunchedEffect(Unit) {
                onSignInSuccess()
            }
        }
    }
}


@Composable
fun SignInScreenContent(
    formData: SignInFormData,
    onLogIntoAccount: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.sign_in),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.welcome_back_to_gym_masters),
                color = Color.Gray,
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.height(32.dp))

            CustomTextField(
                text = formData.email, label = stringResource(R.string.email),
                isError = !formData.isEmailValid,
                errorText = stringResource(R.string.email_invalid_format),
                options = KeyboardOptions(keyboardType = KeyboardType.Email),
                onValueChange = onEmailChange
            )

            CustomPasswordTextField(
                text = formData.password, label = stringResource(R.string.password),
                isError = !formData.isPasswordValid,
                errorText = stringResource(R.string.password_must_be_atleast_six_characters_long),
                onValueChange = onPasswordChange
            )


            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.want_to_create_account),
                    fontSize = 14.sp,
                )
                TextButton(onClick = onNavigateToSignUp) {
                    Text(text = stringResource(R.string.sign_up_here))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onLogIntoAccount,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.sign_in),
                    fontSize = 24.sp
                )
            }
        }
    }
}

