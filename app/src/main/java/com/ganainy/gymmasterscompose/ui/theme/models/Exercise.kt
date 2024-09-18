package com.ganainy.gymmasterscompose.ui.theme.models

data class Exercise(
    val exerciseId: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val muscleGroup: String = "",
    val difficulty: String = "",
    val imageUrl: String = "",
    val imageUrl2: String = "",
    val dateCreated: Long = 0L,
    val additionalNotes: String? = null
)




