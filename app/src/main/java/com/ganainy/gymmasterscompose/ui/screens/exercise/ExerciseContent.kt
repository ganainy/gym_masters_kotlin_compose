package com.ganainy.gymmasterscompose.ui.screens.exercise

import CustomChip
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.ganainy.gymmasterscompose.ui.models.Exercise

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExerciseContent(exercise: Exercise) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExerciseGif(gifUrl = exercise.gifUrl)
        ExerciseTitle(name = exercise.name)
        ExerciseDetails(
            bodyPart = exercise.bodyPart,
            equipment = exercise.equipment,
            target = exercise.target
        )
        SecondaryMusclesSection(secondaryMuscles = exercise.secondaryMuscles)
        InstructionsSection(instructions = exercise.instructions)
    }
}

@Composable
private fun ExerciseGif(gifUrl: String) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                Glide.with(context)
                    .asGif()
                    .load(gifUrl)
                    .into(this)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun ExerciseTitle(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseDetails(bodyPart: String, equipment: String, target: String) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CustomChip(label = bodyPart)
        CustomChip(label = equipment)
        CustomChip(label = target)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SecondaryMusclesSection(secondaryMuscles: List<String>) {
    if (secondaryMuscles.isNotEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Secondary Muscles:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                secondaryMuscles.forEach { muscle ->
                    CustomChip(label = muscle)
                }
            }
        }
    }
}

@Composable
private fun InstructionsSection(instructions: List<String>) {
    if (instructions.isNotEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Instructions:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            instructions.forEachIndexed { index, instruction ->
                Text(
                    text = "${index + 1}. $instruction",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
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
        )
    )
}