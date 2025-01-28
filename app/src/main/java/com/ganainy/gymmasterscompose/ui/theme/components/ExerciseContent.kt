package com.ganainy.gymmasterscompose.ui.theme.components

import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseContent(exercise: Exercise) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the GIF with Glide
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    Glide.with(context)
                        .asGif()
                        .load(exercise.gifUrl)
                        .into(this)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display the exercise name
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Display body part, equipment, and target
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomChip(label = exercise.bodyPart)
            CustomChip(label = exercise.equipment)
            CustomChip(label = exercise.target)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display secondary muscles
        if (exercise.secondaryMuscles.isNotEmpty()) {
            Text(
                text = "Secondary Muscles:",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                exercise.secondaryMuscles.forEach { muscle ->
                    CustomChip(label = muscle)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display instructions
        if (exercise.instructions.isNotEmpty()) {
            Text(
                text = "Instructions:",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            exercise.instructions.forEachIndexed { index, instruction ->
                Text(
                    text = "${index + 1}. $instruction",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}





@Preview(showBackground = true)
@Composable
private fun ExerciseContentPreview() {
    ExerciseContent(
        Exercise(
            bodyPart = "Legs",
            equipment = "Machine",
            gifUrl = "url_to_squat_machine_gif",
            screenshotPath = "path_to_squat_machine_screenshot",
            id = "1",
            name = "Squat (Machine)",
            target = "Quadriceps",
            secondaryMuscles = listOf("Glutes", "Hamstrings"),
            instructions = listOf(
                "Set the machine to your height.",
                "Place your shoulders under the pads.",
                "Push through your heels to lift."
            )
        ),
    )
}