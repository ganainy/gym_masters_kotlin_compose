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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ganainy.gymmasterscompose.ui.theme.components.ErrorComponent
import com.ganainy.gymmasterscompose.ui.theme.components.LoadingIndicator

@Composable
fun DiscoverScreen(navContoller: NavHostController) {


    //navigation actions
    fun navigateToProfile(userId: String?) {
        navContoller.navigate("profile/$userId")
    }


    val viewModel: DiscoverViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val discoverData by viewModel.discoverData.collectAsState()


    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        CustomSearchBar(
            onQueryChange = { query -> viewModel.onSearchQueryChanged(query) },
            searchQuery = discoverData.searchQuery
        )

        Box(modifier = Modifier.padding(8.dp)) {
            when (uiState) {
                is DiscoverUiState.Loading -> {
                    LoadingIndicator()
                }

                is DiscoverUiState.Error.IntError -> {
                    ErrorComponent(
                        text = stringResource((uiState as DiscoverUiState.Error.IntError).messageStringResource)
                    )
                }

                is DiscoverUiState.Success -> {
                    DiscoverScreenContent(discoverData, viewModel, navigateToProfile = {navigateToProfile(it)})
                }

                is DiscoverUiState.Error.StringError ->  ErrorComponent(
                    text = (uiState as DiscoverUiState.Error.StringError).message
                )
            }
        }

    }
}

@Composable
private fun DiscoverScreenContent(
    discoverData: DiscoverData,
    viewModel: DiscoverViewModel,
    navigateToProfile: (String?) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(discoverData.users) { userWithFollowState ->
            DiscoverProfile(
                user = userWithFollowState.user,
                onFollowClick = { userWithFollowState.user.let { viewModel.followUnfollowUser(it) } },
                isCurrentUserFollowing = userWithFollowState.isFollowing,
                onProfileClick = { navigateToProfile(userWithFollowState.user.id) }
            )
        }
    }
}

