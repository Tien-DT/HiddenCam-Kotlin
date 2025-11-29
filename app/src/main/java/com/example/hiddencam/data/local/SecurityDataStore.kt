package com.example.hiddencam.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hiddencam.domain.model.AppLockSettings
import com.example.hiddencam.domain.model.BluetoothRemoteSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "security_settings")

@Singleton
class SecurityDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        // App Lock
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_PIN_HASH = stringPreferencesKey("app_lock_pin_hash")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        
        // Bluetooth Remote
        val BLUETOOTH_REMOTE_ENABLED = booleanPreferencesKey("bluetooth_remote_enabled")
        val PAIRED_DEVICE_NAME = stringPreferencesKey("paired_device_name")
        val PAIRED_DEVICE_ADDRESS = stringPreferencesKey("paired_device_address")
    }
    
    // ==================== App Lock ====================
    
    val appLockSettingsFlow: Flow<AppLockSettings> = context.securityDataStore.data.map { preferences ->
        AppLockSettings(
            isEnabled = preferences[PreferencesKeys.APP_LOCK_ENABLED] ?: false,
            pin = preferences[PreferencesKeys.APP_LOCK_PIN_HASH] ?: "",
            isBiometricEnabled = preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
        )
    }
    
    suspend fun getAppLockSettings(): AppLockSettings {
        return appLockSettingsFlow.first()
    }
    
    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.securityDataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LOCK_ENABLED] = enabled
        }
    }
    
    suspend fun setPin(pin: String) {
        context.securityDataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LOCK_PIN_HASH] = hashPin(pin)
        }
    }
    
    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.securityDataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
    }
    
    fun verifyPin(inputPin: String, storedHash: String): Boolean {
        return hashPin(inputPin) == storedHash
    }
    
    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    // ==================== Bluetooth Remote ====================
    
    val bluetoothRemoteSettingsFlow: Flow<BluetoothRemoteSettings> = context.securityDataStore.data.map { preferences ->
        BluetoothRemoteSettings(
            isEnabled = preferences[PreferencesKeys.BLUETOOTH_REMOTE_ENABLED] ?: false,
            pairedDeviceName = preferences[PreferencesKeys.PAIRED_DEVICE_NAME] ?: "",
            pairedDeviceAddress = preferences[PreferencesKeys.PAIRED_DEVICE_ADDRESS] ?: ""
        )
    }
    
    suspend fun getBluetoothRemoteSettings(): BluetoothRemoteSettings {
        return bluetoothRemoteSettingsFlow.first()
    }
    
    suspend fun setBluetoothRemoteEnabled(enabled: Boolean) {
        context.securityDataStore.edit { preferences ->
            preferences[PreferencesKeys.BLUETOOTH_REMOTE_ENABLED] = enabled
        }
    }
    
    suspend fun setPairedDevice(name: String, address: String) {
        context.securityDataStore.edit { preferences ->
            preferences[PreferencesKeys.PAIRED_DEVICE_NAME] = name
            preferences[PreferencesKeys.PAIRED_DEVICE_ADDRESS] = address
        }
    }
    
    suspend fun clearPairedDevice() {
        context.securityDataStore.edit { preferences ->
            preferences[PreferencesKeys.PAIRED_DEVICE_NAME] = ""
            preferences[PreferencesKeys.PAIRED_DEVICE_ADDRESS] = ""
        }
    }
}
