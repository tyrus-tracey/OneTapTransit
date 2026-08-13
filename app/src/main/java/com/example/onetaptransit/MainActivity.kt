package com.example.onetaptransit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.onetaptransit.ui.theme.OneTapTransitTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OneTapTransitTheme {
                val transitViewModel: TransitViewModel by viewModels()

                Box(modifier = Modifier
                    .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 0.dp, vertical = 128.dp)
                    ) {
                        var staticDataUpdated by rememberSaveable { mutableStateOf(false) }
                        var realtimeUpdated by rememberSaveable { mutableStateOf(false) }

                        TestButton(
                            "Static Data",
                            "Static Data Downloaded",
                            {
                                transitViewModel.updateStaticData(this@MainActivity) {
                                    staticDataUpdated = true
                                }
                            },
                            staticDataUpdated
                        )

                        TestButton(
                            "Realtime",
                            "Realtime Downloaded",
                            {
                                transitViewModel.updateRealtimeFeed() {
                                    realtimeUpdated = true
                                }
                            },
                            realtimeUpdated
                        )

                        TestButton(
                            "query stops",
                            "done!",
                            onButtonClick = {
                                transitViewModel.queryTest() { }
                            },
                            false
                        )

                        TestButton(
                            "truncate stops",
                            "done!",
                            onButtonClick = {
                                transitViewModel.truncateTest() { }
                            },
                            false
                        )
                    }
                }
            }
        }
    }
}