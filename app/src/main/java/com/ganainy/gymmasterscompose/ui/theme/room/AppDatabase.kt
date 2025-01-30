package com.ganainy.gymmasterscompose.ui.theme.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ganainy.gymmasterscompose.ui.theme.models.BodyPart
import com.ganainy.gymmasterscompose.ui.theme.models.Equipment
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.UserStats
import com.ganainy.gymmasterscompose.ui.theme.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.theme.models.User

@Database(entities = [Exercise::class, BodyPart::class, Equipment::class, TargetMuscle::class, User::class, UserStats::class], version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun bodyPartListDao(): BodyPartListDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun targetDao(): TargetDao
    abstract fun userDao(): UserDao
    abstract fun statsDao(): StatsDao
}