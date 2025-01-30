package com.ganainy.gymmasterscompose.ui.theme.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ganainy.gymmasterscompose.ui.theme.models.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)
}