package com.example.hiddencam.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "security_settings")

@Singleton
class SecurityDataStore @Inject constructor(
    private val context: Context
) {
    private object PreferencesKeys {
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_PIN_HASH = stringPreferencesKey("app_lock_pin_hash")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val LOCK_TIMEOUT = intPreferencesKey("lock_timeout") // 0 = immediate, minutes otherwise
        val LAST_BACKGROUND_TIME = longPreferencesKey("last_background_time")
        val BLUETOOTH_REMOTE_ENABLED = booleanPreferencesKey("bluetooth_remote_enabled")
        val PAIRED_DEVICE_NAME = stringPreferencesKey("paired_device_name")
        val PAIRED_DEVICE_ADDRESS = stringPreferencesKey("paired_device_address")
    }

    // App Lock Enabled
    val appLockEnabled: Flow<Boolean> = context.securityDataStore.data.map { preferences ->
        preferences[PreferencesKeys.APP_LOCK_ENABLED] ?: false
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.securityDataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LOCK_ENABLED] = enabled
        }
    }

    // PIN Hash
    val appLockPinHash: Flow<String> = context.securityDataStore.data.map { preferences ->
        preferences[PreferencesKeys.APP_LOCK_PIN_HASH] ?: ""
    }

    suspend fun setPin(pin: String) {
        val hash = hashPin(pin)
        context.securityDataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LOCK_PIN_HASH] = hash
            preferences[PreferencesKeys.APP_LOCK_ENABLED] = true
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val hash = hashPin(pin)
        val storedHash = context.securityDataStore.data.map { preferences ->
            preferences[PreferencesKeys.APP_LOCK_PIN_HASH]
        }.first()
        return hash == storedHash
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Biometric Enabled
    val biometricEnabled: Flow<Boolean> = context.securityDataStore.data.map { preferences ->
        preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.securityDataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
    }

    // Lock Timeout (0 = immediate, 1, 2, 5, 10 minutes)
    val lockTimeout: Flow<Int> = context.securityDataStore.data.map { preferences ->
        preferences[PreferencesKeys.LOCK_TIMEOUT] ?: 0
    }

    suspend fun setLockTimeout(minutes: Int) {
        context.securityDataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCK_TIMEOUT] = minutes
        }
    }

    // Last Background Time
    val lastBackgroundTime: Flow<Long> = context.securityDataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_BACKGROUND_TIME] ?: 0L
    }

    suspend fun setLastBackgroundTime(time: Long) {
        context.securityDataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_BACKGROUND_TIME] = time
        }
    }

    suspend fun shouldLockApp(): Boolean {
        val timeout = context.securityDataStore.data.map { it[PreferencesKeys.LOCK_TIMEOUT] ?: 0 }.first()
        val lastTime = context.securityDataStore.data.map { it[PreferencesKeys.LAST_BACKGROUND_TIME] ?: 0L }.first()
        val lockEnabled = context.securityDataStore.data.map { it[PreferencesKeys.APP_LOCK_ENABLED] ?: false }.first()
        
        if (!lockEnabled) return false
        if (timeout == 0) return true // Immediate lock
        
        val elapsedMinutes = (System.currentTimeMillis() - lastTime) / 60000
        return elapsedMinutes >= timeout
    }

    // Bluetooth Remote Enabled
    val bluetoothRemoteEnabled: Flow<Boolean> = context.securityDataStore.data.map { preferences ->
        preferences[PreferencesKeys.BLUETOOTH_REMOTE_ENABLED] ?: false
    }

    suspend fun setBluetoothRemoteEnabled(enabled: Boolean) {
        context.securityDataStore.edit { preferences ->
            preferences[PreferencesKeys.BLUETOOTH_REMOTE_ENABLED] = enabled
        }
    }

    // Paired Device
    val pairedDeviceName: Flow<String?> = context.securityDataStore.data.map { preferences ->
        preferences[PreferencesKeys.PAIRED_DEVICE_NAME]
    }

    val pairedDeviceAddress: Flow<String?> = context.securityDataStore.data.map { preferences ->
        preferences[PreferencesKeys.PAIRED_DEVICE_ADDRESS]
    }

    suspend fun setPairedDevice(name: String, address: String) {
        context.securityDataStore.edit { preferences ->
            preferences[PreferencesKeys.PAIRED_DEVICE_NAME] = name
            preferences[PreferencesKeys.PAIRED_DEVICE_ADDRESS] = address
        }
    }

    suspend fun clearPairedDevice() {
        context.securityDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.PAIRED_DEVICE_NAME)
            preferences.remove(PreferencesKeys.PAIRED_DEVICE_ADDRESS)
        }
    }
}
