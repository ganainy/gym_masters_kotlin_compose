package com.ganainy.gymmasterscompose.utils

import com.ganainy.gymmasterscompose.di.IoDispatcher
import com.ganainy.gymmasterscompose.ui.models.BodyPart
import com.ganainy.gymmasterscompose.ui.models.Equipment
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.repository.IExerciseRepository
import com.ganainy.gymmasterscompose.ui.repository.ResultWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CachedExerciseDataResult(
    val exercises: List<Exercise>,
    val bodyParts: List<BodyPart>,
    val targets: List<TargetMuscle>,
    val equipment: List<Equipment>
)

// Shared utility class for exercise-related operations
class ExerciseDataManager @Inject constructor(
    private val exerciseRepository: IExerciseRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun loadCachedExerciseData(): Result<CachedExerciseDataResult> = coroutineScope {
        try {
            // Fetch all cached data concurrently
            val exercisesDeferred = async { exerciseRepository.getCachedExercises() }
            val bodyPartsDeferred = async { exerciseRepository.getCachedBodyParts() }
            val targetsDeferred = async { exerciseRepository.getCachedTargets() }
            val equipmentDeferred = async { exerciseRepository.getCachedEquipment() }

            // Await results
            val exercisesResult = exercisesDeferred.await()
            val bodyPartsResult = bodyPartsDeferred.await()
            val targetsResult = targetsDeferred.await()
            val equipmentResult = equipmentDeferred.await()

            // Check for errors in any fetch
            listOf(exercisesResult, bodyPartsResult, targetsResult, equipmentResult)
                .filterIsInstance<ResultWrapper.Error>()
                .firstOrNull()?.let { errorResult ->
                    return@coroutineScope Result.failure(errorResult.exception)
                }

            // All successful, extract data (assuming Success type)
            val exercises = (exercisesResult as ResultWrapper.Success).data
            val bodyParts = (bodyPartsResult as ResultWrapper.Success).data
            val targets = (targetsResult as ResultWrapper.Success).data
            val equipment = (equipmentResult as ResultWrapper.Success).data

            Result.success(CachedExerciseDataResult(exercises, bodyParts, targets, equipment))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun filterExercises(
        exercises: List<Exercise>,
        query: String = "",
        bodyPart: BodyPart? = null,
        target: TargetMuscle? = null,
        equipment: Equipment? = null
    ): List<Exercise> = withContext(ioDispatcher) {
        exercises.filter { exercise ->
            val matchesBodyPart = bodyPart?.let { exercise.bodyPart == it.name } ?: true
            val matchesTarget = target?.let { exercise.target == it.name } ?: true
            val matchesEquipment = equipment?.let { exercise.equipment == it.name } ?: true
            val matchesQuery = query.isEmpty() || exercise.name.contains(query, ignoreCase = true)

            matchesBodyPart && matchesTarget && matchesEquipment && matchesQuery
        }
    }
}


// Data class to hold exercise data results
data class ExerciseDataResult(
    val bodyParts: List<BodyPart>,
    val targets: List<TargetMuscle>,
    val equipment: List<Equipment>,
    val exercises: List<Exercise>
)