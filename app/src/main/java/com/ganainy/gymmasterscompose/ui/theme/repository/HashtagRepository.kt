package com.ganainy.gymmasterscompose.ui.theme.repository

import android.util.Log
import com.ganainy.gymmasterscompose.ui.theme.models.Hashtag
import com.ganainy.gymmasterscompose.ui.theme.models.Hashtag.Companion.HASHTAGS_COLLECTION
import com.ganainy.gymmasterscompose.ui.theme.models.Hashtag.Companion.HASHTAG_LAST_USED
import com.ganainy.gymmasterscompose.ui.theme.models.Hashtag.Companion.HASHTAG_USE_COUNT
import com.ganainy.gymmasterscompose.ui.theme.models.toHashtag
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface IHashtagRepository {
    suspend fun updateHashtags(tags: List<String>): Map<String, Any>
    fun deleteOrDecreaseHashtags(tags: List<String>): Map<out String, Any?>
}

class HashtagRepository @Inject constructor(
    private val database: FirebaseDatabase
): IHashtagRepository {
    private suspend fun loadHashTagData(tags: List<String>): List<Hashtag> {
        return try {
            val tagsRef = database.getReference(HASHTAGS_COLLECTION)
            val snapshot = tagsRef.get().await()
            if (snapshot.exists()) {
                snapshot.children.mapNotNull { it.getValue(Hashtag::class.java) }
                    .filter { it.tag in tags }
            } else {
                emptyList<Hashtag>()
            }
        } catch (e: Exception) {
            Log.e("DataRepository", "Error fetching hashTags: ${e.message}")
            emptyList<Hashtag>()
        }
    }

    override suspend fun updateHashtags(tags: List<String>): Map<String, Any> {
        val updates = mutableMapOf<String, Any>()

        if (tags.isNotEmpty()) {
            val existingHashTags = loadHashTagData(tags)
            val existingTagNames = existingHashTags.map { it.tag }.toSet()

            // Update count for existing hashtags, update last used
            existingHashTags.forEach { hashtag ->
                updates["$HASHTAGS_COLLECTION/${hashtag.tag}/$HASHTAG_USE_COUNT"] = ServerValue.increment(1)
                updates["$HASHTAGS_COLLECTION/${hashtag.tag}/$HASHTAG_LAST_USED"] = System.currentTimeMillis()
            }

            // Create new hashtags
            tags.filterNot { it in existingTagNames }.forEach { newTag ->
                updates["$HASHTAGS_COLLECTION/$newTag"] = newTag.toHashtag()
            }
        }

        return updates
    }

    /**
     * Decrements the usage count of hashtags or deletes them if no longer used
     * @param tags List of hashtags to process
     * @return Map of database updates to perform
     */
    override fun deleteOrDecreaseHashtags(tags: List<String>): Map<String, Any?> {
        val updates = mutableMapOf<String, Any?>()
        tags.forEach { tag ->
            // Reference to the tag in the database
            val tagRef = database.reference.child(HASHTAGS_COLLECTION).child(tag)

            updates["/$HASHTAGS_COLLECTION/$tag/$HASHTAG_USE_COUNT"] = ServerValue.increment(-1)


            // TODO set up a database trigger in Firebase to delete tags when usageCount reaches 0
            // This is more reliable than trying to check and delete here
        }

        return updates
    }

}