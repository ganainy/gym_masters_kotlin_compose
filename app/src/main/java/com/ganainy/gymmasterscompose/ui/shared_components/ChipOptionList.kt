package com.ganainy.gymmasterscompose.ui.shared_components

import CustomChip
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.ui.models.BodyPart
import com.ganainy.gymmasterscompose.ui.models.Equipment
import com.ganainy.gymmasterscompose.ui.models.TargetMuscle


@OptIn(ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
@Composable
fun <T> ChipOptionList(
    label: String,
    options: List<T>?,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    isExpanded: Boolean? = null // Optional parameter for previews
) {
    // Use provided isExpanded for previews, otherwise manage state internally
    val expanded = isExpanded ?: remember { mutableStateOf(false) }.value
    val isExpandedState = remember { mutableStateOf(expanded) }

    // Update internal state if isExpanded is null (runtime usage)
    if (isExpanded == null) {
        isExpandedState.value = expanded
    }

        Box(
            modifier = Modifier
                .padding(8.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Expand/Collapse Button with animation
                FilledIconButton(
                    onClick = { isExpandedState.value = !isExpandedState.value },
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterVertically),
                    shape = RoundedCornerShape(50),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (isExpandedState.value) Icons.Default.Close else Icons.Default.ArrowDropDown,
                        contentDescription = if (isExpandedState.value) "Close" else "Expand",
                        modifier = Modifier
                            .rotate(if (isExpandedState.value) 180f else 0f)
                            .animateContentSize(animationSpec = tween(300))
                    )
                }

                // Label with enhanced styling
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 8.dp)
                )

                // Chip Options with animation
                AnimatedVisibility(
                    visible = isExpandedState.value && !options.isNullOrEmpty(),
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        options?.forEach { option ->
                            val optionName = when (option) {
                                is BodyPart -> option.name
                                is TargetMuscle -> option.name
                                is Equipment -> option.name
                                else -> "Unknown"
                            }

                            CustomChip(
                                label = optionName,
                                isSelected = option == selectedOption,
                                onClick = { onOptionSelected(option) }
                            )
                        }
                    }
                }
            }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
fun ChipOptionListCollapsedPreview() {
    val sampleOptions = listOf(
        BodyPart("Arms"),
        BodyPart("Legs"),
        BodyPart("Back")
    )
    var selectedOption by remember { mutableStateOf<BodyPart?>(null) }

    ChipOptionList(
        label = "Body Parts",
        options = sampleOptions,
        selectedOption = selectedOption,
        onOptionSelected = { selectedOption = it },
        isExpanded = false
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
fun ChipOptionListExpandedPreview() {
    val sampleOptions = listOf(
        BodyPart("Arms"),
        BodyPart("Legs"),
        BodyPart("Back")
    )
    var selectedOption by remember { mutableStateOf<BodyPart?>(null) }

    ChipOptionList(
        label = "Body Parts",
        options = sampleOptions,
        selectedOption = selectedOption,
        onOptionSelected = { selectedOption = it },
        isExpanded = true
    )
}

