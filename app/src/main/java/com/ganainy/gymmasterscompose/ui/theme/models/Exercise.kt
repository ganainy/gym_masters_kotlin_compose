package com.ganainy.gymmasterscompose.ui.theme.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Entity(tableName = "exercise")
@Serializable
data class Exercise(
    val bodyPart: String = "",
    val equipment: String= "",
    val gifUrl: String= "",
    @PrimaryKey val id: String= "",
    val name: String= "",
    val target: String= "",
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: List<String>  = emptyList(),
    var screenshotPath: String? = null // the path of a screenshot of the gif on the device
)



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

