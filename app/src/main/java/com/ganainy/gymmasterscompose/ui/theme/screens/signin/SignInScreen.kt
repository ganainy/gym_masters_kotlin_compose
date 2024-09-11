package com.ganainy.gymmasterscompose.ui.theme.screens.signin


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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.components.CustomPasswordTextField
import com.ganainy.gymmasterscompose.ui.theme.components.CustomProgressIndicator
import com.ganainy.gymmasterscompose.ui.theme.components.CustomSnackBar
import com.ganainy.gymmasterscompose.ui.theme.components.CustomTextField
import com.ganainy.gymmasterscompose.ui.theme.repository.AuthRepository

@Composable
fun SignInScreen(
    appRepository: AuthRepository,
    navigateToSignUp: () -> Unit,
    navigateToFeed: () -> Unit,
    navigateBack: () -> Unit
) {
    val viewModel: SignInViewModel = viewModel(factory = SignInViewModelFactory(appRepository))
    val uiState by viewModel.uiState.collectAsState()
    val formData by viewModel.formData.collectAsState()


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
            CustomProgressIndicator()
        }

        is SignInUiState.Error -> {
            // Show error message
            CustomSnackBar(message = stringResource((uiState as SignInUiState.Error).messageStringResource)) {
            }
        }

        is SignInUiState.Success -> {
            LaunchedEffect(Unit) {
                navigateToFeed()
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
                    Text(text = stringResource(R.string.sign_up_here), color = Color.Blue)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onLogIntoAccount,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.sign_in),
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Preview
@Composable
private fun SignUpScreenPreview() {
    /*  SignUpScreenContent(

      )*/
}