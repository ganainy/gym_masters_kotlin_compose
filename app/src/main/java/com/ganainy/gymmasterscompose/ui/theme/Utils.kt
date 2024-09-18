package com.ganainy.gymmasterscompose.ui.theme

import android.text.TextUtils
import android.util.Patterns
import kotlin.random.Random

object AppUtils {


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

}