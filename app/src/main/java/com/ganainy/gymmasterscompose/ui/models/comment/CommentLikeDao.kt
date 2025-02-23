package com.ganainy.gymmasterscompose.ui.models.comment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentLikeDao {

    @Query("SELECT * FROM comment_likes WHERE user_id = :userId AND comment_id = :commentId AND post_id = :postId LIMIT 1")
     fun isCommentLiked(userId: String,commentId: String, postId: String): Flow<CommentLikeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: CommentLikeEntity)

    @Query("DELETE FROM comment_likes WHERE id = :likeId")
    suspend fun removeLike(likeId: String)

}