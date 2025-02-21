package com.ganainy.gymmasterscompose.ui.theme.screens.discover

import com.ganainy.gymmasterscompose.ui.theme.models.User

sealed interface DiscoverScreenAction {
    data class NavigateToProfile(val userId: String?) : DiscoverScreenAction
    data class UpdateSearchQuery(val searchQuery: String) : DiscoverScreenAction
    data class ToggleFollowUser(val user: User) : DiscoverScreenAction
    object Refresh : DiscoverScreenAction
}
