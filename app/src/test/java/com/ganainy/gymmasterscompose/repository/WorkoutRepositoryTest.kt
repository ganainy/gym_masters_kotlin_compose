
import android.net.Uri
import com.ganainy.gymmasterscompose.ui.theme.repository.ResultWrapper
import com.ganainy.gymmasterscompose.ui.theme.repository.WorkoutRepository
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class WorkoutRepositoryTest {

    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var workoutRepository: WorkoutRepository

    @Before
    fun setUp() {
        firebaseStorage = mock()
        storageReference = mock()
        whenever(firebaseStorage.reference).thenReturn(storageReference)
        workoutRepository = WorkoutRepository(mock(), firebaseStorage)
    }

    @Test
    @Config(sdk = [28])
    fun `uploadWorkoutCoverImage should return success with URL`() = runBlocking {
        val imagePath = "file://path/to/image"
        val downloadUrl = "https://firebase.storage/download/url"
        val uri = Uri.parse(imagePath)

        val imageRef = mock<StorageReference>()
        val downloadUri = mock<Uri>()
        whenever(downloadUri.toString()).thenReturn(downloadUrl)
        whenever(storageReference.child(anyString())).thenReturn(imageRef)
        whenever(imageRef.putFile(uri)).thenReturn(mock())
        whenever(imageRef.downloadUrl.await()).thenReturn(downloadUri)

        val result = workoutRepository.uploadWorkoutCoverImage(imagePath)

        assertEquals(ResultWrapper.Success(downloadUrl), result)
    }

    @Test
    fun `uploadWorkoutCoverImage should return error on failure`() = runBlocking {
        val imagePath = "file://path/to/image"
        val uri = Uri.parse(imagePath)

        val imageRef = mock<StorageReference>()
        whenever(firebaseStorage.reference.child(anyString())).thenReturn(imageRef)
        whenever(imageRef.putFile(uri)).thenThrow(RuntimeException("Upload failed"))

        val result = workoutRepository.uploadWorkoutCoverImage(imagePath)

        assert(result is ResultWrapper.Error)
    }
}