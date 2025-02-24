package com.ganainy.gymmasterscompose.ui.screens.create_workout.composables

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.models.workout.Workout
import com.ganainy.gymmasterscompose.utils.MockData.sampleWorkout
import kotlinx.coroutines.launch

@Composable
internal fun WorkoutCoverImageContent(
    workout: Workout,
    onEditWorkout: (Workout) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    // Animation for image scaling and fade-in
    val animatedScale by animateFloatAsState(
        targetValue = if (workout.imagePath.isNotEmpty()) 1f else 0.95f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (workout.imagePath.isNotEmpty()) 1f else 0.7f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                onEditWorkout(workout.copy(imagePath = uri.toString()))
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image preview box with shadow and rounded corners
        Box(
            modifier = Modifier
                .size(300.dp) // Fixed size for 1:1 aspect ratio
                .scale(animatedScale)
                .alpha(animatedAlpha)
                .clip(RoundedCornerShape(16.dp))
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable{
                    launcher.launch("image/*") // Launch image picker on click
                },
            contentAlignment = Alignment.Center
        ) {
            if (workout.imagePath.isNotEmpty()) {
                // Display selected image with crossfade animation
                AsyncImage(
                    model = workout.imagePath,
                    contentDescription = "Workout cover image",
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop, // Crop to fill the box
                    placeholder = painterResource(R.drawable.dumbbell),
                    error = painterResource(R.drawable.dumbbell)
                )

                // Remove button
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            onEditWorkout(workout.copy(imagePath = ""))
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove image",
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            } else {
                // Placeholder with hint for user
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Tap to select image",
                            modifier = Modifier
                                .size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to add image",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutCoverImageContentNoImageSelected_Preview() {
    WorkoutCoverImageContent(
        workout = sampleWorkout, onEditWorkout = { }
    )
}

@Preview
@Composable
private fun WorkoutCoverImageContentImageSelected_Preview() {
    WorkoutCoverImageContent(
        workout = sampleWorkout.copy(imagePath = "https://firebasestorage.googleapis.com/v0/b/gym-masters.appspot.com/o/workouts%2F-Myv9QdZ0nK7WYV3JpQJk%2Fimage?alt=media&token=0e0f9f5b-9d3e-4e1e-94f1-6c9e0d7a5f0c"),
        onEditWorkout = { }
    )
}