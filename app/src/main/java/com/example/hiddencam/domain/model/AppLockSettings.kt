package com.example.hiddencam.domain.model

/**
 * App lock settings for securing the application
 */
data class AppLockSettings(
    val isEnabled: Boolean = false,
    val pin: String = "", // 4-digit PIN, stored hashed
    val isBiometricEnabled: Boolean = false
)
