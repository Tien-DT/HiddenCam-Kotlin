package com.example.hiddencam.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Utility class for managing WiFi Hotspot (AP mode) functionality.
 * 
 * Note: Starting from Android 8.0 (API 26), programmatic control of WiFi hotspot
 * is highly restricted. This class provides guidance for manual setup and 
 * utilities for getting network information.
 */
object WifiHotspotManager {
    
    private const val TAG = "WifiHotspotManager"
    
    /**
     * Data class representing hotspot configuration
     */
    data class HotspotConfig(
        val ssid: String = "HiddenCam_Server",
        val password: String = "hiddencam123",
        val securityType: SecurityType = SecurityType.WPA2_PSK
    )
    
    enum class SecurityType {
        OPEN,
        WPA2_PSK,
        WPA3_SAE
    }
    
    /**
     * Get the local IP address of the device.
     * Works whether connected via WiFi or running hotspot.
     */
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                
                // Skip loopback and down interfaces
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP address", e)
        }
        return null
    }
    
    /**
     * Get the hotspot IP address (typically 192.168.43.1 when device is AP)
     */
    fun getHotspotIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val name = networkInterface.name
                
                // Check for AP interface names
                if (name.contains("ap") || name.contains("wlan") || 
                    name.contains("swlan") || name.startsWith("softap")) {
                    
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (!address.isLoopbackAddress && address is Inet4Address) {
                            return address.hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting hotspot IP", e)
        }
        return null
    }
    
    /**
     * Get the SSID of the currently connected WiFi network
     */
    fun getCurrentWifiSsid(context: Context): String? {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        @Suppress("DEPRECATION")
        val wifiInfo = wifiManager.connectionInfo
        
        return wifiInfo?.ssid?.removeSurrounding("\"")
    }
    
    /**
     * Check if WiFi is enabled
     */
    fun isWifiEnabled(context: Context): Boolean {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }
    
    /**
     * Check if device is connected to a WiFi network
     */
    fun isConnectedToWifi(context: Context): Boolean {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    /**
     * Get network information for display
     */
    fun getNetworkInfo(context: Context): NetworkInfo {
        val ipAddress = getLocalIpAddress()
        val hotspotIp = getHotspotIpAddress()
        val ssid = getCurrentWifiSsid(context)
        val isWifiConnected = isConnectedToWifi(context)
        
        return NetworkInfo(
            ipAddress = ipAddress ?: hotspotIp,
            ssid = ssid,
            isWifiConnected = isWifiConnected,
            isHotspotLikely = hotspotIp != null && !isWifiConnected
        )
    }
    
    data class NetworkInfo(
        val ipAddress: String?,
        val ssid: String?,
        val isWifiConnected: Boolean,
        val isHotspotLikely: Boolean
    )
    
    /**
     * Get instructions for setting up hotspot manually.
     * Since Android restricts programmatic hotspot control,
     * users need to enable it manually.
     */
    fun getHotspotSetupInstructions(): String {
        return """
            📱 Cài đặt WiFi Hotspot:
            
            1. Mở Settings (Cài đặt)
            2. Tìm "Hotspot & Tethering" hoặc "Mobile Hotspot"
            3. Bật "WiFi Hotspot" / "Mobile Hotspot"
            4. Đặt tên (SSID): HiddenCam_Server
            5. Đặt mật khẩu: hiddencam123
            6. Chọn bảo mật: WPA2-Personal
            7. Lưu và bật Hotspot
            
            💡 Tip: Sau khi bật hotspot, quay lại app để xem địa chỉ IP của web server.
            
            📋 Để kết nối:
            - Trên thiết bị khác, kết nối WiFi: HiddenCam_Server
            - Nhập mật khẩu: hiddencam123
            - Mở trình duyệt và truy cập địa chỉ web server
        """.trimIndent()
    }
    
    /**
     * Suggested QR code data for easy WiFi connection
     * Format: WIFI:S:<ssid>;T:<security>;P:<password>;;
     */
    fun generateWifiQrCodeData(config: HotspotConfig): String {
        val security = when (config.securityType) {
            SecurityType.OPEN -> "nopass"
            SecurityType.WPA2_PSK -> "WPA"
            SecurityType.WPA3_SAE -> "SAE"
        }
        
        return if (config.securityType == SecurityType.OPEN) {
            "WIFI:S:${config.ssid};T:nopass;;"
        } else {
            "WIFI:S:${config.ssid};T:$security;P:${config.password};;"
        }
    }
    
    /**
     * Open system WiFi settings
     */
    fun openWifiSettings(context: Context) {
        try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening WiFi settings", e)
        }
    }
    
    /**
     * Open system Tethering/Hotspot settings
     */
    fun openHotspotSettings(context: Context) {
        try {
            // Try to open tethering settings directly
            val intent = android.content.Intent().apply {
                action = android.provider.Settings.ACTION_WIRELESS_SETTINGS
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // Try more specific intents for different Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                intent.action = android.provider.Settings.Panel.ACTION_WIFI
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening hotspot settings", e)
            // Fallback to general wireless settings
            try {
                val fallbackIntent = android.content.Intent(
                    android.provider.Settings.ACTION_WIRELESS_SETTINGS
                ).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Error opening fallback settings", e2)
            }
        }
    }
    
    /**
     * Monitor network changes
     */
    class NetworkChangeCallback(
        private val onNetworkAvailable: (String?) -> Unit,
        private val onNetworkLost: () -> Unit
    ) : ConnectivityManager.NetworkCallback() {
        
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Handler(Looper.getMainLooper()).post {
                onNetworkAvailable(getLocalIpAddress())
            }
        }
        
        override fun onLost(network: Network) {
            super.onLost(network)
            Handler(Looper.getMainLooper()).post {
                onNetworkLost()
            }
        }
    }
    
    fun registerNetworkCallback(
        context: Context,
        callback: NetworkChangeCallback
    ) {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        
        connectivityManager.registerNetworkCallback(request, callback)
    }
    
    fun unregisterNetworkCallback(
        context: Context,
        callback: NetworkChangeCallback
    ) {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering network callback", e)
        }
    }
}
