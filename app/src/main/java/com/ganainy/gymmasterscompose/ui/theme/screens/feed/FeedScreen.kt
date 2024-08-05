package com.ganainy.gymmasterscompose.ui.theme.screens.feed

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganainy.gymmasterscompose.ui.theme.components.CustomProgressIndicator
import com.ganainy.gymmasterscompose.ui.theme.components.CustomSnackBar
import com.ganainy.gymmasterscompose.ui.theme.repository.AppRepository
import com.ganainy.gymmasterscompose.ui.theme.screens.signin.SignInUiState


@Composable
fun FeedScreen(appRepository: AppRepository, navigateToSignIn: () -> Unit) {

    val viewModel: FeedViewModel = viewModel(factory = FeedViewModelFactory(appRepository))
    val uiState by viewModel.uiState.collectAsState()
    val feedData by viewModel.feedData.collectAsState()

    Row {
        Text(
            text = "FeedScreen",
        )
        Button(onClick = { viewModel.signOut() }) { Text(text = "sign out") }
    }

    when (uiState) {
        is FeedUiState.Initial -> {
            // Show initial state (empty form)
        }

        is FeedUiState.Loading -> {
            // Show loading indicator
            CustomProgressIndicator()
        }

        is FeedUiState.Error -> {
            // Show error message
            CustomSnackBar(message = stringResource((uiState as SignInUiState.Error).messageStringResource)) {
            }
        }

        is FeedUiState.LogoutSuccess -> {
            LaunchedEffect(Unit) {
                navigateToSignIn()
            }
        }
    }

}