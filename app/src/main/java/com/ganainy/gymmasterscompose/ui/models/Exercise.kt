package com.ganainy.gymmasterscompose.ui.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Entity(tableName = "exercise")
@Serializable
@Parcelize
data class Exercise(
    val bodyPart: String = "",
    val equipment: String = "",
    val gifUrl: String = "",
    @PrimaryKey val id: String = "",
    val name: String = "",
    val target: String = "",
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    var screenshotPath: String? = null, // the path of a screenshot of the gif on the device,
    // api doesn't provide an exercise image, so we take the exercise gif and extract an image from it
    var isSavedLocally: Boolean = false // if the user saved this exercise locally
) : Parcelable


@Entity(tableName = "body_part_list")
data class BodyPart(
    @PrimaryKey val name: String
)


@Entity(tableName = "equipment")
data class Equipment(
    @PrimaryKey val name: String
)


@Entity(tableName = "targets")
data class TargetMuscle(
    @PrimaryKey val name: String
)

