package com.github.aivanovski.testswithme.android.presentation.core.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDropdownMenu(
    isEnabled: Boolean = true,
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (option: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {
            if (isEnabled) {
                isExpanded = !isExpanded
            }
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            readOnly = true,
            enabled = isEnabled,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selectedOption,
            label = {
                Text(text = label)
            },
            onValueChange = { },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) }
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            for (option in options) {
                DropdownMenuItem(
                    onClick = {
                        isExpanded = false
                        onOptionSelected.invoke(option)
                    },
                    text = {
                        Text(
                            text = option
                        )
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}