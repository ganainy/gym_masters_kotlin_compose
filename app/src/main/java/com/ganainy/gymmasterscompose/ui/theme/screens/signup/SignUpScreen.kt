package com.ganainy.gymmasterscompose.ui.theme.screens.signup


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
import androidx.compose.runtime.collectAsState
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
fun SignUpScreen(
    appRepository: AuthRepository,
    navigateToSignIn: () -> Unit,
    navigateToFeed: () -> Unit,
    navigateBack: () -> Unit
) {
    val viewModel: SignUpViewModel = viewModel(factory = SignUpViewModelFactory(appRepository))
    val uiState = viewModel.uiState.collectAsState().value
    val signUpFormData = viewModel.signUpFormData.collectAsState().value



    SignUpScreenContent(
        signUpFormData,
        onCreateAccount = { ->
            viewModel.createAccount()
        },
        onSignInClick = navigateToSignIn,
        onUpdateEmail = { email -> viewModel.updateEmail(email) },
        onUpdatePassword = { password -> viewModel.updatePassword(password) },
        onUpdateUsername = { username -> viewModel.updateUsername(username) },
        onNavigateBack = navigateBack
    )

    if (uiState is SignUpUiState.Loading) {
        CustomProgressIndicator()
    } else if (uiState is SignUpUiState.Error) {
        val errorMessage = stringResource(uiState.messageStringResource)
        CustomSnackBar(errorMessage, {})
    } else if (uiState is SignUpUiState.Success) {
        navigateToFeed()
    }


}


@Composable
fun SignUpScreenContent(
    signUpFormData: SignUpFormData,
    onCreateAccount: () -> Unit,
    onUpdateUsername: (String) -> Unit,
    onUpdateEmail: (String) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onSignInClick: () -> Unit,
    onNavigateBack: () -> Unit,
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
                text = stringResource(R.string.create_account),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.welcome_to_gym_masters_where_you_can_create_and_share_custom_exercises_and_workouts_and_see_other_users_unique_creations),
                color = Color.Gray,
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.height(32.dp))


            CustomTextField(
                text = signUpFormData.username, label = stringResource(R.string.user_name),
                isError = !signUpFormData.isUsernameValid,
                errorText = stringResource(R.string.username_cannot_be_empty),
                onValueChange = onUpdateUsername,
            )


            CustomTextField(
                text = signUpFormData.email, label = stringResource(R.string.email),
                isError = !signUpFormData.isEmailValid,
                errorText = stringResource(R.string.email_invalid_format),
                options = KeyboardOptions(keyboardType = KeyboardType.Email),
                onValueChange = onUpdateEmail,
            )



            CustomPasswordTextField(
                text = signUpFormData.password, label = stringResource(R.string.password),
                isError = !signUpFormData.isPasswordValid,
                errorText = stringResource(R.string.password_must_be_atleast_six_characters_long),
                onValueChange = onUpdatePassword,
            )


            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.already_using_gym_masters),
                    fontSize = 14.sp,
                )
                TextButton(onClick = onSignInClick) {
                    Text(text = stringResource(R.string.sign_in_here), color = Color.Blue)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onCreateAccount() },
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
                    text = stringResource(R.string.create_account),
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Preview
@Composable
private fun SignUpScreenPreview() {
    /* SignUpScreenContent(
     )*/
}