package com.ganainy.gymmasterscompose.ui.models

import com.google.firebase.Timestamp

/**
 * Data class representing a hashtag.
 *
 * @property tag The text of the hashtag.
 * @property useCount The number of content associated with the hashtag.
 * @property lastUsed The timestamp when the hashtag was last used.
 */
data class Hashtag(
    val tag: String = "",
    val useCount: Int = 0,
    val lastUsed: Timestamp = Timestamp.now()
){
    companion object{
        const val HASHTAGS_COLLECTION = "hashtags"
        const val TAG = "tag"
        const val HASHTAG_USE_COUNT = "useCount"
        const val HASHTAG_LAST_USED = "lastUsed"
    }
}

/**
 * Extension function to convert a String to a Hashtag.
 *
 * This function creates a Hashtag object from the given String. The tag is set to the String value,
 * the useCount is initialized to 1, and the lastUsed timestamp is set to the current system time.
 *
 * @receiver The String to be converted to a Hashtag.
 * @return A Hashtag object with the tag set to the String value, useCount initialized to 1, and lastUsed set to the current system time.
 */
fun String.toHashtag(): Hashtag = Hashtag(tag = this,  useCount = 1, lastUsed = Timestamp.now())