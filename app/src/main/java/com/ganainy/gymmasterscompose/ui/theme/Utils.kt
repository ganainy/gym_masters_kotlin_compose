package com.ganainy.gymmasterscompose.ui.theme

import android.text.TextUtils
import android.util.Patterns
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.R
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



    //extension function for the map to remove by value instead of key
    fun removeByValue(map: MutableMap<String, String>, valueToRemove: String) {
        val entriesToRemove = map.entries.filter { it.value == valueToRemove }
        entriesToRemove.forEach { map.remove(it.key) }
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

    //generate random id
     fun generateRandomId(): String {
            val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            (1..10).map { chars.random() }.joinToString("")
        return  chars.toString()
    }

    //show snackbar
    @Composable
     fun ShowSnackbar(errorMessage: String, action: (() -> Unit)? = null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.BottomCenter)
                .padding(
                    bottom = WindowInsets
                        .navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding() + 16.dp
                )
        ) {
            Snackbar(
                modifier = Modifier.padding(horizontal = 16.dp),
                action = if (action != null) {
                    {
                        TextButton(onClick =  action) {
                            Text(text = stringResource(R.string.retry))
                        }
                    }
                } else null
            ) { Text(text = errorMessage) }
        }
    }

}