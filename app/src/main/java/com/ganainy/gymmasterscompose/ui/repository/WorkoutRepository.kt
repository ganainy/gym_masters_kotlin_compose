package com.ganainy.gymmasterscompose.ui.repository

import android.net.Uri
import android.util.Log
import com.ganainy.gymmasterscompose.Constants
import com.ganainy.gymmasterscompose.ui.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutEntity.Companion.WORKOUTS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutEntity.Companion.WORKOUTS_METRICS
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutEntity.Companion.WORKOUTS_TIMESTAMP
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutEntity.Companion.WORKOUT_LIKES_COUNT
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutEntity.Companion.WORKOUT_SAVE_COUNT
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutLike
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutLike.Companion.WORKOUT_LIKES_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutSave
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutSave.Companion.WORKOUT_SAVES_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.workout.toWorkout
import com.ganainy.gymmasterscompose.ui.models.workout.toWorkoutEntity
import com.ganainy.gymmasterscompose.ui.room.AppDatabase
import com.ganainy.gymmasterscompose.ui.screens.workout_list.SortType
import com.ganainy.gymmasterscompose.utils.Utils.generateRandomId
import com.google.firebase.Timestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import androidx.core.net.toUri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi

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
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val appDatabase: AppDatabase,
    private val hashtagRepository: IHashtagRepository
) : IWorkoutRepository {

    // Firestore Collection References
    private val workoutsCollection = firestore.collection(WORKOUTS_COLLECTION)
    private val workoutLikesCollection = firestore.collection(WORKOUT_LIKES_COLLECTION)
    private val workoutSavesCollection = firestore.collection(WORKOUT_SAVES_COLLECTION)

    override suspend fun getWorkout(workoutId: String): Workout? {
        return try {
            val snapshot = workoutsCollection.document(workoutId).get().await()
            if (snapshot.exists()) {
                snapshot.toObject(Workout::class.java)
            } else {
                Log.w("WorkoutRepository", "Workout document $workoutId does not exist.")
                null
            }
        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Error fetching workout: $workoutId", e)
            null // Return null on error
        }
    }


    override fun getWorkoutFlow(workoutId: String): Flow<ResultWrapper<Workout?>> = callbackFlow {
        val docRef = workoutsCollection.document(workoutId)
        var listenerRegistration: ListenerRegistration? = null
        try {
            listenerRegistration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ResultWrapper.Error(error))
                    Log.e("WorkoutRepository", "Error listening to workout $workoutId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    try {
                        trySend(ResultWrapper.Success(snapshot.toObject(Workout::class.java)))
                    } catch (e: Exception) {
                        Log.e(
                            "WorkoutRepository",
                            "Error converting workout snapshot $workoutId",
                            e
                        )
                        trySend(ResultWrapper.Error(Exception("Error parsing workout data", e)))
                    }
                } else {
                    // Document doesn't exist
                    trySend(ResultWrapper.Success(null))
                }
            }
        } catch (e: Exception) {
            trySend(ResultWrapper.Error(e))
            close(e)
        }
        awaitClose {
            Log.d("WorkoutRepository", "Removing listener for workout $workoutId")
            listenerRegistration?.remove()
        }
    }

    override suspend fun uploadWorkoutWithImage(
        workout: Workout,
        imagePath: String
    ): ResultWrapper<Unit> {
        // 1. Handle image upload (no change needed here)
        val imageUrlResult = if (imagePath.isNotEmpty()) {
            uploadWorkoutCoverImage(imagePath)
        } else {
            ResultWrapper.Success("") // Empty string if no image
        }

        val imageUrl = when(imageUrlResult) {
            is ResultWrapper.Success -> imageUrlResult.data
            is ResultWrapper.Error -> return ResultWrapper.Error(imageUrlResult.exception) // Return early on upload failure
            is ResultWrapper.Loading -> return ResultWrapper.Error(Exception("Image upload still loading - unexpected"))
        }

        // 2. Prepare Workout data
        val workoutWithImage = workout.copy(
            imageUrl = imageUrl,
            // Normalize tags if needed
            tags = workout.tags.map { it.lowercase().trim() }.filter { it.isNotEmpty() }.distinct()
            // Ensure metrics are initialized if part of the model being saved
        )

        // 3. Prepare Firestore WriteBatch
        val batch = firestore.batch()
        try {
            // Define document reference
            val workoutDocRef = workoutsCollection.document(workoutWithImage.id)

            // Add operations to batch:
            // a) Set the workout document
            batch.set(workoutDocRef, workoutWithImage)

            // b) Add hashtag updates using the refactored repository
            hashtagRepository.addHashtagUpdatesToBatch(batch, workoutWithImage.tags)

            // 4. Commit the batch
            batch.commit().await()
            Log.d("WorkoutRepository", "Workout ${workoutWithImage.id} uploaded successfully.")
            return ResultWrapper.Success(Unit)

        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Error committing workout upload batch for ${workoutWithImage.id}", e)
            // TODO:  deleting uploaded image if batch fails?
            return ResultWrapper.Error(e)
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
        // Use a more specific path if desired, e.g., including workout ID if known beforehand
        val imageRef = storage.reference.child(Constants.WORKOUT_COVER_IMAGES)
            .child(generateRandomId(Constants.COVER_IMAGE)) // Or use workout ID + timestamp
        return try {
            imageRef.putFile(imagePath.toUri()).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()
            ResultWrapper.Success(downloadUrl)
        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Failed to upload workout cover image: $imagePath", e)
            ResultWrapper.Error(e)
        }
    }


    override suspend fun deleteWorkout(workoutId: String): ResultWrapper<Unit> {
        // 1. Get workout data first to access tags and image URL
        val workout = getWorkout(workoutId) // Use the Firestore implementation
        if (workout == null) {
            Log.w("WorkoutRepository", "Cannot delete workout $workoutId: Not found.")
            return ResultWrapper.Error(Exception("Workout not found"))
        }

        val batch = firestore.batch()
        return try {
            // 2. Find associated likes and saves (consider doing this less frequently or via Functions)
            // This adds read cost to every delete.
            val likesQuery = workoutLikesCollection.whereEqualTo(WorkoutLike.WORKOUT_ID_FIELD, workoutId).get().await()
            val savesQuery = workoutSavesCollection.whereEqualTo(WorkoutSave.WORKOUT_ID_FIELD, workoutId).get().await()
            val likeRefsToDelete = likesQuery.documents.map { it.reference }
            val saveRefsToDelete = savesQuery.documents.map { it.reference }

            // 3. Define main document reference
            val workoutDocRef = workoutsCollection.document(workoutId)

            // 4. Add operations to batch:
            // a) Delete workout document
            batch.delete(workoutDocRef)

            // b) Delete associated likes
            likeRefsToDelete.forEach { batch.delete(it) }
            // c) Delete associated saves
            saveRefsToDelete.forEach { batch.delete(it) }
            Log.d("WorkoutRepository", "Prepared batch to delete ${likeRefsToDelete.size} likes and ${saveRefsToDelete.size} saves for workout $workoutId")


            // d) Decrement/delete hashtags using the refactored repository
            hashtagRepository.addHashtagDecrementToBatch(batch, workout.tags)

            // 5. Commit the batch
            batch.commit().await()
            Log.d("WorkoutRepository", "Workout $workoutId and associated data deleted from Firestore.")

            // 6. Delete cover image from Storage (after successful batch commit)
            if (workout.imageUrl.isNotEmpty()) {
                deleteWorkoutCoverImage(workout.imageUrl) // Call the existing storage delete method
            }

            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Error deleting workout $workoutId", e)
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


    override suspend fun isWorkoutLikedByUser(workoutId: String, userId: String): ResultWrapper<Boolean> {
        return try {
            val likeDocId = WorkoutLike.createId(userId, workoutId)
            val snapshot = workoutLikesCollection.document(likeDocId).get().await()
            ResultWrapper.Success(snapshot.exists())
        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Error checking like status for workout $workoutId by user $userId", e)
            ResultWrapper.Error(e)
        }
    }


    override suspend fun isWorkoutSavedByUser(workoutId: String, userId: String): ResultWrapper<Boolean> {
        return try {
            val saveDocId = WorkoutSave.createId(userId, workoutId)
            val snapshot = workoutSavesCollection.document(saveDocId).get().await()
            ResultWrapper.Success(snapshot.exists())
        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Error checking save status for workout $workoutId by user $userId", e)
            ResultWrapper.Error(e)
        }
    }

    override suspend fun toggleWorkoutSave(workout: Workout, userId: String): ResultWrapper<Unit> {
        val saveDocId = WorkoutSave.createId(userId, workout.id)
        val batch = firestore.batch()
        return try {
            // Check current backend state
            val isCurrentlySavedResult = isWorkoutSavedByUser(workout.id, userId)
            val isCurrentlySaved = when(isCurrentlySavedResult) {
                is ResultWrapper.Success -> isCurrentlySavedResult.data
                is ResultWrapper.Error -> throw isCurrentlySavedResult.exception // Propagate error
                is ResultWrapper.Loading -> throw IllegalStateException("isWorkoutSavedByUser returned Loading")
            }

            // Document references
            val saveDocRef = workoutSavesCollection.document(saveDocId)
            val workoutDocRef = workoutsCollection.document(workout.id)
            val metricsSaveCountPath = "$WORKOUTS_METRICS.$WORKOUT_SAVE_COUNT"

            if (isCurrentlySaved) {
                // --- Unsave Logic ---
                batch.delete(saveDocRef)
                batch.update(workoutDocRef, metricsSaveCountPath, FieldValue.increment(-1))
                Log.d("WorkoutRepository", "Prepared batch to UNSAVE workout $workout.id for user $userId")

            } else {
                // --- Save Logic ---
                val saveObject = WorkoutSave(
                    id = saveDocId,
                    userId = userId,
                    timestamp = Timestamp.now(),
                    workoutId = workout.id
                )
                batch.set(saveDocRef, saveObject)
                batch.update(workoutDocRef, metricsSaveCountPath, FieldValue.increment(1))
                Log.d("WorkoutRepository", "Prepared batch to SAVE workout $workout.id for user $userId")
            }

            // Commit the batch
            batch.commit().await()
            Log.d("WorkoutRepository", "Workout save toggled successfully for workout ${workout.id}")
            ResultWrapper.Success(Unit)

        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Error toggling workout save for workout ${workout.id}", e)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getWorkouts(sortType: SortType, limit: Int): Flow<ResultWrapper<List<Workout>>> = callbackFlow {
        trySend(ResultWrapper.Loading()) // Emit loading state

        var query: Query = workoutsCollection // Base query

        // Apply sorting based on sortType
        query = when (sortType) {
            SortType.NEWEST -> query.orderBy(WORKOUTS_TIMESTAMP, Query.Direction.DESCENDING)
            SortType.MOST_LIKED -> query.orderBy("$WORKOUTS_METRICS.$WORKOUT_LIKES_COUNT", Query.Direction.DESCENDING)
            SortType.MOST_SAVED -> query.orderBy("$WORKOUTS_METRICS.$WORKOUT_SAVE_COUNT", Query.Direction.DESCENDING)
        }

        // Apply limit
        query = query.limit(limit.toLong())

        var listenerRegistration: ListenerRegistration? = null
        try {
            listenerRegistration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ResultWrapper.Error(error))
                    Log.e("WorkoutRepository", "Error listening to workouts (sort: $sortType)", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val workouts = snapshot.documents.mapNotNull { doc ->
                        try { doc.toObject(Workout::class.java) } catch (e: Exception) {
                            Log.e("WorkoutRepository", "Error converting workout doc ${doc.id}", e)
                            null
                        }
                    }
                    // No need to reverse here as Firestore ordering handles it
                    trySend(ResultWrapper.Success(workouts))
                } else {
                    trySend(ResultWrapper.Success(emptyList()))
                }
            }
        } catch (e: Exception) {
            // Error setting up the listener
            trySend(ResultWrapper.Error(e))
            close(e)
        }

        awaitClose {
            Log.d("WorkoutRepository", "Removing listener for workouts (sort: $sortType)")
            listenerRegistration?.remove()
        }
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

