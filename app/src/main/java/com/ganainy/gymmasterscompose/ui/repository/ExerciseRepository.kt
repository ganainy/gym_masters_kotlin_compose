package com.ganainy.gymmasterscompose.ui.repository


import AndroidImageProcessor
import android.content.Context
import android.util.Log
import com.ganainy.gymmasterscompose.di.IoDispatcher
import com.ganainy.gymmasterscompose.prefs.ExerciseDownloadPrefs
import com.ganainy.gymmasterscompose.ui.models.BodyPart
import com.ganainy.gymmasterscompose.ui.models.Equipment
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.retrofit.ExerciseApi
import com.ganainy.gymmasterscompose.ui.room.AppDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject


/*get exercises from the api at https://exercisedb.p.rapidapi.com*/
interface IExerciseRepository {
    suspend fun getExercises(
        limit: Int? = null,
        offset: Int? = null
    ): ResultWrapper<List<Exercise>>?

    suspend fun getExercisesByBodyPart(bodyPart: BodyPart): ResultWrapper<List<Exercise>>
    suspend fun getBodyPartList(): ResultWrapper<List<BodyPart>>?
    suspend fun getEquipmentList(): ResultWrapper<List<Equipment>>?
    suspend fun getTargetList(): ResultWrapper<List<TargetMuscle>>?
    suspend fun getExercisesByEquipment(type: Equipment): ResultWrapper<List<Exercise>>
    suspend fun getExercisesByTarget(target: TargetMuscle): ResultWrapper<List<Exercise>>
    suspend fun getExerciseById(id: String): ResultWrapper<Exercise>
    suspend fun getExercisesByName(name: String): ResultWrapper<List<Exercise>>
    suspend fun toggleExerciseSaveLocally(exercise: Exercise): ResultWrapper<Unit>
    suspend fun observeExercise(exerciseId: String): Flow<ResultWrapper<Exercise?>>
    suspend fun observeSavedExercises(): Flow<ResultWrapper<List<Exercise>?>>

    /** Fetches all exercises directly from the local Room cache. */
    suspend fun getCachedExercises(): ResultWrapper<List<Exercise>>

    /** Fetches the body part list directly from the local Room cache. */
    suspend fun getCachedBodyParts(): ResultWrapper<List<BodyPart>>

    /** Fetches the equipment list directly from the local Room cache. */
    suspend fun getCachedEquipment(): ResultWrapper<List<Equipment>>

    /** Fetches the target muscle list directly from the local Room cache. */
    suspend fun getCachedTargets(): ResultWrapper<List<TargetMuscle>>

    /**
     * Attempts to fetch all exercises from the API using pagination and cache them locally.
     * Checks a flag to prevent re-downloading after the first successful completion.
     *
     * @param forceRefresh If true, ignores the completion flag and attempts download again.
     * @return ResultWrapper indicating success (Unit) or failure. Success means the process finished,
     *         even if some pages failed (errors will be logged). Failure indicates a critical error.
     */
    suspend fun fetchAllExercisesAndCache(forceRefresh: Boolean = false): ResultWrapper<Unit>
}


class ExerciseRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val exerciseApi: ExerciseApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val context: Context,
    private val exerciseDownloadPrefs: ExerciseDownloadPrefs
) : IExerciseRepository {

    companion object {
        private const val CACHE_TIMEOUT = 24 * 60 * 60 * 1000L // 24 hours
        private const val EXERCISE_PAGE_SIZE = 100 // Max allowed by API
    }

    val imageProcessor = AndroidImageProcessor(context)
    private val exerciseDao = appDatabase.exerciseDao()

    // Generic function to handle API calls with caching
    private suspend fun <T> fetchWithCache(
        dbQuery: suspend () -> T?,
        networkCall: suspend () -> T?,
        saveCallResult: suspend (T) -> Unit, // DAO operation passed in
        shouldFetch: (T?) -> Boolean = { it == null || (it is List<*> && it.isEmpty()) } // List check fixed
    ): ResultWrapper<T> = withContext(ioDispatcher) {
        try {
            val cachedData = dbQuery()
            if (cachedData != null && !shouldFetch(cachedData)) {
                return@withContext ResultWrapper.Success(cachedData)
            }

            val networkResult = networkCall() ?: // Return error immediately if network call returns null
            return@withContext ResultWrapper.Error(Exception("Failed to fetch data from network (result is null)"))


            // Process images for exercises *before* saving
            val processedResult = if (networkResult is List<*>) {
                val processedList = networkResult.filterIsInstance<Exercise>().map { exercise ->
                    val imagePath = processExerciseImageAndGetPath(exercise)
                    exercise.copy(screenshotPath = imagePath)
                }
                // Reconstruct result type if necessary (e.g., if networkResult wasn't just List<Exercise>)
                // This assumes networkResult IS List<Exercise> for simplicity here
                processedList as T
            } else if (networkResult is Exercise) {
                val imagePath = processExerciseImageAndGetPath(networkResult)
                networkResult.copy(screenshotPath = imagePath) as T
            }
            else {
                networkResult // No processing needed for non-exercise types
            }


            // Save the potentially processed result
            saveCallResult(processedResult) // Call the specific DAO operation (insert/update)

            ResultWrapper.Success(processedResult) // Return processed result

        } catch (e: Exception) {
            Log.e("ExerciseRepository", "Error in fetchWithCache", e)
            // Try returning cache even on network/processing error
            val cachedDataOnError = try { dbQuery() } catch (e: Exception) { null }
            if (cachedDataOnError != null && !shouldFetch(cachedDataOnError)) {
                ResultWrapper.Success(cachedDataOnError)
            } else {
                ResultWrapper.Error(e)
            }
        }
    }

    override suspend fun getCachedExercises(): ResultWrapper<List<Exercise>> = withContext(ioDispatcher) {
        try {
            val exercises = exerciseDao.getAllExercises() // Directly query Room
            ResultWrapper.Success(exercises)
        } catch (e: Exception) {
            Log.e("ExerciseRepository", "Error getting cached exercises from Room", e)
            ResultWrapper.Error(e)
        }
    }

    override suspend fun getCachedBodyParts(): ResultWrapper<List<BodyPart>> = withContext(ioDispatcher) {
        try {
            val bodyParts = appDatabase.bodyPartListDao().getBodyPartList() ?: emptyList()
            ResultWrapper.Success(bodyParts)
        } catch (e: Exception) {
            Log.e("ExerciseRepository", "Error getting cached body parts from Room", e)
            ResultWrapper.Error(e)
        }
    }

    override suspend fun getCachedEquipment(): ResultWrapper<List<Equipment>> = withContext(ioDispatcher) {
        try {
            val equipment = appDatabase.equipmentDao().getAll() ?: emptyList()
            ResultWrapper.Success(equipment)
        } catch (e: Exception) {
            Log.e("ExerciseRepository", "Error getting cached equipment from Room", e)
            ResultWrapper.Error(e)
        }
    }

    override suspend fun getCachedTargets(): ResultWrapper<List<TargetMuscle>> = withContext(ioDispatcher) {
        try {
            val targets = appDatabase.targetDao().getAll() ?: emptyList()
            ResultWrapper.Success(targets)
        } catch (e: Exception) {
            Log.e("ExerciseRepository", "Error getting cached targets from Room", e)
            ResultWrapper.Error(e)
        }
    }

    override suspend fun fetchAllExercisesAndCache(forceRefresh: Boolean): ResultWrapper<Unit> = withContext(ioDispatcher) {
        // 1. Check if download was already completed (unless forced)
        if (!forceRefresh && exerciseDownloadPrefs.isInitialDownloadComplete()) {
            Log.d("ExerciseRepository", "Initial exercise download already completed. Skipping.")
            return@withContext ResultWrapper.Success(Unit)
        }
        // Optional: Check if DB has *any* data, maybe don't clear if resuming?
        // For simplicity, we assume a full download attempt each time until success flag is set.

        Log.d("ExerciseRepository", "Starting initial exercise download process...")
        var offset = 0
        var fetchMore = true
        var page = 1
        var totalFetched = 0
        var lastPageError = false // Flag to prevent infinite loop on persistent error

        while (fetchMore) {
            Log.d("ExerciseRepository", "Fetching page $page (Offset: $offset, Limit: $EXERCISE_PAGE_SIZE)")
            try {
                // 2. Call API directly (bypass fetchWithCache)
                val exercisesFromApi = exerciseApi.getExercises(limit = EXERCISE_PAGE_SIZE, offset = offset)

                if (exercisesFromApi.isNullOrEmpty()) {
                    Log.d("ExerciseRepository", "Fetched empty list or null. Assuming end of exercises.")
                    fetchMore = false // Stop if API returns empty list
                } else {
                    totalFetched += exercisesFromApi.size
                    Log.d("ExerciseRepository", "Fetched ${exercisesFromApi.size} exercises for page $page. Total so far: $totalFetched")

                    // 3. Process images and prepare for DB insert
                    val exercisesToInsert = exercisesFromApi.map { exercise ->
                        val imagePath = try {
                            imageProcessor.getImagePathFromGif(exercise.gifUrl)
                        } catch (e: Exception) {
                            Log.e("ExerciseRepository", "Error processing image for exercise ${exercise.id}", e)
                            null // Continue without image if processing fails
                        }
                        exercise.copy(screenshotPath = imagePath) // Set path before insert
                    }

                    // 4. Save the fetched batch to Room DB
                    try {
                        exerciseDao.insertAll(exercisesToInsert)
                        Log.d("ExerciseRepository", "Successfully inserted ${exercisesToInsert.size} exercises from page $page.")
                    } catch(dbError: Exception) {
                        Log.e("ExerciseRepository", "Error inserting exercises from page $page into Room DB", dbError)
                        // Decide if DB error is critical. Maybe continue? For now, let's continue.
                        lastPageError = true // Avoid infinite loop if DB keeps failing
                    }


                    // 5. Check if this was the last page
                    if (exercisesFromApi.size < EXERCISE_PAGE_SIZE) {
                        Log.d("ExerciseRepository", "Fetched less than page size (${exercisesFromApi.size} < $EXERCISE_PAGE_SIZE). Assuming end of exercises.")
                        fetchMore = false
                    } else {
                        // Prepare for next iteration
                        offset += EXERCISE_PAGE_SIZE
                        page++
                        lastPageError = false // Reset error flag after successful page
                        // Optional delay to avoid rate limiting
                        // delay(200) // Delay for 200ms
                    }
                }
            } catch (e: Exception) {
                Log.e("ExerciseRepository", "Error fetching exercises on page $page (Offset: $offset)", e)
                // Decide how to handle API errors: stop? retry page? skip page?
                // For robustness, let's log and stop the loop to avoid hammering a failing API.
                // Set a flag if the error persists on the same page.
                if(lastPageError) {
                    Log.e("ExerciseRepository", "Repeated error fetching page $page. Aborting download.")
                    fetchMore = false // Abort on repeated error for the same page
                    return@withContext ResultWrapper.Error(Exception("Failed to fetch page $page after retry", e))
                } else {
                    lastPageError = true
                    // Optional: Add a longer delay before potentially retrying the same offset
                    // delay(1000)
                    // Or just break: fetchMore = false
                    Log.w("ExerciseRepository", "Continuing after error on page $page. Will retry offset $offset or stop if repeated.")
                    // Let's break here to be safe. User can retry manually if needed.
                    fetchMore = false
                    return@withContext ResultWrapper.Error(Exception("Failed to fetch page $page", e))

                }
            }
        } // end while

        // 6. Mark download as complete only if we exited the loop without a critical error ending it prematurely
        if (!lastPageError) { // Check if loop completed normally or stopped due to empty page
            Log.d("ExerciseRepository", "Finished exercise download loop. Total fetched (approx): $totalFetched")
            exerciseDownloadPrefs.setInitialDownloadComplete(true)
            return@withContext ResultWrapper.Success(Unit)
        } else {
            Log.e("ExerciseRepository", "Exercise download loop aborted due to persistent error.")
            // Don't set the completion flag
            // Return the last error encountered if needed, or a generic error
            return@withContext ResultWrapper.Error(Exception("Exercise download aborted due to errors."))
        }
    }

    override suspend fun getExercises(limit: Int?, offset: Int?): ResultWrapper<List<Exercise>> =
        fetchWithCache(
            dbQuery = { exerciseDao.getExercisesPage(limit ?: 10, offset ?: 0) }, // Assuming a paginated query exists
            networkCall = { exerciseApi.getExercises(limit, offset) },
            saveCallResult = { exercises -> exerciseDao.insertAll(exercises as List<Exercise>) } // Pass insertAll
        )

    override suspend fun getExercisesByBodyPart(bodyPart: BodyPart): ResultWrapper<List<Exercise>> =
        fetchWithCache(
            dbQuery = { exerciseDao.getExercisesByBodyPart(bodyPart.name) },
            networkCall = { exerciseApi.getExercisesByBodyPart(bodyPart.name) },
            saveCallResult = { exercises -> exerciseDao.insertAll(exercises as List<Exercise>) } // Pass insertAll
        )

    override suspend fun getBodyPartList(): ResultWrapper<List<BodyPart>> =
        fetchWithCache(
            dbQuery = { appDatabase.bodyPartListDao().getBodyPartList() },
            networkCall = { exerciseApi.getBodyPartList().map { BodyPart(it) } },
            saveCallResult = { appDatabase.bodyPartListDao().insertBodyPartList(it as List<BodyPart>) }
        )

    override suspend fun getEquipmentList(): ResultWrapper<List<Equipment>> =
        fetchWithCache(
            dbQuery = { appDatabase.equipmentDao().getAll() },
            networkCall = { exerciseApi.getEquipmentList().map { Equipment(it) } },
            saveCallResult = { appDatabase.equipmentDao().insertAll(it as List<Equipment>) }
        )

    override suspend fun getTargetList(): ResultWrapper<List<TargetMuscle>> =
        fetchWithCache(
            dbQuery = { appDatabase.targetDao().getAll() },
            networkCall = { exerciseApi.getTargetList().map { TargetMuscle(it) } },
            saveCallResult = { appDatabase.targetDao().insertAll(it as List<TargetMuscle>) }
        )

    override suspend fun getExercisesByEquipment(type: Equipment): ResultWrapper<List<Exercise>> =
        fetchWithCache(
            dbQuery = { exerciseDao.getExercisesByEquipment(type.name) },
            networkCall = { exerciseApi.getExercisesByEquipment(type.name) },
            saveCallResult = { exercises -> exerciseDao.insertAll(exercises as List<Exercise>) }
        )

    override suspend fun getExercisesByTarget(target: TargetMuscle): ResultWrapper<List<Exercise>> =
        fetchWithCache(
            dbQuery = { exerciseDao.getExercisesByTarget(target.name) },
            networkCall = { exerciseApi.getExercisesByTarget(target.name) },
            saveCallResult = { exercises -> exerciseDao.insertAll(exercises as List<Exercise>) }
        )

    override suspend fun getExerciseById(id: String): ResultWrapper<Exercise> =
        fetchWithCache(
            dbQuery = { exerciseDao.getExerciseById(id) },
            networkCall = { exerciseApi.getExerciseById(id) },
            saveCallResult = { exercise -> exerciseDao.insert(exercise as Exercise) } // Pass insert
        )

    override suspend fun getExercisesByName(name: String): ResultWrapper<List<Exercise>> =
        fetchWithCache(
            dbQuery = { exerciseDao.getExercisesByName(name) },
            networkCall = { exerciseApi.getExercisesByName(name) },
            saveCallResult = { exercises -> exerciseDao.insertAll(exercises as List<Exercise>) }
        )

    override suspend fun toggleExerciseSaveLocally(exercise: Exercise): ResultWrapper<Unit> {
        exercise.isSavedLocally = !exercise.isSavedLocally
        return try {
            exerciseDao.update(exercise)
            ResultWrapper.Success(Unit)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    override suspend fun observeExercise(exerciseId: String): Flow<ResultWrapper<Exercise?>> {
        return flow {
            try {
                appDatabase.exerciseDao().observeExerciseById(exerciseId).collect { exercise ->
                    emit(ResultWrapper.Success(exercise))
                }
            } catch (e: Exception) {
                emit(ResultWrapper.Error(e))
            }
        }
    }

    override suspend fun observeSavedExercises(): Flow<ResultWrapper<List<Exercise>?>> {
        return flow {
            try {
                appDatabase.exerciseDao().observeSavedExercises().collect { exercises ->
                    emit(ResultWrapper.Success(exercises))
                }
            } catch (e: Exception) {
                emit(ResultWrapper.Error(e))
            }
        }
    }

    private suspend fun processExerciseImage(exercise: Exercise): Exercise {
        // Try to get image path, either from cache or by downloading gifUrl and image of it to disk
        val imagePath = imageProcessor.getImagePathFromGif(exercise.gifUrl)

        return if (imagePath != null) {
            val updatedExercise = exercise.copy(screenshotPath = imagePath)
            appDatabase.exerciseDao().update(updatedExercise)
            updatedExercise
        } else {
            exercise
        }
    }

    private suspend fun processExerciseImageAndGetPath(exercise: Exercise): String? {
        return try {
            imageProcessor.getImagePathFromGif(exercise.gifUrl)
        } catch (e: Exception) {
            Log.e("ExerciseRepository", "Error processing image for exercise ${exercise.id}", e)
            null
        }
    }


}