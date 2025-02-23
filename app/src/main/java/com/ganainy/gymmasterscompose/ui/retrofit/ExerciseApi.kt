package com.ganainy.gymmasterscompose.ui.retrofit

import com.ganainy.gymmasterscompose.ui.models.Exercise
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ExerciseApi {
    @GET("exercises")
    suspend fun getExercises(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): List<Exercise>?

    @GET("exercises/bodyPart/{bodyPart}")
    suspend fun getExercisesByBodyPart(@Path("bodyPart") bodyPart: String): List<Exercise>

    @GET("exercises/bodyPartList")
    suspend fun getBodyPartList(): List<String>

    @GET("exercises/equipmentList")
    suspend fun getEquipmentList(): List<String>

    @GET("exercises/targetList")
    suspend fun getTargetList(): List<String>

    @GET("exercises/equipment/{type}")
    suspend fun getExercisesByEquipment(@Path("type") type: String): List<Exercise>

    @GET("exercises/target/{target}")
    suspend fun getExercisesByTarget(@Path("target") target: String): List<Exercise>

    @GET("exercises/exercise/{id}")
    suspend fun getExerciseById(@Path("id") id: String): Exercise

    @GET("exercises/name/{name}")
    suspend fun getExercisesByName(@Path("name") name: String): List<Exercise>
}