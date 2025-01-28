package com.ganainy.gymmasterscompose.ui.theme.repository

import android.util.Log
import com.ganainy.gymmasterscompose.Constants
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.Workout
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Workout and exercise management
interface IWorkoutRepository {
    suspend fun getExercise(exerciseId: String): Exercise?
    suspend fun getWorkout(workoutId: String): Workout?
    suspend fun createWorkout(workout: Workout): ResultWrapper<Unit>
    suspend fun createExercise(exercise: Exercise): ResultWrapper<Unit>
    suspend fun uploadWorkout(workout: Workout): ResultWrapper<Unit>
}

class WorkoutRepository @Inject constructor(private val  database: FirebaseDatabase) : IWorkoutRepository{

    override suspend fun getExercise(exerciseId: String): Exercise? {
        return try {
            val exerciseRef = database.getReference(Constants.EXERCISES).child(exerciseId)
            val snapshot = exerciseRef.get().await()
            if (snapshot.exists()) {
                snapshot.getValue(Exercise::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("DataRepository", "Error fetching exercise: ${e.message}")
            null
        }
    }

    override suspend fun getWorkout(workoutId: String): Workout? {
        return try {
            val workoutRef = database.getReference(Constants.WORKOUTS).child(workoutId)
            val snapshot = workoutRef.get().await()
            if (snapshot.exists()) {
                snapshot.getValue(Workout::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("DataRepository", "Error fetching workout: ${e.message}")
            null
        }
    }

    override suspend fun createWorkout(workout: Workout): ResultWrapper<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun createExercise(exercise: Exercise): ResultWrapper<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadWorkout(workout: Workout): ResultWrapper<Unit> {
        return try {
            val workoutRef = database.getReference(Constants.WORKOUTS).child(workout.workoutId)
            workoutRef.setValue(workout).await()
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

}

