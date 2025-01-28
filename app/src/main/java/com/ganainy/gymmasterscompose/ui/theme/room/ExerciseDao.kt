package com.ganainy.gymmasterscompose.ui.theme.room


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise

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

    @Query("SELECT * FROM exercise WHERE bodyPart LIKE :bodyPart")
    abstract fun getExercisesByBodyPart(bodyPart: String): List<Exercise>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(exercise: Exercise)
}