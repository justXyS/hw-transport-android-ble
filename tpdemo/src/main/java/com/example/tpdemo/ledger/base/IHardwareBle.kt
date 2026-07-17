package com.example.tpdemo.ledger.base

import com.example.tpdemo.ledger.callback.HardwareBleCallback
import com.example.tpdemo.ledger.callback.HardwareScanCallback
import com.ledger.live.ble.model.BleDeviceModel

/**
 * Author: clement
 * Create: 2026/7/17
 * Desc:
 */
interface IHardwareBle {

    /**
     * 开始搜索
     */
    fun startScan(callback: HardwareScanCallback)

    /**
     * 停止搜索
     */
    fun stopScan()

    /**
     * 连接设备
     */
    fun connect(device: BleDeviceModel, callback: HardwareBleCallback)

    /**
     * 断开连接
     */
    fun disconnect()

    /**
     * 是否处于连接状态
     */
    fun isConnected(): Boolean
}