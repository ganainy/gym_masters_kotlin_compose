package com.ganainy.gymmasterscompose.ui.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing cached likes in Room database
 */
@Dao
interface CachedLikeDao {
    @Query("SELECT * FROM cached_likes WHERE userId = :userId AND targetId = :targetId AND likeType = :type AND (postId = :postId OR postId IS NULL)")
    fun observeLike(userId: String, targetId: String, type: LikeType, postId: String? = null): Flow<CachedLike?>

    @Query("SELECT EXISTS(SELECT 1 FROM cached_likes WHERE userId = :userId AND targetId = :targetId AND likeType = :type AND isLiked = 1 AND (postId = :postId OR postId IS NULL))")
    suspend fun isLiked(userId: String, targetId: String, type: LikeType, postId: String? = null): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: CachedLike)

    @Query("DELETE FROM cached_likes WHERE id = :id")
    suspend fun deleteLike(id: String)

    @Query("SELECT * FROM cached_likes WHERE isPending = 1")
    suspend fun getPendingLikes(): List<CachedLike>

    @Query("SELECT * FROM cached_likes WHERE userId = :userId AND likeType = :type AND (postId = :postId OR postId IS NULL)")
    fun observeTypeLikes(userId: String, type: LikeType, postId: String? = null): Flow<List<CachedLike?>>

    @Query("SELECT * FROM cached_likes WHERE id = :likeId LIMIT 1") // Query by primary key 'id'
    suspend fun getLike(likeId: String): CachedLike? // Returns nullable CachedLike
}