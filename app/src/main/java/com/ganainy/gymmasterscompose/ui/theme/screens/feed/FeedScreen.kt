package com.ganainy.gymmasterscompose.ui.theme.screens.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.components.CustomProgressIndicator
import com.ganainy.gymmasterscompose.ui.theme.components.CustomSnackBar
import com.ganainy.gymmasterscompose.ui.theme.components.ToolbarWithMenu
import com.ganainy.gymmasterscompose.ui.theme.repository.AuthRepository
import com.ganainy.gymmasterscompose.ui.theme.screens.signin.SignInUiState
import kotlinx.coroutines.launch


@Composable
fun FeedScreen(
    appRepository: AuthRepository,
    navigateToSignIn: () -> Unit,
    navigateToDiscover: () -> Unit
) {

    val viewModel: FeedViewModel = viewModel(factory = FeedViewModelFactory(appRepository))
    val uiState by viewModel.uiState.collectAsState()
    val feedData by viewModel.feedData.collectAsState()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    label = { Text(text = stringResource(R.string.discover)) },
                    selected = false,
                    onClick = navigateToDiscover
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(text = stringResource(R.string.sign_out)) },
                    selected = false,
                    onClick = { viewModel.signOut() }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                ToolbarWithMenu(
                    openMenu = {
                        scope.launch {
                            drawerState.open() // Open the drawer when menu icon is clicked
                        }
                    }
                )
            },

            ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                //todo add feed content in a lazy column with box exercises and workouts
            }
        }
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