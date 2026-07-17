package com.example.tpdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tpdemo.ui.theme.LiveTransportBleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiveTransportBleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.spacedBy(
                            16.dp
                        )
                    ) {
                        Button(onClick = {
                            // TODO Button 1
                        }) {
                            Text("Button 1")
                        }

                        Button(onClick = {
                            // TODO Button 2
                        }) {
                            Text("Button 2")
                        }

                        Button(onClick = {
                            // TODO Button 3
                        }) {
                            Text("Button 3")
                        }
                    }
                }
            }
        }
    }
}