package com.ganainy.gymmasterscompose.utils

import android.content.Context
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import kotlin.random.Random

object Utils {


    fun isValidEmail(email: String): Boolean {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
                )
    }

    fun isValidFieldLength(field: String, length: Int): Boolean {
        return (field.trim { it <= ' ' }
            .isEmpty()) || field.length >= length
    }

    // function for providing a random username
    fun generateRandomUsername(): String {
        val words = listOf(
            "ninja", "pirate", "wizard", "panda", "robot",
            "unicorn", "dragon", "zombie", "viking", "alien"
        )

        val colors = listOf(
            "red", "blue", "green", "yellow", "purple",
            "orange", "black", "white", "silver", "gold"
        )

        val word = words.random()
        val color = colors.random()
        val number = Random.nextInt(100, 999)

        return "$color$word$number"
    }

    //TimeAgo library
    @Composable
    fun formatRelativeTime(timestamp: Long): String {
        val currentLocale = LocalContext.current.resources.configuration.locales[0]
        val timeAgoMessages = remember(currentLocale) {
            TimeAgoMessages.Builder().withLocale(currentLocale).build()
        }
        return TimeAgo.using(timestamp, timeAgoMessages)
    }

    // TimeAgo library for Firestore Timestamp
    @Composable
    fun formatRelativeTimeFromFireStoreTimeStamp(timestamp: com.google.firebase.Timestamp): String {
        val currentLocale = LocalContext.current.resources.configuration.locales[0]
        val timeAgoMessages = remember(currentLocale) {
            TimeAgoMessages.Builder().withLocale(currentLocale).build()
        }
        return TimeAgo.using(timestamp.toDate().time, timeAgoMessages)
    }

    //generate random id
     fun generateRandomId(prefix:String): String {
            val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            val randomString = (1..10).map { chars.random() }.joinToString("")
        return  "$prefix _$randomString"
    }

    //show toast
    fun showToast(
        context: Context,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        Toast.makeText(context, message, duration).show()
    }


     fun extractHashtags(content: String): List<String> {
        return content.split("\\s+".toRegex())
            .filter { it.startsWith("#") && it.length > 1 }
            .map { it.substring(1) } // Remove the # symbol
            .distinct() // Remove duplicates
    }

}