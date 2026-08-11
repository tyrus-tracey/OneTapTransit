package com.example.onetaptransit

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun TestButton(
    displayText: String,
    postTaskText: String,
    onButtonClick: () -> Unit,
    isTaskDone: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = onButtonClick)
        {
            Text(if (isTaskDone) postTaskText else displayText)
        }
    }
}
