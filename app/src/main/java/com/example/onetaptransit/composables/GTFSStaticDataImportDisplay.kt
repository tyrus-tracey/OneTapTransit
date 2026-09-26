package com.example.onetaptransit.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.onetaptransit.GTFSStaticDataImportProgressState
import com.example.onetaptransit.workers.StaticDataTableName

@Composable
fun GTFSStaticDataImportDisplay (
    gtfsStaticDataImportProgressState: GTFSStaticDataImportProgressState,
    onClickCancel: () -> Unit = {}
) {
    fun switchProgressValue(table: StaticDataTableName) : Int {
        return when (table) {
            StaticDataTableName.ROUTES -> gtfsStaticDataImportProgressState.progRoutes
            StaticDataTableName.TRIPS -> gtfsStaticDataImportProgressState.progTrips
            StaticDataTableName.CALENDAR -> gtfsStaticDataImportProgressState.progCalendar
            StaticDataTableName.CALENDAR_DATES -> gtfsStaticDataImportProgressState.progCalendarDates
            StaticDataTableName.STOPS -> gtfsStaticDataImportProgressState.progStops
            StaticDataTableName.STOP_TIMES -> gtfsStaticDataImportProgressState.progStopTimes
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .zIndex(1.0f)
        .background(Color.Black.copy(alpha = 0.4f))
        .pointerInput(Unit) {
            detectTapGestures {} // Consume any input behind this
        }
    ) {
        Box(modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth()
            .padding(32.dp)
            .background(Color.White)
        ) {
            Column() {
                Text(
                    "Importing GTFS Static Data",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                for (staticTable in StaticDataTableName.entries) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            staticTable.name,
                            fontSize = 16.sp,
                        )
                        Text(
                            switchProgressValue(staticTable).toString() + "%",
                            fontSize = 16.sp,
                        )
                    }

                    LinearProgressIndicator(
                        progress = { switchProgressValue(staticTable).toFloat() / 100.0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 0.dp)
                    )
                }

                Button(
                    onClick = onClickCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text="Cancel Import",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }


}

