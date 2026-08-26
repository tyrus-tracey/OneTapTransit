package com.example.onetaptransit

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun TextInputWithTestButton(
    transitState: State<TransitState>,
    buttonDisplayText: String,
    buttonPostTaskText: String,
    onTextValueChange: (String) -> Unit,
    onButtonClick: () -> Unit,
    onQueryEventConsumed: () -> Unit,
    isTaskDone: Boolean
) {
    Row() {
        TextField(
            value = transitState.value.userEntryStopCode,
            onValueChange = { onTextValueChange(it) },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TestButton(
            buttonDisplayText,
            buttonPostTaskText,
            { onButtonClick() },
            isTaskDone,
            transitState.value.eQuerySuccess,
            transitState.value.eQueryFailed,
            onQueryEventConsumed
        )
    }
}