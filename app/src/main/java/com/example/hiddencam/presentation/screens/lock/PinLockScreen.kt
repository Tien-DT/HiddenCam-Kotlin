package com.example.hiddencam.presentation.screens.lock

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.hiddencam.data.datastore.SecurityDataStore
import kotlinx.coroutines.launch

// Helper function to find FragmentActivity from Context
private fun Context.findActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun PinLockScreen(
    securityDataStore: SecurityDataStore,
    isSetupMode: Boolean = false,
    onSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val storedPinHash by securityDataStore.appLockPinHash.collectAsState(initial = "")
    val isBiometricEnabled by securityDataStore.biometricEnabled.collectAsState(initial = false)
    
    var enteredPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirmMode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var biometricTrigger by remember { mutableStateOf(0) } // Trigger for biometric prompt
    
    val title = when {
        isSetupMode && !isConfirmMode -> "Set PIN"
        isSetupMode && isConfirmMode -> "Confirm PIN"
        else -> "Enter PIN"
    }
    
    val subtitle = when {
        isSetupMode && !isConfirmMode -> "Create a 4-digit PIN"
        isSetupMode && isConfirmMode -> "Re-enter your PIN"
        else -> "Enter your 4-digit PIN to unlock"
    }
    
    // Show biometric on launch if enabled and not in setup mode
    LaunchedEffect(isBiometricEnabled, isSetupMode, storedPinHash) {
        if (isBiometricEnabled && !isSetupMode && storedPinHash.isNotEmpty()) {
            biometricTrigger++
        }
    }
    
    // Handle biometric authentication
    LaunchedEffect(biometricTrigger) {
        if (biometricTrigger > 0 && !isSetupMode) {
            val activity = context.findActivity()
            if (activity == null) {
                Toast.makeText(context, "Cannot find activity", Toast.LENGTH_SHORT).show()
                return@LaunchedEffect
            }
            
            val biometricManager = BiometricManager.from(context)
            val canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            
            when (canAuthenticate) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    val executor = ContextCompat.getMainExecutor(context)
                    val biometricPrompt = BiometricPrompt(
                        activity,
                        executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                onSuccess()
                            }
                            
                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                // User cancelled or error
                            }
                            
                            override fun onAuthenticationFailed() {
                                // User can retry
                            }
                        }
                    )
                    
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Unlock HiddenCam")
                        .setSubtitle("Use fingerprint to unlock")
                        .setNegativeButtonText("Use PIN")
                        .setAllowedAuthenticators(
                            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.BIOMETRIC_WEAK
                        )
                        .build()
                    
                    biometricPrompt.authenticate(promptInfo)
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    Toast.makeText(context, "No biometric hardware", Toast.LENGTH_SHORT).show()
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    Toast.makeText(context, "Biometric hardware unavailable", Toast.LENGTH_SHORT).show()
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    Toast.makeText(context, "No biometric enrolled", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(context, "Biometric not available: $canAuthenticate", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Lock icon
        Text(
            text = "🔒",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // PIN dots
        PinDotsRow(
            enteredLength = if (isConfirmMode) confirmPin.length else enteredPin.length
        )
        
        // Error message
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Number pad
        NumberPad(
            onNumberClick = { number ->
                errorMessage = ""
                if (isSetupMode) {
                    if (!isConfirmMode) {
                        if (enteredPin.length < 4) {
                            enteredPin += number
                            if (enteredPin.length == 4) {
                                isConfirmMode = true
                            }
                        }
                    } else {
                        if (confirmPin.length < 4) {
                            confirmPin += number
                            if (confirmPin.length == 4) {
                                if (confirmPin == enteredPin) {
                                    // Save PIN using SecurityDataStore
                                    coroutineScope.launch {
                                        securityDataStore.setPin(enteredPin)
                                        securityDataStore.setAppLockEnabled(true)
                                        onSuccess()
                                    }
                                } else {
                                    errorMessage = "PINs don't match. Try again."
                                    enteredPin = ""
                                    confirmPin = ""
                                    isConfirmMode = false
                                }
                            }
                        }
                    }
                } else {
                    if (enteredPin.length < 4) {
                        enteredPin += number
                        if (enteredPin.length == 4) {
                            // Verify PIN using SecurityDataStore
                            coroutineScope.launch {
                                if (securityDataStore.verifyPin(enteredPin)) {
                                    onSuccess()
                                } else {
                                    errorMessage = "Incorrect PIN"
                                    enteredPin = ""
                                }
                            }
                        }
                    }
                }
            },
            onBackspaceClick = {
                if (isConfirmMode && confirmPin.isNotEmpty()) {
                    confirmPin = confirmPin.dropLast(1)
                } else if (!isConfirmMode && enteredPin.isNotEmpty()) {
                    enteredPin = enteredPin.dropLast(1)
                }
                errorMessage = ""
            },
            onBiometricClick = if (isBiometricEnabled && !isSetupMode) {
                { biometricTrigger++ }
            } else null
        )
    }
}

@Composable
private fun PinDotsRow(enteredLength: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < enteredLength) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

@Composable
private fun NumberPad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onBiometricClick: (() -> Unit)?
) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("biometric", "0", "backspace")
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        numbers.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                row.forEach { item ->
                    when (item) {
                        "biometric" -> {
                            if (onBiometricClick != null) {
                                NumberPadButton(
                                    content = {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Biometric",
                                            modifier = Modifier.size(28.dp)
                                        )
                                    },
                                    onClick = onBiometricClick
                                )
                            } else {
                                Spacer(modifier = Modifier.size(72.dp))
                            }
                        }
                        "backspace" -> {
                            NumberPadButton(
                                content = {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Backspace",
                                        modifier = Modifier.size(28.dp)
                                    )
                                },
                                onClick = onBackspaceClick
                            )
                        }
                        else -> {
                            NumberPadButton(
                                content = {
                                    Text(
                                        text = item,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = { onNumberClick(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberPadButton(
    content: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(72.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
