package com.example.tpdemo.ledger.callback

import com.ledger.live.ble.model.BleDeviceModel

/**
 * Author: clement
 * Create: 2026/7/17
 * Desc: 
 */
interface HardwareScanCallback {

    fun onDevicesFound(foundDevices: MutableList<BleDeviceModel>)
    fun onTimeout()
    fun onError(error: String)

}