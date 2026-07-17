package com.example.tpdemo.ledger

import android.content.Context
import com.example.tpdemo.ledger.base.HardwareBleManager
import com.example.tpdemo.ledger.callback.HardwareBleCallback
import com.example.tpdemo.ledger.callback.HardwareScanCallback
import com.example.tpdemo.ledger.model.LedgerAppInfo
import com.example.tpdemo.ledger.model.LedgerCode
import com.example.tpdemo.utils.ActivityManager
import com.ledger.live.ble.BleManager
import com.ledger.live.ble.BleManagerFactory
import com.ledger.live.ble.model.BleDeviceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resumeWithException

/**
 * Author: clement
 * Create: 2026/7/17
 * Desc: 
 */
class LedgerBleManager : HardwareBleManager() {

    //Ledger SDK封装的BleManager
    private val bleManager: BleManager = BleManagerFactory.newInstance(getContext())

    /**
     * 执行
     */
    override fun scan(callback: HardwareScanCallback) {
        //开始扫描
        try {
            bleManager.startScanning { scannedDevices ->
                Timber.tag("scanResult").e(scannedDevices.toString())
                //搜索到设备后
                callback.onDevicesFound(scannedDevices as MutableList<BleDeviceModel>)
            }
        } catch (e: Throwable) {
            //停止搜索
            stopScan()
            callback.onError("BLE_SCAN_FAILED")
        }
    }

    override fun stopScan() {
        super.stopScan()
        bleManager.stopScanning()
    }

    override fun connect(
        device: BleDeviceModel,
        callback: HardwareBleCallback
    ) {
        managerScope.launch(Dispatchers.IO) {
            try {
                //开始连接
                connectInternal(device)
                onConnected(device, callback)
            } catch (e: Exception) {
                managerScope.launch(Dispatchers.Main) {
                    currentDevice = null
                    callback.onError(e.message ?: "")
                }
            }
        }

    }

    /**
     * 连接蓝牙
     */
    private suspend fun connectInternal(device: BleDeviceModel) =
        suspendCancellableCoroutine { continuation ->
            try {
                bleManager.connect(
                    address = device.id,
                    onConnectSuccess = {
                        if (continuation.isActive) {
                            currentDevice = device
                            continuation.resumeWith(Result.success(Unit))
                        }
                    }, onConnectError = { error ->
                        if (continuation.isActive) {
                            continuation.resumeWithException(Exception(error.message))
                        }
                    })
            } catch (e: Throwable) {
                if (continuation.isActive) {
                    continuation.resumeWithException(Exception(e.message))
                }
            }
        }

    /**
     * 蓝牙连接成功后
     */
    private fun onConnected(
        device: BleDeviceModel,
        callback: HardwareBleCallback
    ) {
        managerScope.launch(Dispatchers.IO) {
            managerScope.launch(Dispatchers.Main) {
                //流程完成，连接成功
                callback.onSuccess()
            }
        }
    }

    override fun disconnect() {
        try {
            bleManager.disconnect(
                onDisconnectSuccess = {
                    currentDevice = null
                }
            )
        } catch (e: Throwable) {
            currentDevice = null
        }
    }

    override fun isConnected(): Boolean {
        return bleManager.isConnected
    }

    /**
     * 发送指令
     */
    suspend fun sendApdu(apduHex: String): String = suspendCancellableCoroutine { continuation ->
        try {
            bleManager.send(
                apduHex = apduHex,
                onSuccess = { answer ->
                    if (continuation.isActive) {
                        continuation.resumeWith(Result.success(answer))
                    }
                },
                onError = { errorMessage ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            IllegalStateException(errorMessage)
                        )
                    }
                }
            )
        } catch (e: Throwable) {
            if (continuation.isActive) {
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * 获取Ledger App信息
     */
    suspend fun getAppInfo(): LedgerAppInfo {
        val responseHex = sendApdu("b0010000")
        if (!isSuccessResponse(responseHex)) {
            val responseCode = responseHex.toInt(16)
            throw Exception("getAppInfo 失败")
        }
        return LedgerAppInfo.parse(responseHex)
    }

    /**
     * 打开Ledger App
     */
    suspend fun openApp(appName: String): Boolean {
        val nameBytes = appName.toByteArray(Charsets.US_ASCII)
        val apdu = buildString {
            append("e0d80000")
            append(nameBytes.size.toString(16).padStart(2, '0'))
            nameBytes.forEach { byte ->
                append(byte.toInt().and(0xff).toString(16).padStart(2, '0'))
            }
        }
        val responseHex = sendApdu(apdu)
        Timber.tag("openApp").e(responseHex)
        return when (val responseCode = responseHex.toInt(16)) {
            LedgerCode.SUCCESS -> true
            LedgerCode.REJECT_OPEN_INSTALLED_APP -> false
            LedgerCode.OPEN_NOT_INSTALLED_APP -> throw Exception("ledger_not_installed_app_$appName")

            else -> throw Exception("打开 openApp 失败")
        }
    }

    /**
     * 回到Dashboard，即退出Ledger App
     */
    suspend fun quitApp(): Boolean {
        val responseHex = sendApdu("b0a7000000")
        Timber.tag("quitApp").e(responseHex)
        return isSuccessResponse(responseHex)
    }

    /**
     * 判断是否成功的响应
     */
    private fun isSuccessResponse(responseHex: String): Boolean {
        return responseHex.endsWith("9000", ignoreCase = true)
    }

    /**
     * 确保蓝牙已连接。检查蓝牙是否已经连接，如果还没连接，则先连接蓝牙
     */
    suspend fun ensureBleConnected(device: BleDeviceModel) {
        suspendCancellableCoroutine { continuation ->
            //蓝牙已经连接
            if (isConnected()) {
                continuation.resume(Unit) { _, _, _ -> }
                return@suspendCancellableCoroutine
            }
            //连接蓝牙
            connect(device, object : HardwareBleCallback {
                override fun onSuccess() {
                    if (continuation.isActive) {
                        continuation.resume(Unit) { _, _, _ -> }
                    }
                }

                override fun onError(error: String) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            RuntimeException(error)
                        )
                    }
                }
            })
        }
    }

    /**
     * 检查Ledger是否打开了某个app，如果没有，则打开该app
     */
    suspend fun openAppIfNeed(appName: String) {
        val appInfo = getAppInfo()
        if (appInfo.isAppOpened(appName)) {
            return
        }
        //当前打开的是其它App,先退出
        if (!appInfo.isBolos) quitApp()
        //通知Ledger打开App
        val opened = openApp(appName)
        //用户选择打开
        if (opened) {
            return
        }
        //再次通知Ledger打开App
        val secondOpened = openApp(appName)
        if (!secondOpened) {
            throw Exception("ledger_error_reject_open_app")
        }
    }

    private fun getContext(): Context {
        return ActivityManager.curActivity!!.applicationContext
    }

}