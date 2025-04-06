package com.ganainy.gymmasterscompose.ui.repository

import android.util.Log
import com.ganainy.gymmasterscompose.ui.models.Hashtag
import com.ganainy.gymmasterscompose.ui.models.Hashtag.Companion.HASHTAGS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.Hashtag.Companion.HASHTAG_LAST_USED
import com.ganainy.gymmasterscompose.ui.models.Hashtag.Companion.HASHTAG_USE_COUNT
import com.ganainy.gymmasterscompose.ui.models.toHashtag
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface IHashtagRepository {
    /**
     * Adds operations to update/create hashtags to the provided WriteBatch.
     * Increments counts for existing tags, creates new ones.
     *
     * @param batch The Firestore WriteBatch to add operations to.
     * @param tags The list of tags being used.
     */
    suspend fun addHashtagUpdatesToBatch(batch: WriteBatch, tags: List<String>)

    /**
     * Adds operations to decrement hashtag counts to the provided WriteBatch.
     * Does NOT handle deletion at count 0 TODO (recommend Functions trigger for that).
     *
     * @param batch The Firestore WriteBatch to add operations to.
     * @param tags The list of tags whose usage is being removed/decremented.
     */
    suspend fun addHashtagDecrementToBatch(batch: WriteBatch, tags: List<String>)
}

class HashtagRepository @Inject constructor(
    private val firestore: FirebaseFirestore
): IHashtagRepository {

    private val hashtagsCollection = firestore.collection(HASHTAGS_COLLECTION)

    // Helper to load existing hashtags using Firestore
    private suspend fun loadHashTagData(tags: List<String>): List<Hashtag> {
        if (tags.isEmpty()) return emptyList()
        return try {
            // Use 'whereIn' query - limited to 30 tags per query
            // TODO: Handle cases where tags.size > 30 by chunking queries
            val querySnapshot = hashtagsCollection.whereIn(Hashtag.TAG, tags.take(30)).get().await()
            querySnapshot.documents.mapNotNull { it.toObject(Hashtag::class.java) }
        } catch (e: Exception) {
            Log.e("HashtagRepository", "Error fetching hashtags: ${tags.joinToString()}", e)
            emptyList()
        }
    }

    override suspend fun addHashtagUpdatesToBatch(batch: WriteBatch, tags: List<String>) {
        if (tags.isEmpty()) return

        try {
            val uniqueTags = tags.map { it.lowercase().trim() }.filter { it.isNotEmpty() }.distinct()
            if(uniqueTags.isEmpty()) return

            // Fetch existing data for these tags
            val existingHashTags = loadHashTagData(uniqueTags)
            val existingTagMap = existingHashTags.associateBy { it.tag } // Map tag name to Hashtag object

            uniqueTags.forEach { tag ->
                val docRef = hashtagsCollection.document(tag) // Use tag name as document ID

                if (existingTagMap.containsKey(tag)) {
                    // Update existing hashtag count and timestamp
                    batch.update(docRef, HASHTAG_USE_COUNT, FieldValue.increment(1))
                    batch.update(docRef, HASHTAG_LAST_USED, System.currentTimeMillis()) // Or FieldValue.serverTimestamp()
                } else {
                    // Create new hashtag document
                    val newHashtag = tag.toHashtag() // Assumes this sets count to 1 and timestamp
                    batch.set(docRef, newHashtag)
                }
            }
        } catch (e: Exception) {
            Log.e("HashtagRepository", "Error preparing hashtag updates for batch", e)
            // Consider how to handle errors here - should it prevent the whole batch?
            // Re-throwing might be appropriate if hashtag updates are critical.
            // throw e
        }
    }

    override suspend fun addHashtagDecrementToBatch(batch: WriteBatch, tags: List<String>) {
        if (tags.isEmpty()) return

        try {
            val uniqueTags = tags.map { it.lowercase().trim() }.filter { it.isNotEmpty() }.distinct()
            if(uniqueTags.isEmpty()) return

            // For simple decrement, we don't strictly need to load first,
            // but doing so prevents unnecessary updates for non-existent tags.
            // Alternatively, just apply updates and let non-existent ones fail silently or handle later.
            // Let's just apply the decrements. Firestore handles increments on non-existent fields gracefully (sets to value).
            uniqueTags.forEach { tag ->
                val docRef = hashtagsCollection.document(tag)
                // Decrement the usage count. If doc/field doesn't exist, this might set it to -1.
                batch.update(docRef, HASHTAG_USE_COUNT, FieldValue.increment(-1))

                // TODO: Set up a database trigger (Cloud Function) in Firebase
                // to delete tags when usageCount reaches 0 or less.
                // Checking and deleting here is prone to race conditions.
            }
        } catch (e: Exception) {
            Log.e("HashtagRepository", "Error preparing hashtag decrements for batch", e)
            // Consider re-throwing if needed
            // throw e
        }
    }

}