package com.ganainy.gymmasterscompose.ui.theme.screens.discover

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganainy.gymmasterscompose.ui.theme.components.CustomProgressIndicator
import com.ganainy.gymmasterscompose.ui.theme.components.CustomSnackBar
import com.ganainy.gymmasterscompose.ui.theme.components.DiscoverProfile
import com.ganainy.gymmasterscompose.ui.theme.repository.DataRepository
import com.ganainy.gymmasterscompose.ui.theme.screens.signin.SignInUiState

@Composable
fun DiscoverScreen(dataRepository: DataRepository) {

    val viewModel: DiscoverViewModel = viewModel(factory = DiscoverViewModelFactory(dataRepository))
    val uiState by viewModel.uiState.collectAsState()
    val discoverData by viewModel.discoverData.collectAsState()

    when (uiState) {

        is DiscoverUiState.Loading -> {
            // Show loading indicator
            CustomProgressIndicator()
        }

        is DiscoverUiState.Error -> {
            // Show error message
            CustomSnackBar(message = stringResource((uiState as SignInUiState.Error).messageStringResource)) {
            }
        }

        is DiscoverUiState.Success -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(discoverData.users) { item ->
                    DiscoverProfile(item)
                }
            }
        }

    }

}