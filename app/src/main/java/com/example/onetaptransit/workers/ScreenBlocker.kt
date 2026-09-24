package com.example.onetaptransit.workers

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun ScreenBlocker(text: String) {
    Box(modifier = Modifier
        .fillMaxSize()
        .zIndex(1.0f)
        .background(Color.Black.copy(alpha = 0.4f))
        .pointerInput(Unit) {
            detectTapGestures {} // Consume any input while Box is active
        }
    ) {
        Box(modifier = Modifier
            .width(256.dp)
            .height(128.dp)
            .background(Color.White)
        ) {
            Text(
                text,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}