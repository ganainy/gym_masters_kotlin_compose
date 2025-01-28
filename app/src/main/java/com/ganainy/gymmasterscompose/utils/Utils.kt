package com.ganainy.gymmasterscompose.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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

    // Function to take a screenshot from a GIF and return its path on the device
    suspend fun getImagePathFromGif(context: Context, gifUrl: String): String? {
        val fileName = gifUrl.split("/").last().let {
            if (it.contains(".")) it else "$it.png"
        }
        val file = File(context.externalCacheDir, fileName)

        return try {
            val bitmap = loadBitmapFromGif(context, gifUrl) // Load the bitmap from the GIF
            if (bitmap != null && saveImageToDisk(bitmap, file)) {
                file.path // Return the file path if saving is successful
            } else {
                null // Return null if saving fails
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Function to load a bitmap from a GIF using Glide
    private suspend fun loadBitmapFromGif(context: Context, gifUrl: String): Bitmap? {
        return suspendCancellableCoroutine { continuation ->
            Glide.with(context)
                .asBitmap()
                .load(gifUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        if (!continuation.isCancelled) {
                            continuation.resume(resource) {} // Resume the coroutine with the bitmap
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        if (!continuation.isCancelled) {
                            continuation.resume(null) {} // Resume with null if the resource is cleared
                        }
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        if (!continuation.isCancelled) {
                            continuation.resume(null) {} // Resume with null if loading fails
                        }
                    }
                })
        }
    }

    // Function to save a bitmap to disk
    private fun saveImageToDisk(bitmap: Bitmap, file: File): Boolean {
        var out: FileOutputStream? = null
        return try {
            out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, out) // Save as PNG
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try {
                out?.close()
            } catch (ignore: IOException) {
            }
        }
    }

    // Function to get a bitmap from a file path
    fun getBitmapFromPath(path: String): Bitmap? {
        return try {
            android.graphics.BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}