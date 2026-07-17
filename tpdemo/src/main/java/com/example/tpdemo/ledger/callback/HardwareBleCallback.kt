package com.example.tpdemo.ledger.callback

/**
 * Author: clement
 * Create: 2026/7/17
 * Desc: 
 */
interface HardwareBleCallback {

    fun onSuccess()
    fun onError(error: String)

}
