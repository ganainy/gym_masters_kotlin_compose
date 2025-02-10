package com.ganainy.gymmasterscompose.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

interface IImageProcessor {
    suspend fun getImagePathFromGif( gifUrl: String): String?
}

// Implementation that requires context
class AndroidImageProcessor(private val context: Context) : IImageProcessor {


    // Function to take a screenshot from a GIF and return its path on the device
    override suspend fun getImagePathFromGif( gifUrl: String): String? {
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

}