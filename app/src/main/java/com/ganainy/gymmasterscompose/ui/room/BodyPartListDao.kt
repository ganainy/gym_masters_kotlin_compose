package com.ganainy.gymmasterscompose.ui.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ganainy.gymmasterscompose.ui.models.BodyPart

@Dao
interface BodyPartListDao {
    @Query("SELECT * FROM body_part_list")
    suspend fun getBodyPartList(): List<BodyPart>

    @Insert
    suspend fun insertBodyPartList(bodyPartList: List<BodyPart>)
}