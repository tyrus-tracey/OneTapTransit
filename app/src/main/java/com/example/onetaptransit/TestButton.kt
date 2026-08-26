package com.example.onetaptransit

import androidx.compose.animation.Animatable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@Composable
fun TestButton(
    displayText: String,
    postTaskText: String,
    onButtonClick: () -> Unit,
    isTaskDone: Boolean,
    eQuerySuccess: Boolean,
    eQueryFailed: Boolean,
    onQueryEventConsumed: () -> Unit
) {
    val colorDefault = MaterialTheme.colorScheme.primary
    val colorFlash = remember { Animatable(colorDefault) }

    LaunchedEffect(eQuerySuccess, eQueryFailed) {
        if (eQuerySuccess) {
            colorFlash.animateTo(Color(android.graphics.Color.GREEN))
            colorFlash.animateTo(colorDefault)
        } else if (eQueryFailed) {
            colorFlash.animateTo(Color(android.graphics.Color.RED))
            colorFlash.animateTo(colorDefault)
        }

        onQueryEventConsumed()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(containerColor = colorFlash.value)
        )
        {
            Text(if (isTaskDone) postTaskText else displayText)
        }
    }
}
