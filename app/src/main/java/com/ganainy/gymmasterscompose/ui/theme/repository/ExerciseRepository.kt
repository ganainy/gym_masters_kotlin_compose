package com.ganainy.gymmasterscompose.ui.theme.repository


import AndroidImageProcessor
import android.content.Context
import com.ganainy.gymmasterscompose.di.IoDispatcher
import com.ganainy.gymmasterscompose.ui.theme.models.BodyPart
import com.ganainy.gymmasterscompose.ui.theme.models.Equipment
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.theme.networking.retrofit.ExerciseApi
import com.ganainy.gymmasterscompose.ui.theme.room.AppDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject


/*get exercises from the api at https://exercisedb.p.rapidapi.com*/
interface IExerciseRepository {
    suspend fun getExercises(limit: Int? = null, offset: Int? = null): ResultWrapper<List<Exercise>>?
    suspend fun getExercisesByBodyPart(bodyPart: BodyPart): ResultWrapper<List<Exercise>>
    suspend fun getBodyPartList(): ResultWrapper<List<BodyPart>>?
    suspend fun getEquipmentList(): ResultWrapper<List<Equipment>>?
    suspend fun getTargetList(): ResultWrapper<List<TargetMuscle>>?
    suspend fun getExercisesByEquipment(type: Equipment): ResultWrapper<List<Exercise>>
    suspend fun getExercisesByTarget(target: TargetMuscle): ResultWrapper<List<Exercise>>
    suspend fun getExerciseById(id: String): ResultWrapper<Exercise>
    suspend fun getExercisesByName(name: String): ResultWrapper<List<Exercise>>
    suspend fun toggleExerciseSaveLocally(exercise: Exercise): ResultWrapper<Unit>
    suspend fun observeExercise(exerciseId: String):  Flow<ResultWrapper<Exercise?>>
    suspend fun observeSavedExercises(): Flow<ResultWrapper<List<Exercise>?>>
}


class ExerciseRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val exerciseApi: ExerciseApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val context: Context
) : IExerciseRepository {


    val imageProcessor = AndroidImageProcessor(context)

    // Generic function to handle API calls with caching
    private suspend fun <T> fetchWithCache(
        dbQuery: suspend () -> T?,
        networkCall: suspend () -> T?,
        saveCallResult: suspend (T) -> Unit,
        shouldFetch: (T?) -> Boolean = { it == null || (it is Collection<*> && it.isEmpty()) }
    ): ResultWrapper<T> = withContext(ioDispatcher) {
        try {
            // First try to get from cache
            val cachedData = dbQuery()

            // If cache is valid, return it
            if (cachedData != null && !shouldFetch(cachedData)) {
                return@withContext ResultWrapper.Success(cachedData)
            }

            // Otherwise fetch from network
            val networkResult = networkCall()

            // Process images for exercises
            if (networkResult is List<*>) {
                networkResult.filterIsInstance<Exercise>().forEach { exercise ->
                    imageProcessor.getImagePathFromGif(exercise.gifUrl)?.let { imagePath ->
                        exercise.screenshotPath = imagePath
                    }
                }
            }

            // Save network result to cache if not null
            if (networkResult != null) {
                saveCallResult(networkResult)
            }

            // Return network result or error if null
            return@withContext if (networkResult != null) {
                ResultWrapper.Success(networkResult)
            } else {
                ResultWrapper.Error(Exception("Failed to fetch data from network"))
            }

        } catch (e: Exception) {
            // If we have cached data, return it despite the error
            val cachedData = dbQuery()
            if (cachedData != null && !shouldFetch(cachedData)) {
                ResultWrapper.Success(cachedData)
            } else {
                ResultWrapper.Error(e)
            }
        }
    }

    override suspend fun getExercises(limit: Int?, offset: Int?): ResultWrapper<List<Exercise>> =
        fetchWithCache(
            dbQuery = { appDatabase.exerciseDao().getAllExercises() },
            networkCall = { exerciseApi.getExercises(limit, offset) },
            saveCallResult = {
                it.forEach { exercise ->
                    processExerciseImage(exercise).also { updatedExercise ->
                        appDatabase.exerciseDao().insert(updatedExercise)
                    }
                }
            }
        )

    override suspend fun getExercisesByBodyPart(bodyPart: BodyPart): ResultWrapper<List<Exercise>> =
        fetchWithCache(
            dbQuery = { appDatabase.exerciseDao().getExercisesByBodyPart(bodyPart.name) },
            networkCall = { exerciseApi.getExercisesByBodyPart(bodyPart.name) },
            saveCallResult = { appDatabase.exerciseDao().insertAll(it) }
        )

    override suspend fun getBodyPartList(): ResultWrapper<List<BodyPart>> =
        fetchWithCache(
            dbQuery = { appDatabase.bodyPartListDao().getBodyPartList() },
            networkCall = { exerciseApi.getBodyPartList().map { BodyPart(it) } },
            saveCallResult = { appDatabase.bodyPartListDao().insertBodyPartList(it) }
        )

    override suspend fun getEquipmentList(): ResultWrapper<List<Equipment>> =
        fetchWithCache(
            dbQuery = { appDatabase.equipmentDao().getAll() },
            networkCall = { exerciseApi.getEquipmentList().map { Equipment(it) } },
            saveCallResult = { appDatabase.equipmentDao().insertAll(it) }
        )

    override suspend fun getTargetList(): ResultWrapper<List<TargetMuscle>> =
        fetchWithCache(
            dbQuery = { appDatabase.targetDao().getAll() },
            networkCall = { exerciseApi.getTargetList().map { TargetMuscle(it) } },
            saveCallResult = { appDatabase.targetDao().insertAll(it) }
        )

    override suspend fun getExercisesByEquipment(type: Equipment): ResultWrapper<List<Exercise>> =
        fetchWithCache(
            dbQuery = { appDatabase.exerciseDao().getExercisesByEquipment(type.name) },
            networkCall = { exerciseApi.getExercisesByEquipment(type.name) },
            saveCallResult = { appDatabase.exerciseDao().insertAll(it) }
        )

    override suspend fun getExercisesByTarget(target: TargetMuscle): ResultWrapper<List<Exercise>> =
        fetchWithCache(
            dbQuery = { appDatabase.exerciseDao().getExercisesByTarget(target.name) },
            networkCall = { exerciseApi.getExercisesByTarget(target.name) },
            saveCallResult = { appDatabase.exerciseDao().insertAll(it) }
        )

    override suspend fun getExerciseById(id: String): ResultWrapper<Exercise> =
        fetchWithCache(
            dbQuery = { appDatabase.exerciseDao().getExerciseById(id) },
            networkCall = { exerciseApi.getExerciseById(id) },
            saveCallResult = { appDatabase.exerciseDao().insert(it) }
        )

    override suspend fun getExercisesByName(name: String): ResultWrapper<List<Exercise>> =
        fetchWithCache(
            dbQuery = { appDatabase.exerciseDao().getExercisesByName(name) },
            networkCall = { exerciseApi.getExercisesByName(name) },
            saveCallResult = { appDatabase.exerciseDao().insertAll(it) }
        )

    override suspend fun toggleExerciseSaveLocally(exercise: Exercise): ResultWrapper<Unit> {
        exercise.isSavedLocally = !exercise.isSavedLocally
        return try {
            appDatabase.exerciseDao().update(exercise)
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

    companion object {
        private const val CACHE_TIMEOUT = 24 * 60 * 60 * 1000L // 24 hours
    }
}