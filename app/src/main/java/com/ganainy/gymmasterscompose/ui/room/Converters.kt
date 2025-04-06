package com.ganainy.gymmasterscompose.ui.room

import androidx.room.TypeConverter
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutExercise
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


/**
 * A class containing type converters for Room database.
 *
 * This class provides methods to convert between complex data types and their
 * JSON string representations for storing in the Room database.
 */
class Converters {

    /**
     * Converts a list of strings to a JSON string.
     *
     * @param value The list of strings to be converted.
     * @return The JSON string representation of the list.
     */
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    /**
     * Converts a JSON string to a list of strings.
     *
     * @param value The JSON string to be converted.
     * @return The list of strings represented by the JSON string.
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Converts a list of WorkoutExercise objects to a JSON string.
     *
     * @param value The list of WorkoutExercise objects to be converted.
     * @return The JSON string representation of the list.
     */
    @TypeConverter
    fun fromWorkoutExerciseList(value: List<WorkoutExercise>): String {
        return Json.encodeToString(value)
    }

    /**
     * Converts a JSON string to a list of WorkoutExercise objects.
     *
     * @param value The JSON string to be converted.
     * @return The list of WorkoutExercise objects represented by the JSON string.
     */
    @TypeConverter
    fun toWorkoutExerciseList(value: String): List<WorkoutExercise> {
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
         * Converts a Firestore Timestamp to a Long.
         *
         * @param value The Firestore Timestamp to be converted.
         * @return The Long representation of the Timestamp.
         */
        @TypeConverter
        fun fromTimestamp(value: com.google.firebase.Timestamp?): Long? {
            return value?.seconds
        }

        /**
         * Converts a Long to a Firestore Timestamp.
         *
         * @param value The Long to be converted.
         * @return The Firestore Timestamp representation of the Long.
         */
        @TypeConverter
        fun toTimestamp(value: Long?): com.google.firebase.Timestamp? {
            return value?.let { com.google.firebase.Timestamp(it, 0) }
        }

}