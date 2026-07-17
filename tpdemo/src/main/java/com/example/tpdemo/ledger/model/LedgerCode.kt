package com.example.tpdemo.ledger.model

/**
 * Author: clement
 * Create: 2026/7/17
 * Desc: 
 */
object LedgerCode {

    const val SUCCESS = 0x9000

    //拒绝打开已安装的app
    const val REJECT_OPEN_INSTALLED_APP = 0x5501

    //打开未安装的app
    const val OPEN_NOT_INSTALLED_APP = 0x6807

    //当前 App 无法处理请求
    const val APP_NOT_AVAILABLE = 0x6984

    //设备未解锁
    const val NOT_UNLOCKED = 0x5515

    //拒绝交易
    const val REJECT_TX = 0x6985

}