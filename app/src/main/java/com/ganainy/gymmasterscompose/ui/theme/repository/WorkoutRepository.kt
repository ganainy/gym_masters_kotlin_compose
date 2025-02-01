package com.ganainy.gymmasterscompose.ui.theme.repository

import android.net.Uri
import android.util.Log
import com.ganainy.gymmasterscompose.Constants
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.Workout.Companion.WORKOUTS
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

class WorkoutRepository @Inject constructor(
    private val database: FirebaseDatabase, private val storage: FirebaseStorage,
    private val hashtagRepository: IHashtagRepository
) : IWorkoutRepository {

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
            val workoutRef = database.getReference(WORKOUTS).child(workoutId)
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


    /**
     * Uploads a workout to the Firebase database.
     *
     * This function uploads the given workout to the Firebase database. It also updates the usage count
     * for existing hashtags and creates new hashtags if necessary.
     *
     * @param workout The workout to be uploaded.
     * @return A `ResultWrapper` containing `Unit` if the upload is successful, or an error if it fails.
     */
    override suspend fun uploadWorkout(workout: Workout): ResultWrapper<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "$WORKOUTS/${workout.id}" to workout
            )

            // Check if the workout has tags
            // If tags exist, increment the use count and update the last used timestamp
            // Otherwise, create new hashtags
            updates.putAll(hashtagRepository.updateHashtags(workout.tags))

            database.reference.updateChildren(updates).await()
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    override suspend fun uploadWorkoutCoverImage(imagePath: String): ResultWrapper<String> {
        val imageRef = storage.reference.child(Constants.WORKOUT_COVER_IMAGES)
            .child(generateRandomId(Constants.COVER_IMAGE))
        return try {
            imageRef.putFile(Uri.parse(imagePath)).await()
            ResultWrapper.Success(imageRef.downloadUrl.await().toString())
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }
}

