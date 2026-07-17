package com.example.tpdemo.ledger.model

import java.io.Serializable

/**
 * Author: clement
 * Create: 2026/7/17
 * Desc: 
 */
data class LedgerAppInfo(
    val appName: String,
    val version: String,
    val flags: Int,
    val status: Int
) {

    val isSuccess: Boolean
        get() = status == LedgerCode.SUCCESS

    val isBolos: Boolean
        get() = appName.equals("BOLOS", ignoreCase = true)

    val isOfficialApp: Boolean
        get() = flags and 0x02 != 0

    fun isAppOpened(chain: String): Boolean {
        return appName.equals(chain, true)
    }

    companion object {

        /**
         * 解析 Ledger getAppInfo 返回值
         *
         * 0105424f4c4f5305312e362e319000 -> BOLOS / 1.6.1
         * 0107426974636f696e05322e342e3601029000 -> Bitcoin / 2.4.6 / flags=0x02
         */
        fun parse(responseHex: String): LedgerAppInfo {
            val response = responseHex.trim()
                .removePrefix("0x")
                .removePrefix("0X")
                .lowercase()

            //取字符串最后4个字符
            val statusHex = response.takeLast(4)
            val status = statusHex.toInt(16)
            val data = response.dropLast(4)

            var offset = 0
            fun readByte(): Int {
                val value = data.substring(offset, offset + 2).toInt(16)
                offset += 2
                return value
            }

            fun readAscii(length: Int): String {
                val end = offset + length * 2
                val bytes = ByteArray(length) { index ->
                    data.substring(offset + index * 2, offset + index * 2 + 2)
                        .toInt(16)
                        .toByte()
                }
                offset = end
                return bytes.toString(Charsets.US_ASCII)
            }

            val format = readByte()
            if (format != 0x01) {
                throw Exception("Unsupported Ledger app info format: $format")
            }

            val appNameLength = readByte()
            val appName = readAscii(appNameLength)

            val versionLength = readByte()
            val version = readAscii(versionLength)

            var flags = 0
            if (offset < data.length) {
                val flagsLength = readByte()
                repeat(flagsLength) {
                    flags = (flags shl 8) or readByte()
                }
            }
            return LedgerAppInfo(appName, version, flags, status)
        }
    }

}