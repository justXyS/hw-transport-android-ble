package com.example.tpdemo.ledger.base

import android.os.Build
import com.example.tpdemo.ledger.callback.HardwareBleCallback
import com.example.tpdemo.ledger.callback.HardwareScanCallback
import com.example.tpdemo.utils.ActivityManager
import com.example.tpdemo.utils.PermissionUtil
import com.hjq.permissions.permission.PermissionNames
import com.ledger.live.ble.model.BleDeviceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Author: clement
 * Create: 2026/7/17
 * Desc: 
 */
abstract class HardwareBleManager : IHardwareBle {

    companion object {

        //扫描的超时时间
        const val SCAN_TIMEOUT_MILLIS = 20_000L

        //连接的超时时间
        const val CONNECT_TIMEOUT_MILLIS = 20_000L
    }

    protected var scanTimeoutJob: Job? = null
    protected val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    var currentDevice: BleDeviceModel? = null
        protected set
    var isScanning = false
        protected set

    /**
     * 扫描蓝牙设备
     */
    override fun startScan(callback: HardwareScanCallback) {
        managerScope.launch {
            //请求蓝牙权限
            val granted = requestBlePermission()
            if (!granted) {
                callback.onError("BLE_PERMISSION_DENIED")
                return@launch
            }
            if (isScanning) return@launch
            isScanning = true
            //计时，超时后回调
            startScanTimeout(callback)
            //执行扫描
            scan(callback)
        }
    }

    /**
     * 真正执行扫描
     */
    protected abstract fun scan(callback: HardwareScanCallback)

    override fun stopScan() {
        //取消计时
        cancelScanTimeout()
        isScanning = false
    }

    /**
     * 请求蓝牙权限
     */
    protected suspend fun requestBlePermission(): Boolean =
        suspendCancellableCoroutine { cont ->
            PermissionUtil.getInstance().attachPermissionCheckCallback(object :
                PermissionUtil.CheckCallback2 {
                override fun onPermissionGranted() {
                    if (cont.isActive) {
                        cont.resume(true)
                    }
                }

                override fun onPermissionDenied(var1: Array<String?>?) {
                    if (cont.isActive) {
                        cont.resume(false)
                    }
                }
            })
            val context = ActivityManager.curActivity
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PermissionUtil.getInstance().requestPermissions(
                    context,
                    PermissionNames.BLUETOOTH_SCAN,
                    PermissionNames.BLUETOOTH_ADVERTISE,
                    PermissionNames.BLUETOOTH_CONNECT,
                    PermissionNames.ACCESS_COARSE_LOCATION,
                    PermissionNames.ACCESS_FINE_LOCATION
                )
            } else {
                PermissionUtil.getInstance().requestPermissions(
                    context,
                    PermissionNames.ACCESS_COARSE_LOCATION,
                    PermissionNames.ACCESS_FINE_LOCATION
                )
            }
        }

    /**
     * 开始计时
     */
    protected fun startScanTimeout(
        callback: HardwareScanCallback,
        timeoutMillis: Long = SCAN_TIMEOUT_MILLIS
    ) {
        scanTimeoutJob?.cancel()
        scanTimeoutJob = managerScope.launch {
            delay(timeoutMillis)
            if (isScanning) {
                stopScan()
                callback.onTimeout()
            }
        }
    }

    /**
     * 取消计时
     */
    protected fun cancelScanTimeout() {
        scanTimeoutJob?.cancel()
        scanTimeoutJob = null
    }

    /**
     * 释放资源
     */
    protected fun release() {
        disconnect()
        cancelScanTimeout()
        currentDevice = null
        isScanning = false
        managerScope.cancel()
    }

    /**
     * 扫描+连接
     */
    fun scan2Connect(device: BleDeviceModel, callback: HardwareBleCallback) {
        //1.扫描蓝牙设备
        startScan(object : HardwareScanCallback {
            override fun onDevicesFound(foundDevices: MutableList<BleDeviceModel>) {
                val targetDevice = foundDevices.firstOrNull {
                    it.id == device.id
                }
                //找到匹配的device后
                if (targetDevice != null) {
                    //停止搜索
                    stopScan()
                    //断开连接
                    if (isConnected()) {
                        disconnect()
                    }
                    //开始连接
                    connect(device, callback)
                }
            }

            override fun onTimeout() {
                //扫描超时
                callback.onError("BLE_SCAN_TIMEOUT")
            }

            override fun onError(error: String) {
                callback.onError(error)
            }
        })
    }

}