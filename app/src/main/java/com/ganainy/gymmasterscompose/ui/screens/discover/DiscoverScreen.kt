package com.ganainy.gymmasterscompose.ui.screens.discover

import CustomSearchBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.models.User
import com.ganainy.gymmasterscompose.ui.shared_components.CustomTopAppBar
import com.ganainy.gymmasterscompose.ui.shared_components.ErrorComponent
import com.ganainy.gymmasterscompose.ui.shared_components.LoadingIndicator

@Composable
fun DiscoverScreen(navigateToProfile: (String?) -> Unit) {



    val viewModel: DiscoverViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val discoverData by viewModel.discoverData.collectAsState()


    DiscoverUserList( discoverData, uiState, onAction = {action -> when (action) {
        is DiscoverScreenAction.NavigateToProfile -> navigateToProfile(action.userId)
        is DiscoverScreenAction.UpdateSearchQuery -> viewModel.onSearchQueryChanged(action.searchQuery)
        is DiscoverScreenAction.ToggleFollowUser -> viewModel.followUnfollowUser(action.user)
        is DiscoverScreenAction.Refresh -> viewModel.refresh()
    }
    })

}


@Composable
private fun DiscoverUserList(
    discoverData: DiscoverData,
    uiState: DiscoverUiState,
    onAction: (DiscoverScreenAction) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CustomTopAppBar(
            title = stringResource(R.string.discover),
            actionIcons = listOf(Icons.Default.Refresh),
            onActionClicks = listOf { onAction(DiscoverScreenAction.Refresh) },
                   )

        CustomSearchBar(
            hint = stringResource(id = R.string.search_for_a_user),
            onQueryChange = { query -> onAction(DiscoverScreenAction.UpdateSearchQuery(query)) },
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
                    DiscoverUserList(
                        discoverData,
                        onAction = onAction,)
                }

                is DiscoverUiState.Error.StringError -> ErrorComponent(
                    text = (uiState as DiscoverUiState.Error.StringError).message
                )
            }
        }

    }
}



@Preview(showBackground = true)
@Composable
private fun DiscoverUserListPreview() {
    val previewData = DiscoverData(
        users = listOf(
            UserWithFollowStatus(
                user = User(
                    id = "1",
                    displayName = "John Doe",
                    profilePictureUrl = "",
                    bio = "Fitness enthusiast"
                ),
                isFollowing = true
            ),
            UserWithFollowStatus(
                user = User(
                    id = "2",
                    displayName = "Jane Smith",
                    profilePictureUrl = "",
                    bio = "Personal trainer"
                ),
                isFollowing = false
            )
        )
    )

    DiscoverUserList(
        discoverData = previewData,
        uiState = DiscoverUiState.Success,
        onAction = {}
    )
}

@Composable
private fun DiscoverUserList(
    discoverData: DiscoverData,
    onAction: (DiscoverScreenAction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(discoverData.users) { userWithFollowState ->
            DiscoverProfile(
                user = userWithFollowState.user,
                onFollowClick = { onAction(DiscoverScreenAction.ToggleFollowUser(userWithFollowState.user)) },
                isFollowedByLocalUser = userWithFollowState.isFollowing,
                onProfileClick = { onAction(DiscoverScreenAction.NavigateToProfile(userWithFollowState.user.id)) }
            )
            if (discoverData.users.last() != userWithFollowState)
            HorizontalDivider()
        }
    }
}



