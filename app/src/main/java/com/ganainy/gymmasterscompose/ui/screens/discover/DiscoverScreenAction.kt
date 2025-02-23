package com.ganainy.gymmasterscompose.ui.screens.discover

import com.ganainy.gymmasterscompose.ui.models.User

sealed interface DiscoverScreenAction {
    data class NavigateToProfile(val userId: String?) : DiscoverScreenAction
    data class UpdateSearchQuery(val searchQuery: String) : DiscoverScreenAction
    data class ToggleFollowUser(val user: User) : DiscoverScreenAction
    object Refresh : DiscoverScreenAction
}
