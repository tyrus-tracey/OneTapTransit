package com.example.onetaptransit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NextArrivalDisplay(state: State<TransitState>) {
    val nextArrival = state.value.nextArrival
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(Color(android.graphics.Color.BLUE)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val headsignNumber = nextArrival.tripHeadsign.substringBefore(' ')
                val headsignName = nextArrival.tripHeadsign.substringAfter(' ')
                val textColor = Color(android.graphics.Color.rgb(250, 200, 40))
                Text(headsignNumber, fontSize = 72.sp, modifier = Modifier.padding(8.dp), color = textColor)
                Text(headsignName, fontSize = 32.sp, color = textColor)
            }
            Box(modifier = Modifier
                .fillMaxWidth()
                .background(Color(android.graphics.Color.YELLOW)),
            ) {
                Text(
                    nextArrival.arrivalTime.toString(),
                    fontSize = 48.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
            }
        }
    }
}