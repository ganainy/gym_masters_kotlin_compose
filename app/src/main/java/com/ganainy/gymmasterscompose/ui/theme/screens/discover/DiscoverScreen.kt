package com.ganainy.gymmasterscompose.ui.theme.screens.discover

import CustomSearchBar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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

@Composable
fun DiscoverScreen(dataRepository: DataRepository) {

    val viewModel: DiscoverViewModel = viewModel(factory = DiscoverViewModelFactory(dataRepository))
    val uiState by viewModel.uiState.collectAsState()
    val discoverData by viewModel.discoverData.collectAsState()


    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        CustomSearchBar(
            onQueryChange = { query -> viewModel.onQueryChange(query) },
            searchQuery = discoverData.searchQuery
        )

        Box(modifier = Modifier.padding(8.dp)) {
            when (uiState) {
                is DiscoverUiState.Loading -> {
                    CustomProgressIndicator()
                }

                is DiscoverUiState.Error -> {
                    CustomSnackBar(
                        message = stringResource((uiState as DiscoverUiState.Error).messageStringResource)
                    ) {
                        // Add any action for the SnackBar if needed
                    }
                }

                is DiscoverUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(discoverData.users) { item ->
                            DiscoverProfile(
                                item,
                                { viewModel.followUnfollowUser(item) },
                                viewModel.isFollowedByLoggedUser(item)
                            )
                        }
                    }
                }
            }
        }

    }
}
