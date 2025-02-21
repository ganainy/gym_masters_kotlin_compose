
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ganainy.gymmasterscompose.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

/**
 * Interface defining image processing operations
 */
interface IImageProcessor {
    /**
     * Gets or generates an image path from a GIF URL.
     * If the image already exists in cache, returns its path.
     * Otherwise, downloads the GIF, extracts a frame, and saves it.
     *
     * @param gifUrl The URL of the GIF to process
     * @return The path to the saved image or null if processing fails
     */
    suspend fun getImagePathFromGif(gifUrl: String): String?

}

/**
 * Android-specific implementation of IImageProcessor
 * Handles GIF processing, image caching
 *
 * @property context Android Context for accessing system resources
 */
class AndroidImageProcessor @Inject constructor(private val context: Context) : IImageProcessor {

    companion object {
        private const val COMPRESSION_QUALITY = 85
        private const val DEFAULT_EXTENSION = "png"
    }

    /**
     * Gets or generates an image path from a GIF URL with caching
     *
     * @param gifUrl URL of the GIF to process
     * @return Path to the saved image or null if processing fails
     */
    override suspend fun getImagePathFromGif(gifUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            // Generate consistent filename from URL
            val fileName = generateFileName(gifUrl)
            val file = File(context.externalCacheDir, fileName)

            // Return existing file path if already cached
            if (file.exists() && file.length() > 0) {
                return@withContext file.path
            }

            // Download and process GIF if not cached
            val bitmap = loadBitmapFromGif(context, gifUrl)
            if (bitmap != null && saveImageToDisk(bitmap, file)) {
                file.path
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AndroidImageProcessor", "Error processing GIF", e)
            null
        }
    }

    /**
     * Generates a consistent filename from a GIF URL
     *
     * @param gifUrl The URL to generate filename from
     * @return Generated filename with appropriate extension
     */
    private fun generateFileName(gifUrl: String): String {
        val lastSegment = gifUrl.split("/").last()
        return if (lastSegment.contains(".")) {
            lastSegment.replace(Regex("\\.[^.]*$"), ".$DEFAULT_EXTENSION")
        } else {
            "$lastSegment.$DEFAULT_EXTENSION"
        }
    }

    /**
     * Loads a bitmap from a GIF URL using Glide
     *
     * @param context Android Context for Glide
     * @param gifUrl URL of the GIF to load
     * @return Loaded Bitmap or null if loading fails
     */
    private suspend fun loadBitmapFromGif(context: Context, gifUrl: String): Bitmap? {
        return suspendCancellableCoroutine { continuation ->
            Glide.with(context)
                .asBitmap()
                .load(gifUrl)
                .error(R.drawable.dumbbell) // Add placeholder for error cases
                .timeout(30000) // 30 second timeout
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        if (!continuation.isCancelled) {
                            continuation.resume(resource) {
                                resource.recycle()
                            }
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        if (!continuation.isCancelled) {
                            continuation.resume(null) {}
                        }
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        if (!continuation.isCancelled) {
                            Log.e("AndroidImageProcessor", "Failed to load GIF: $gifUrl")
                            continuation.resume(null) {}
                        }
                    }
                })
        }
    }

    /**
     * Saves a bitmap to disk with error handling
     *
     * @param bitmap Bitmap to save
     * @param file Destination file
     * @return true if save successful, false otherwise
     */
    private fun saveImageToDisk(bitmap: Bitmap, file: File): Boolean {
        var outputStream: FileOutputStream? = null
        return try {
            // Ensure parent directories exist
            file.parentFile?.mkdirs()

            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, outputStream)
            true
        } catch (e: Exception) {
            Log.e("AndroidImageProcessor", "Error saving image to disk", e)
            // Delete partially written file if save failed
            file.delete()
            false
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                Log.e("AndroidImageProcessor", "Error closing output stream", e)
            }
        }
    }


}

/**
 * Extension function to check if an image file exists in cache
 */
fun IImageProcessor.isImageCached(gifUrl: String,context: Context): Boolean {
    val fileName = gifUrl.split("/").last().let {
        if (it.contains(".")) it.replace(Regex("\\.[^.]*$"), ".png") else "$it.png"
    }
    return File(context.externalCacheDir, fileName).exists()
}

