package com.ganainy.gymmasterscompose.ui.theme.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "exercise")
@Parcelize
data class Exercise(
    val bodyPart: String,
    val equipment: String,
    val gifUrl: String,
    @PrimaryKey val id: String,
    val name: String,
    val target: String,
    val secondaryMuscles: List<String>,
    val instructions: List<String>,
    var screenshotPath: String? = null // the path of a screenshot of the gif on the device
) : Parcelable


// 0:"back"
// 1:"cardio"
// 2:"chest"
// 3:"lower arms"
// 4:"lower legs"
// 5:"neck"
// 6:"shoulders"
// 7:"upper arms"
// 8:"upper legs"
// 9:"waist"
@Entity(tableName = "body_part_list")
data class BodyPart(
    @PrimaryKey val name: String
)


// 0:"assisted"
// 1:"band"
// 2:"barbell"
// 3:"body weight"
// 4:"bosu ball"
// 5:"cable"
// 6:"dumbbell"
// 7:"elliptical machine"
// 8:"ez barbell"
// 9:"hammer"
// 10:"kettlebell"
// 11:"leverage machine"
// 12:"medicine ball"
// 13:"olympic barbell"
// 14:"resistance band"
// 15:"roller"
// 16:"rope"
// 17:"skierg machine"
// 18:"sled machine"
// 19:"smith machine"
// 20:"stability ball"
// 21:"stationary bike"
// 22:"stepmill machine"
// 23:"tire"
// 24:"trap bar"
// 25:"upper body ergometer"
// 26:"weighted"
// 27:"wheel roller"

@Entity(tableName = "equipment")
data class Equipment(
    @PrimaryKey val name: String
)



// 0:"abductors"
// 1:"abs"
// 2:"adductors"
// 3:"biceps"
// 4:"calves"
// 5:"cardiovascular system"
// 6:"delts"
// 7:"forearms"
// 8:"glutes"
// 9:"hamstrings"
// 10:"lats"
// 11:"levator scapulae"
// 12:"pectorals"
// 13:"quads"
// 14:"serratus anterior"
// 15:"spine"
// 16:"traps"
// 17:"triceps"
// 18:"upper back"

@Entity(tableName = "targets")
data class TargetMuscle(
    @PrimaryKey val name: String
)

