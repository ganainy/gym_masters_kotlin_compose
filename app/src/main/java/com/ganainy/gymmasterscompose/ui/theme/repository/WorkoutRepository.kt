package com.ganainy.gymmasterscompose.ui.theme.repository

import android.net.Uri
import android.util.Log
import com.ganainy.gymmasterscompose.Constants
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.Workout
import com.ganainy.gymmasterscompose.utils.Utils.generateRandomId
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Workout and exercise management
interface IWorkoutRepository {
    suspend fun getExercise(exerciseId: String): Exercise?
    suspend fun getWorkout(workoutId: String): Workout?
    suspend fun uploadWorkout(workout: Workout): ResultWrapper<Unit>
    suspend fun uploadWorkoutCoverImage(imagePath: String): ResultWrapper<String>
}

class WorkoutRepository @Inject constructor(private val  database: FirebaseDatabase, private val storage: FirebaseStorage) : IWorkoutRepository{

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


    override suspend fun uploadWorkout(workout: Workout): ResultWrapper<Unit> {
        return try {
            val workoutRef = database.getReference(Constants.WORKOUTS).child(workout.workoutId)
            workoutRef.setValue(workout).await()
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    override suspend fun uploadWorkoutCoverImage(imagePath: String): ResultWrapper<String> {
        val imageRef = storage.reference.child(Constants.WORKOUT_COVER_IMAGES).child(generateRandomId(Constants.COVER_IMAGE))
        return try {
            imageRef.putFile(Uri.parse(imagePath)).await()
            ResultWrapper.Success(imageRef.downloadUrl.await().toString())
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }
}

