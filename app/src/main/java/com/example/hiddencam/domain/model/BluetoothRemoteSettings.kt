package com.example.hiddencam.domain.model

/**
 * Bluetooth remote settings for remote recording control
 */
data class BluetoothRemoteSettings(
    val isEnabled: Boolean = false,
    val pairedDeviceName: String = "",
    val pairedDeviceAddress: String = ""
)
