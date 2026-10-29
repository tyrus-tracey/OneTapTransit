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

                        var showRemoveStopAlertDialog by rememberSaveable { mutableStateOf(false) }
                        var tempStopCode : String? by rememberSaveable { mutableStateOf("") }
                        var tempStopName : String? by rememberSaveable { mutableStateOf("") }

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
                                tempStopCode = stopCode
                                tempStopName = stopName
                                showRemoveStopAlertDialog = true
                            }
                        )

                        if (showRemoveStopAlertDialog) {
                            if (tempStopCode == null || tempStopName == null) {
                                Log.e("MAIN ACTIVITY", "Attempted to create Alert Dialog, but selected transitStop state value is null: tempStopCode: $tempStopCode, tempStopName: $tempStopName")
                            } else {
                                AlertDialog(
                                    onDismissRequest = { showRemoveStopAlertDialog = false },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            transitViewModel.removeTransitStopByStopCode(
                                                tempStopCode!!
                                            )
                                            tempStopCode
                                            showRemoveStopAlertDialog = false
                                        }) { Text("OK") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = {
                                            showRemoveStopAlertDialog = false
                                        }) { Text("Cancel") }
                                    },
                                    title = { Text(text = "Remove Stop?") },
                                    text = {
                                        Text(
                                            text = "$tempStopCode $tempStopName",
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