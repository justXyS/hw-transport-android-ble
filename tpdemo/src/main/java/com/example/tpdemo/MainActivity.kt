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
import androidx.lifecycle.lifecycleScope
import com.example.tpdemo.ledger.LedgerBleManager
import com.example.tpdemo.ledger.callback.HardwareBleCallback
import com.example.tpdemo.ui.theme.LiveTransportBleTheme
import com.example.tpdemo.utils.ActivityManager
import com.example.tpdemo.utils.PermissionUtil
import com.ledger.live.ble.model.BleDeviceModel
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private val ledgerBleManager by lazy { LedgerBleManager() }
    private val ledgerDevice by lazy {
        BleDeviceModel(
            "DE:F1:7D:4B:09:44",
            "Nano X 95D4",
            rssi = 0,
            device = null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        ActivityManager.addActivity(this)
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
                            //连接设备
                            ledgerBleManager.connect(ledgerDevice, object : HardwareBleCallback {
                                override fun onSuccess() {
                                    Timber.e("蓝牙连接成功")
                                }

                                override fun onError(error: String) {
                                    Timber.e("蓝牙连接失败:%s", error)
                                }
                            })
                        }) {
                            Text("连接设备")
                        }

                        Button(onClick = {
                            //open app
                            openApp("Ethereum")
                        }) {
                            Text("open Ethereum")
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

    fun openApp(appName: String) {
        lifecycleScope.launch {
            ledgerBleManager.openApp(appName)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        PermissionUtil.getInstance().onRequestPermissionsResult(
            this@MainActivity, requestCode,
            permissions,
            grantResults
        )
    }

}