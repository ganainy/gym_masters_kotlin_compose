package com.ganainy.gymmasterscompose.ui.theme.models

data class Reactions(
    val id: String = "", // id of the exercise / workout
    val reactions: Map<String, Reaction> = emptyMap() // Map of user ID to their reaction
)

data class Reaction(
    val userId: String = "",
    val type: ReactionType = ReactionType.LIKE,
    val timestamp: Long = 0L
)

enum class ReactionType {
    LIKE,
    FIRE,
    HUNDRED,
    CLAP,
    MUSCLE,
}