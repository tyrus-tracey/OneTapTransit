package com.example.onetaptransit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.onetaptransit.composables.GTFSStaticDataImportDisplay
import com.example.onetaptransit.composables.NextArrivalsForStopDisplay
import com.example.onetaptransit.composables.TestButton
import com.example.onetaptransit.composables.TextInputWithTestButton
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

                if (gtfsStaticDataImportState.isLoading) {
                    GTFSStaticDataImportDisplay(
                        gtfsStaticDataImportState,
                        { transitViewModel.cancelStaticDataImport(applicationContext) }
                    )
                }

                Box(modifier = Modifier
                    .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 0.dp, vertical = 128.dp)
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


                        val nextArrivalsState = transitViewModel.nextArrivalsState.collectAsStateWithLifecycle()
                        val savedTransitStopsState = transitViewModel.savedTransitStopsState.collectAsStateWithLifecycle()

                        TextInputWithTestButton(
                            transitViewModel.transitState.collectAsStateWithLifecycle(), //TODO: save transitState as -> val state by ...
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

                        NextArrivalsForStopDisplay(
                            savedTransitStopsState.value.transitStops,
                            nextArrivalsState,
                            onStopBannerClick = { stopCode ->
                                transitViewModel.queryNextArrival(
                                    stopCode,
                                    onQueryResponse = {}
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}