package com.example.onetaptransit.composables

import android.view.KeyEvent.ACTION_DOWN
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import com.example.onetaptransit.SavedTransitStopsState
import com.example.onetaptransit.SimpleWorkState
import com.example.onetaptransit.TransitState

@Composable
fun TextInputWithTestButton(
    transitState: State<TransitState>,
    savedTransitStopsState: State<SavedTransitStopsState>,
    buttonDisplayText: String,
    buttonPostTaskText: String,
    onTextValueChange: (String) -> Unit,
    onButtonClick: () -> Unit,
    onQueryEventConsumed: () -> Unit,
    isTaskDone: Boolean
) {
    val focusManager = LocalFocusManager.current
    Row() {
        TextField(
            value = transitState.value.userEntryStopCode,
            onValueChange = { onTextValueChange(it) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onButtonClick()
                }
            ),
            modifier = Modifier
                .onPreviewKeyEvent {
                    if (it.key == Key.Enter && it.nativeKeyEvent.action == ACTION_DOWN) {
                        focusManager.clearFocus()
                        onButtonClick()
                        true
                    } else {
                        false
                    }
                }
        )
        TestButton(
            buttonDisplayText,
            buttonPostTaskText,
            { onButtonClick() },
            isTaskDone,
            savedTransitStopsState.value.addNewStopWorkState == SimpleWorkState.SUCCESS,
            savedTransitStopsState.value.addNewStopWorkState == SimpleWorkState.FAILED,
            onQueryEventConsumed
        )
    }
}