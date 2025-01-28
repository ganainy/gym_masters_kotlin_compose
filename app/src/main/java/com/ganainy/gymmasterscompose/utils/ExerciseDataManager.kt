package com.ganainy.gymmasterscompose.utils

import com.ganainy.gymmasterscompose.di.IoDispatcher
import com.ganainy.gymmasterscompose.ui.theme.models.BodyPart
import com.ganainy.gymmasterscompose.ui.theme.models.Equipment
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.theme.repository.IExerciseRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ResultWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

// Shared utility class for exercise-related operations
class ExerciseDataManager @Inject constructor(
    private val exerciseRepository: IExerciseRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun loadExerciseData(): Result<ExerciseDataResult> = withContext(ioDispatcher) {
        try {
            coroutineScope {
                val bodyParts = async { exerciseRepository.getBodyPartList() }
                val targets = async { exerciseRepository.getTargetList() }
                val equipment = async { exerciseRepository.getEquipmentList() }
                val exercises = async { exerciseRepository.getExercises() }

                val results = awaitAll(bodyParts, targets, equipment, exercises)

                if (results.any { it is ResultWrapper.Error }) {
                    val error = results.filterIsInstance<ResultWrapper.Error>().first()
                    Result.failure(error.exception)
                } else {
                    Result.success(
                        ExerciseDataResult(
                            bodyParts = (bodyParts.await() as ResultWrapper.Success).data,
                            targets = (targets.await() as ResultWrapper.Success).data,
                            equipment = (equipment.await() as ResultWrapper.Success).data,
                            exercises = (exercises.await() as ResultWrapper.Success).data
                        )
                    )
                }
            }
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