package com.example.onetaptransit

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
@Preview
fun TransitStopDisplay(transitStop: TransitStop = TransitStop()) {
    Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Transit Stop ID: ${transitStop.stopID}",
                fontSize = 30.sp,
                textAlign = TextAlign.Center
            )
            transitStop.nextArrivalTime?.let {
                Text(
                    text = "Next arrival: ${transitStop.nextArrivalTime}",
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}