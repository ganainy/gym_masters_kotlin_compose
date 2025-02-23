package com.ganainy.gymmasterscompose.ui.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutEntity
import kotlinx.coroutines.flow.Flow

// Room DAO for WorkoutEntity
@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts")
    suspend fun getAllWorkouts(): List<WorkoutEntity>

    @Query("SELECT * FROM workouts")
     fun getAllWorkoutsFlow(): Flow<List<WorkoutEntity>>

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkoutById(workoutId: String)

    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()
}
