package com.ganainy.gymmasterscompose.ui.theme.models

data class Reactions(
    val id: String,
    val reactions: Map<String, Reaction>
)

data class Reaction(
    val userId: String,
    val type: String,
    val timestamp: Long // Timestamp
)