package com.ganainy.gymmasterscompose.ui.room


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ganainy.gymmasterscompose.ui.models.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise")
    suspend fun getAllExercises(): List<Exercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<Exercise>)

    @Query("SELECT * FROM exercise WHERE equipment LIKE :type")
    abstract fun getExercisesByEquipment(type: String): List<Exercise>?

    @Query("SELECT * FROM exercise WHERE target LIKE :target")
    abstract fun getExercisesByTarget(target: String): List<Exercise>?

    @Query("SELECT * FROM exercise WHERE name LIKE :name")
    abstract fun getExercisesByName(name: String): List<Exercise>?

    @Query("SELECT * FROM exercise WHERE id LIKE :id")
    abstract fun getExerciseById(id: String): Exercise?

    @Query("SELECT * FROM exercise WHERE id LIKE :id")
    abstract fun observeExerciseById(id: String): Flow<Exercise?>

    @Query("SELECT * FROM exercise WHERE bodyPart LIKE :bodyPart")
    abstract fun getExercisesByBodyPart(bodyPart: String): List<Exercise>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: Exercise)

    @Update
    suspend fun update(exercise: Exercise)

    @Query("SELECT * FROM exercise WHERE isSavedLocally = 1")
    abstract fun observeSavedExercises(): Flow<List<Exercise>?>

    /**
     * Fetches a specific page of exercises.
     * Useful for paginated loading from the local cache.
     * ORDER BY id is important for consistent paging.
     */
    @Query("SELECT * FROM exercise ORDER BY id ASC LIMIT :limit OFFSET :offset") // Order by ID for consistency
    suspend fun getExercisesPage(limit: Int, offset: Int): List<Exercise>

}