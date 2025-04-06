package com.ganainy.gymmasterscompose.prefs

import android.content.Context
import android.content.SharedPreferences

class ExerciseDownloadPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("exercise_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_INITIAL_DOWNLOAD_COMPLETE = "initial_exercise_download_complete"
    }

    fun isInitialDownloadComplete(): Boolean {
        return prefs.getBoolean(KEY_INITIAL_DOWNLOAD_COMPLETE, false)
    }

    fun setInitialDownloadComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_INITIAL_DOWNLOAD_COMPLETE, complete).apply()
    }
}