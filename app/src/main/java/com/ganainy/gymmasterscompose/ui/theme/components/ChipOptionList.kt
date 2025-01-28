package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.ui.theme.models.BodyPart
import com.ganainy.gymmasterscompose.ui.theme.models.Equipment
import com.ganainy.gymmasterscompose.ui.theme.models.TargetMuscle


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChipOptionList(
    label: String,
    options: List<T>?,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Expand/Collapse Button
        IconButton(onClick = { isExpanded = !isExpanded }) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.ArrowDropDown,
                contentDescription = if (isExpanded) "Close" else "Expand"
            )
        }

        // Label
        Text(text = label, fontWeight = FontWeight.SemiBold)

        // Chip Options (if expanded)
        if (isExpanded && !options.isNullOrEmpty()) {
            options.forEach { option ->
                val optionName = when (option) {
                    is BodyPart -> option.name
                    is TargetMuscle -> option.name
                    is Equipment -> option.name
                    else -> "Unknown"
                }

                CustomChip(
                    label = optionName,
                    isSelected = option == selectedOption, // Correctly determine selection
                    onClick = { onOptionSelected(option) } // Pass the actual option
                )
            }
        }
    }
}
