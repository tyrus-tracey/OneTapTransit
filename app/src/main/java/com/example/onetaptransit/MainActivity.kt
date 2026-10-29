package com.example.onetaptransit

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.onetaptransit.composables.GTFSStaticDataImportDisplay
import com.example.onetaptransit.composables.TestButton
import com.example.onetaptransit.composables.TextInputWithTestButton
import com.example.onetaptransit.composables.UserSavedStopsDisplay
import com.example.onetaptransit.notifications.cancelNotification
import com.example.onetaptransit.ui.theme.OneTapTransitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OneTapTransitTheme {
                val transitViewModel: TransitViewModel by viewModels()
                val gtfsStaticDataImportState by transitViewModel.gtfsStaticDataImportState.collectAsStateWithLifecycle()
                val transitState = transitViewModel.transitState.collectAsStateWithLifecycle()
                val nextArrivalsState = transitViewModel.nextArrivalsState.collectAsStateWithLifecycle()
                val savedTransitStopsState = transitViewModel.savedTransitStopsState.collectAsStateWithLifecycle()

                if (gtfsStaticDataImportState.isLoading) {
                    GTFSStaticDataImportDisplay(
                        gtfsStaticDataImportState,
                        { transitViewModel.cancelStaticDataImport(applicationContext) }
                    )
                }

                Scaffold(
                    modifier = Modifier.imePadding(),
                    bottomBar = {
                        TextInputWithTestButton(
                            transitState,
                            savedTransitStopsState,
                            "Add Stop",
                            "Add Stop",
                            transitViewModel::updateUserEntryStopCode,
                            {
                                transitViewModel.saveTransitStop()
                            },
                            {
                                transitViewModel.updateAddStopState(SimpleWorkState.STANDBY)
                            },
                            false
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                    ) {
                        var staticDataUpdated by rememberSaveable { mutableStateOf(false) }
                        var realtimeUpdated by rememberSaveable { mutableStateOf(false) }
                        var tablesTruncated by rememberSaveable { mutableStateOf(false) }

                        Row() {
                            TestButton(
                                "Static Data",
                                "Static Data Downloaded",
                                {
                                    transitViewModel.updateStaticData(this@MainActivity) {
                                        staticDataUpdated = true
                                        cancelNotification(999, applicationContext)
                                    }
                                },
                                staticDataUpdated,
                                false,
                                false,
                                {}
                            )

                            TestButton(
                                "Realtime",
                                "Realtime Downloaded",
                                {
                                    transitViewModel.updateRealtimeFeed(this@MainActivity) {
                                        realtimeUpdated = true
                                    }
                                },
                                realtimeUpdated,
                                false,
                                false,
                                {}
                            )

                            TestButton(
                                "Truncate Stops",
                                "Truncate Stops",
                                onButtonClick = {
                                    transitViewModel.truncateAllTables() {
                                        tablesTruncated = true
                                    }
                                },
                                false,
                                tablesTruncated,
                                false,
                                {
                                    tablesTruncated = false
                                }
                            )
                        }



                        UserSavedStopsDisplay(
                            savedTransitStopsState.value.userSavedTransitStops,
                            nextArrivalsState,
                            onStopBannerClick = { stopCode ->
                                transitViewModel.queryNextArrival(
                                    stopCode,
                                    onQueryResponse = {}
                                )
                            },
                            onStopBannerLongClick = { stopCode, stopName ->
                                transitViewModel.updateLongClickedTransitStop(stopCode, stopName)
                                transitViewModel.updateShowRemoveStopAlertDialog(true)
                            }
                        )

                        if (savedTransitStopsState.value.showRemoveStopAlertDialog) {
                            val stopCode = savedTransitStopsState.value.longClickedStopCode
                            val stopName = savedTransitStopsState.value.longClickedStopName
                            if (stopCode == null || stopName == null) {
                                Log.e("MAIN ACTIVITY", "Attempted to create Alert Dialog, but selected transitStop state value is null: stopCode: $stopCode, stopName: $stopName")
                            } else {
                                AlertDialog(
                                    onDismissRequest = { transitViewModel.updateShowRemoveStopAlertDialog(false) },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            transitViewModel.removeTransitStopByStopCode(stopCode)
                                            transitViewModel.updateShowRemoveStopAlertDialog(false)
                                        }) { Text("OK") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = {
                                            transitViewModel.updateShowRemoveStopAlertDialog(false)
                                        }) { Text("Cancel") }
                                    },
                                    title = { Text(text = "Remove Stop?") },
                                    text = {
                                        Text(
                                            text = "$stopCode $stopName",
                                            fontSize = 16.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}