package com.ganainy.gymmasterscompose.ui.theme.repository

import android.net.Uri
import android.util.Log
import com.ganainy.gymmasterscompose.Constants
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutEntity.Companion.WORKOUTS_COLLECTION
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutEntity.Companion.WORKOUTS_METRICS
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutEntity.Companion.WORKOUTS_TIMESTAMP
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutEntity.Companion.WORKOUT_LIKES_COUNT
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutEntity.Companion.WORKOUT_SAVE_COUNT
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutLike
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutLike.Companion.WORKOUT_LIKES_COLLECTION
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutSave
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutSave.Companion.WORKOUT_SAVES_COLLECTION
import com.ganainy.gymmasterscompose.ui.theme.models.workout.toWorkout
import com.ganainy.gymmasterscompose.ui.theme.models.workout.toWorkoutEntity
import com.ganainy.gymmasterscompose.ui.theme.room.AppDatabase
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.SortType
import com.ganainy.gymmasterscompose.utils.Utils.generateRandomId
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Workout and exercise management
interface IWorkoutRepository {
    suspend fun getWorkout(workoutId: String): Workout?
    suspend fun uploadWorkoutWithImage(workout: Workout, imagePath: String): ResultWrapper<Unit>
    suspend fun deleteWorkout(workoutId: String): ResultWrapper<Unit>
    suspend fun isWorkoutLikedByUser(workoutId: String, userId: String): ResultWrapper<Boolean>
    suspend fun isWorkoutSavedByUser(workoutId: String, userId: String): ResultWrapper<Boolean>
    suspend fun toggleWorkoutSave(workout: Workout, userId: String): ResultWrapper<Unit>
    suspend fun getWorkouts(sortType: SortType, limit: Int = 10): Flow<ResultWrapper<List<Workout>>>
    suspend fun deleteWorkoutCoverImage(imageUrl: String): ResultWrapper<Unit>
    suspend fun saveWorkoutLocally(workout: Workout): ResultWrapper<Unit>
    suspend fun deleteWorkoutLocally(workoutId: String): ResultWrapper<Unit>
    suspend fun getLocalWorkouts(): ResultWrapper<List<Workout>>
    suspend fun getLocalWorkoutsFlow(): Flow<ResultWrapper<List<Workout>>>
    fun getWorkoutFlow(workoutId: String): Flow<ResultWrapper<Workout?>>
}

class WorkoutRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val appDatabase: AppDatabase,
    private val hashtagRepository: IHashtagRepository
) : IWorkoutRepository {


    /**
     * Fetches a workout from the Firebase database.
     *
     * This function retrieves a workout by its ID from the Firebase database. If the workout exists,
     * it returns the workout object; otherwise, it returns null.
     *
     * @param workoutId The ID of the workout to be fetched.
     * @return The workout object if it exists, or null if it does not exist.
     */
    override suspend fun getWorkout(workoutId: String): Workout? {
        return try {
            val workoutRef = database.getReference(WORKOUTS_COLLECTION).child(workoutId)
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
     * Fetches a workout from the Firebase database and listens to updates.
     *
     * This function retrieves a workout by its ID from the Firebase database and listens to updates.
     * If the workout exists, it emits the workout object; otherwise, it emits null. If the workout is
     * updated, it emits the updated workout object.
     *
     * @param workoutId The ID of the workout to be fetched.
     * @return A Flow emitting the workout object if it exists, or null if it does not exist. The Flow
     * will keep emitting the updated workout object as long as the workout is updated.
     */
    override fun getWorkoutFlow(workoutId: String): Flow<ResultWrapper<Workout?>> = callbackFlow {
        val workoutRef = database.getReference(WORKOUTS_COLLECTION).child(workoutId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    trySend(ResultWrapper.Success(snapshot.getValue(Workout::class.java)))
                } else {
                    trySend(ResultWrapper.Success(null))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(ResultWrapper.Error(Exception(error.message)))
            }
        }
        workoutRef.addValueEventListener(listener)
        awaitClose { workoutRef.removeEventListener(listener) }
    }

    /**
     * Uploads a workout along with an optional cover image to the Firebase database.
     *
     * This function first uploads the cover image if the image path is provided, then updates the workout
     * object with the image URL. It then creates a map of updates for the database, including the workout
     * and its associated hashtags, and performs the database update.
     *
     * @param workout The workout object to be uploaded.
     * @param imagePath The file path of the cover image to be uploaded. If empty, no image will be uploaded.
     * @return A ResultWrapper indicating the success or failure of the operation.
     */
    override suspend fun uploadWorkoutWithImage(
        workout: Workout,
        imagePath: String
    ): ResultWrapper<Unit> {
        return try {
            // Handle image upload if path is provided
            val workoutWithImage = if (imagePath.isNotEmpty()) {
                when (val imageResult = uploadWorkoutCoverImage(imagePath)) {
                    is ResultWrapper.Success -> workout.copy(imageUrl = imageResult.data)
                    is ResultWrapper.Error -> return ResultWrapper.Error(imageResult.exception)
                    else -> return ResultWrapper.Error(Exception("Unknown error during image upload"))
                }
            } else {
                workout
            }

            // Create updates map for the database
            val updates = mutableMapOf<String, Any>(
                "$WORKOUTS_COLLECTION/${workoutWithImage.id}" to workoutWithImage
            )

            // Update hashtags
            updates.putAll(hashtagRepository.updateHashtags(workoutWithImage.tags))

            // Perform the database update
            database.reference.updateChildren(updates).await()
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    /**
     * Uploads a workout cover image to Firebase Storage.
     *
     * This function uploads a workout cover image to Firebase Storage and returns the download URL of the uploaded image.
     * If the upload fails, it returns an error.
     *
     * @param imagePath The local file path of the cover image to be uploaded.
     * @return A ResultWrapper containing the download URL of the uploaded image if successful, or an error if the operation fails.
     */
    private suspend fun uploadWorkoutCoverImage(imagePath: String): ResultWrapper<String> {
        val imageRef = storage.reference.child(Constants.WORKOUT_COVER_IMAGES)
            .child(generateRandomId(Constants.COVER_IMAGE))
        return try {
            imageRef.putFile(Uri.parse(imagePath)).await()
            ResultWrapper.Success(imageRef.downloadUrl.await().toString())
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }


    /**
     * Deletes a workout and all its related data including likes, saves, and cover image
     * @param workoutId The ID of the workout to delete
     * @return ResultWrapper indicating success or failure
     */
    override suspend fun deleteWorkout(workoutId: String): ResultWrapper<Unit> {
        return try {
            // Get workout data first to access tags and image URL
            val workoutSnapshot = database.reference.child(WORKOUTS_COLLECTION).child(workoutId).get().await()
            val workout = workoutSnapshot.getValue(Workout::class.java) ?: return ResultWrapper.Error(
                Exception("Workout not found")
            )

            val updates = mutableMapOf<String, Any?>()

            // Delete workout
            updates["$WORKOUTS_COLLECTION/$workoutId"] = null

            // Delete likes and saves
            updates["$WORKOUT_LIKES_COLLECTION/$workoutId"] = null
            updates["$WORKOUT_SAVES_COLLECTION/$workoutId"] = null

            // Update hashtags
            updates.putAll(hashtagRepository.deleteOrDecreaseHashtags(workout.tags))

            // Execute all database updates
            database.reference.updateChildren(updates).await()

            // Delete cover image if exists
            if (workout.imageUrl.isNotEmpty()) {
                deleteWorkoutCoverImage(workout.imageUrl)
            }

            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }


    /**
     * Deletes a workout's cover image from storage
     * @param imageUrl The URL of the image to delete
     * @return ResultWrapper indicating success or failure
     */
    override suspend fun deleteWorkoutCoverImage(imageUrl: String): ResultWrapper<Unit> {
        return try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }


    /**
     * Checks if a workout is liked by a specific user
     * @param workoutId The ID of the workout to check
     * @param userId The ID of the user
     * @return ResultWrapper containing boolean indicating if workout is liked
     */
    override suspend fun isWorkoutLikedByUser(workoutId: String, userId: String): ResultWrapper<Boolean> {
        return try {
            val likeSnapshot = database.reference
                .child(WORKOUT_LIKES_COLLECTION)
                .child(WorkoutLike.createId(userId, workoutId))
                .get()
                .await()

            ResultWrapper.Success(likeSnapshot.exists())
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    /**
     * Checks if a workout is saved by a specific user
     * @param workoutId The ID of the workout to check
     * @param userId The ID of the user
     * @return ResultWrapper containing boolean indicating if workout is saved
     */
    override suspend fun isWorkoutSavedByUser(workoutId: String, userId: String): ResultWrapper<Boolean> {
        return try {
            val saveSnapshot = database.reference
                .child(WORKOUT_SAVES_COLLECTION)
                .child(WorkoutSave.createId(userId, workoutId))
                .get()
                .await()

            ResultWrapper.Success(saveSnapshot.exists())
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }




    /**
     * Toggles the save status of a workout for a specific user
     * @param workout The workout to toggle save status for
     * @param userId The ID of the user
     * @return ResultWrapper indicating success or failure
     */
    override suspend fun toggleWorkoutSave(workout: Workout, userId: String): ResultWrapper<Unit> {
        return try {
            val isSaved = when (val result = isWorkoutSavedByUser(workout.id, userId)) {
                is ResultWrapper.Success -> result.data
                else -> return ResultWrapper.Error(Exception("Failed to check save status"))
            }

            val updates = mutableMapOf<String, Any?>()
            val saveKey = "${userId}_${workout.id}"

            if (isSaved) {
                // Remove save
                updates["$WORKOUT_SAVES_COLLECTION/$saveKey"] = null
                updates["$WORKOUTS_COLLECTION/${workout.id}/$WORKOUTS_METRICS/$WORKOUT_SAVE_COUNT"] = ServerValue.increment(-1)
            } else {
                // Add save
                val currentTimestamp = System.currentTimeMillis()
                updates["$WORKOUT_SAVES_COLLECTION/$saveKey"] = WorkoutSave(id =saveKey, userId = userId, timestamp =currentTimestamp , workoutId = workout.id)
                updates["$WORKOUTS_COLLECTION/${workout.id}/$WORKOUTS_METRICS/$WORKOUT_SAVE_COUNT"] =ServerValue.increment(1)
            }

            database.reference.updateChildren(updates).await()
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }



    /**
     * Deletes a workout from local storage
     * @param workoutId The ID of the workout to delete
     * @return ResultWrapper indicating success or failure
     */
    override suspend fun deleteWorkoutLocally(workoutId: String): ResultWrapper<Unit> {
        return try {
            appDatabase.workoutDao().deleteWorkoutById(workoutId)
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    /**
     * Retrieves all workouts stored locally
     * @return ResultWrapper containing list of locally stored workouts
     */
    override suspend fun getLocalWorkouts(): ResultWrapper<List<Workout>> {
        return try {
            val localWorkouts = appDatabase.workoutDao().getAllWorkouts().map { it.toWorkout() }
            ResultWrapper.Success(localWorkouts)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    override suspend fun getLocalWorkoutsFlow(): Flow<ResultWrapper<List<Workout>>> = callbackFlow {
        try {
            appDatabase.workoutDao().getAllWorkoutsFlow().collectLatest { workoutEntities ->
                workoutEntities.map { workoutEntity -> workoutEntity.toWorkout() }.also {
                    send(ResultWrapper.Success(it))
                }
            }
        } catch (e: Exception) {
            send(ResultWrapper.Error(e))
        }
    }

    /**
     * Retrieves a list of workouts from the Firebase database based on the specified sort type and limit.
     *
     * This function returns a Flow that emits a ResultWrapper containing a list of workouts.
     * The workouts are sorted according to the specified sort type and limited to the specified number of items.
     *
     * @param sortType The type of sorting to apply to the workouts (e.g., NEWEST, MOST_LIKED, MOST_SAVED).
     * @param limit The maximum number of workouts to retrieve.
     * @return A Flow emitting a ResultWrapper containing a list of workouts.
     */
    override suspend fun getWorkouts(sortType: SortType, limit: Int): Flow<ResultWrapper<List<Workout>>> = callbackFlow {
        // Determine the query based on the sort type
        val query = when (sortType) {
            SortType.NEWEST -> {
                database.reference
                    .child(WORKOUTS_COLLECTION)
                    .orderByChild(WORKOUTS_TIMESTAMP)
                    .limitToLast(limit)
            }
            SortType.MOST_LIKED -> {
                database.reference
                    .child(WORKOUTS_COLLECTION)
                    .orderByChild("$WORKOUTS_METRICS/$WORKOUT_LIKES_COUNT")
                    .limitToLast(limit)
            }
            SortType.MOST_SAVED -> {
                database.reference
                    .child(WORKOUTS_COLLECTION)
                    .orderByChild("$WORKOUTS_METRICS/$WORKOUT_SAVE_COUNT")
                    .limitToLast(limit)
            }
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("WorkoutRepository", "Data changed, emitting new workouts") // Add logging
                val workouts = snapshot.children.mapNotNull {
                    it.getValue(Workout::class.java)
                }.reversed()

                trySend(ResultWrapper.Success(workouts))
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WorkoutRepository", "Query cancelled: ${error.message}")
                trySend(ResultWrapper.Error(Exception(error.message)))
            }
        }

        // Add the listener to the query
        query.addValueEventListener(listener)

        // Remove the listener when the flow is closed
        awaitClose {
            Log.d("WorkoutRepository", "Removing listener")
            query.removeEventListener(listener) }
    }


    /**
     * Saves a workout to local storage for offline access
     * @param workout The workout to save locally
     * @return ResultWrapper indicating success or failure
     */
    override suspend fun saveWorkoutLocally(workout: Workout): ResultWrapper<Unit> {
        return try {
            appDatabase.workoutDao().insertWorkout(workout.toWorkoutEntity())
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }


}

