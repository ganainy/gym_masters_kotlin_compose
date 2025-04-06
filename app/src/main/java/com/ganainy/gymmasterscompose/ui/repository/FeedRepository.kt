package com.ganainy.gymmasterscompose.ui.repository

import android.util.Log
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost.Companion.POSTS_COLLECTION
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost.Companion.POST_CREATOR
import com.ganainy.gymmasterscompose.ui.models.post.PostByUserEntry.Companion.CREATED_AT
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

interface IFeedRepository {
    suspend fun getPaginatedFeed(
        // List of user IDs whose posts we want (current user + followed)
        relevantUserIds: List<String>,
        // Timestamp of the last post from the previous page for pagination cursor
        lastPostTimestamp: Timestamp? = null,
        // ID of the last post from the previous page (for tie-breaking if timestamps are equal)
        lastPostId: String? = null
    ): ResultWrapper<List<FeedPost>>
}

class FeedRepository(
    private val firestore: FirebaseFirestore,
) : IFeedRepository {
    companion object {
        // Number of posts to fetch per page
        const val PAGE_SIZE = 10
        // Firestore 'whereIn' query limit (currently 30)
        private const val WHERE_IN_LIMIT = 30
    }

    override suspend fun getPaginatedFeed(
        relevantUserIds: List<String>,
        lastPostTimestamp: Timestamp?,
        lastPostId: String?
    ): ResultWrapper<List<FeedPost>> {

        // Handle empty list of users to query for - no posts possible
        if (relevantUserIds.isEmpty()) {
            return ResultWrapper.Success(emptyList())
        }

        // Firestore 'whereIn' queries are limited (currently 30 items).
        // If a user follows more people, we need multiple queries.
        // TODO: Implement multi-query logic if relevantUserIds.size > WHERE_IN_LIMIT
        if (relevantUserIds.size > WHERE_IN_LIMIT) {
            Log.w("FeedRepository", "Querying for ${relevantUserIds.size} users, exceeding Firestore 'whereIn' limit of $WHERE_IN_LIMIT. Feed may be incomplete. Implement multi-query logic.")
            // For now, we'll query only the first chunk, but this is incorrect for > 30 follows
            // return ResultWrapper.Error(Exception("Cannot query feed for more than $WHERE_IN_LIMIT followed users yet.")) // Or truncate list
        }

        return try {
            // Base query on the posts collection
            var query: Query = firestore.collection(POSTS_COLLECTION)
                // Filter posts where the creator's ID is in the list of relevant users
                .whereIn("$POST_CREATOR.id", relevantUserIds.take(WHERE_IN_LIMIT)) // Use take() as temporary fix for >30 limit
                // Order by creation timestamp descending (newest first)
                .orderBy(CREATED_AT, Query.Direction.DESCENDING)
                // Add secondary ordering by ID for stable pagination if timestamps clash
                .orderBy("id", Query.Direction.DESCENDING)
                // Limit the number of posts per page
                .limit(PAGE_SIZE.toLong()) // Firestore limit takes Long

            // Apply pagination cursor if necessary
            if (lastPostTimestamp != null && lastPostId != null) {
                // Use startAfter with the values from the last document of the previous page
                // Note: We need the actual values used in orderBy
                query = query.startAfter(lastPostTimestamp, lastPostId)
            }

            // Execute the query
            val snapshot = query.get().await()

            // Map documents to FeedPost objects
            val posts = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(FeedPost::class.java)
                } catch (e: Exception) {
                    Log.e("FeedRepository", "Error converting document ${document.id} to FeedPost", e)
                    null // Skip documents that fail conversion
                }
            }

            ResultWrapper.Success(posts)

        } catch (e: Exception) {
            Log.e("FeedRepository", "Error fetching paginated feed from Firestore", e)
            ResultWrapper.Error(Exception("Error fetching feed: ${e.message}"))
        }
    }

}

