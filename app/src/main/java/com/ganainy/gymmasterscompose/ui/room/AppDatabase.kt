package com.ganainy.gymmasterscompose.ui.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ganainy.gymmasterscompose.ui.models.BodyPart
import com.ganainy.gymmasterscompose.ui.models.Equipment
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.models.comment.CommentLikeDao
import com.ganainy.gymmasterscompose.ui.models.comment.CommentLikeEntity
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutEntity

@Database(entities = [Exercise::class, BodyPart::class, Equipment::class, TargetMuscle::class,
    WorkoutEntity::class,  CommentLikeEntity::class, CachedLike::class], version = 7)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun bodyPartListDao(): BodyPartListDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun targetDao(): TargetDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun commentLikeDao(): CommentLikeDao
    abstract fun cachedLikeDao(): CachedLikeDao
}