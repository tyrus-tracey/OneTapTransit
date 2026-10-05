package com.example.onetaptransit.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onetaptransit.TransitStop
import com.example.onetaptransit.staticdata.VehicleStopTime

@Composable
fun NextArrivalsForStopDisplay(
    savedTransitStopsState: List<TransitStop>,
    nextArrivalsState: Map<Int, List<VehicleStopTime>>,
    onStopBannerClick: (Int) -> Unit
) {
    Column() {
        for (transitStop in savedTransitStopsState) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray),
                onClick = {
                    onStopBannerClick(transitStop.stopCode)
                },
                content = {
                    Text(
                        transitStop.stopCode.toString() + ": " + transitStop.externalStopName,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center
                    )
                }
            )

            if (nextArrivalsState.containsKey(transitStop.stopCode)) {
                val nextArrivals = nextArrivalsState.getValue(transitStop.stopCode)
                for (nextArrival in nextArrivals) {
                    if (!transitStop.ignoreList.contains(nextArrival.tripHeadsign)) {
                        NextArrivalDisplay(nextArrival)
                    }
                }
            }
        }
    }
}

@Composable
fun NextArrivalDisplay(
    nextArrival: VehicleStopTime
) {
    Box(modifier = Modifier
        .background(Color.Black)
        .padding(bottom = 1.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(android.graphics.Color.BLUE)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val headsignNumber = nextArrival.tripHeadsign.substringBefore(' ')
            val headsignName = nextArrival.tripHeadsign.substringAfter(' ')
            val textColor = Color(android.graphics.Color.rgb(250, 200, 40))

            Text(
                headsignNumber,
                color = textColor,
                modifier = Modifier
                    .fillMaxWidth(0.175f)
                    .padding(4.dp),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(24.sp, 32.sp),
                textAlign = TextAlign.End,
            )
            Text(
                headsignName,
                color = textColor,
                modifier = Modifier
                    .fillMaxWidth(0.60f)
                    .padding(start = 12.dp, end = 8.dp),
                textAlign = TextAlign.Start,
                fontSize = 24.sp
            )

            val arrivalTimeText =
                if (nextArrival.arrivalTime.time == 0L) {
                    "--:--:--"
                } else {
                    nextArrival.arrivalTime.toString()
                }
            val arrivalTimeTextColor = Color.Yellow
            Text(
                arrivalTimeText,
                color = arrivalTimeTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(8.sp, 32.sp),
                textAlign = TextAlign.End,
            )
        }
    }
}