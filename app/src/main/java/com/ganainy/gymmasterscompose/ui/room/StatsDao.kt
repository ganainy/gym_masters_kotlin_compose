package com.ganainy.gymmasterscompose.ui.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ganainy.gymmasterscompose.ui.models.UserStats

@Dao
interface StatsDao {
    @Query("SELECT * FROM stats WHERE userId = :userId")
    suspend fun getStatsByUserId(userId: String): UserStats?

    @Update
    suspend fun updateStats(stats: UserStats)
}