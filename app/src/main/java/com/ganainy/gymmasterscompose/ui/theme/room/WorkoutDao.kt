package com.ganainy.gymmasterscompose.ui.theme.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutEntity

// Room DAO for WorkoutEntity
@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts")
    suspend fun getAllWorkouts(): List<WorkoutEntity>

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkoutById(workoutId: String)

    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()
}
