package com.example.hiddencam.util

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utility class for encrypting and decrypting video files using AES-GCM encryption.
 * Uses the app lock PIN as the encryption key base.
 */
object VideoEncryptionUtil {
    
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val ENCRYPTED_EXTENSION = ".enc"
    private const val DECRYPTED_EXTENSION = ".mp4"
    
    /**
     * Derives a 256-bit AES key from the PIN using SHA-256
     */
    private fun deriveKeyFromPin(pin: String): SecretKey {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }
    
    /**
     * Generates a random IV for GCM mode
     */
    private fun generateIV(): ByteArray {
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        return iv
    }
    
    /**
     * Encrypts a video file using the provided PIN
     * @param inputFile The original video file to encrypt
     * @param outputFile The destination for the encrypted file
     * @param pin The PIN to use for encryption
     * @return True if encryption was successful
     */
    fun encryptFile(inputFile: File, outputFile: File, pin: String): Boolean {
        return try {
            val key = deriveKeyFromPin(pin)
            val iv = generateIV()
            
            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
            
            FileInputStream(inputFile).use { fis ->
                FileOutputStream(outputFile).use { fos ->
                    // Write IV at the beginning of the file
                    fos.write(iv)
                    
                    CipherOutputStream(fos, cipher).use { cos ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (fis.read(buffer).also { bytesRead = it } != -1) {
                            cos.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Encrypts a video file from a Uri (for MediaStore files)
     * @param contentResolver The content resolver to access the file
     * @param sourceUri The Uri of the video to encrypt
     * @param outputFile The destination for the encrypted file
     * @param pin The PIN to use for encryption
     * @return True if encryption was successful
     */
    fun encryptFromUri(contentResolver: ContentResolver, sourceUri: Uri, outputFile: File, pin: String): Boolean {
        return try {
            val key = deriveKeyFromPin(pin)
            val iv = generateIV()
            
            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
            
            contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(outputFile).use { fos ->
                    // Write IV at the beginning of the file
                    fos.write(iv)
                    
                    CipherOutputStream(fos, cipher).use { cos ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            cos.write(buffer, 0, bytesRead)
                        }
                    }
                }
            } ?: return false
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Decrypts a video file using the provided PIN
     * @param encryptedFile The encrypted video file
     * @param outputFile The destination for the decrypted file
     * @param pin The PIN to use for decryption
     * @return True if decryption was successful
     */
    fun decryptFile(encryptedFile: File, outputFile: File, pin: String): Boolean {
        return try {
            val key = deriveKeyFromPin(pin)
            
            FileInputStream(encryptedFile).use { fis ->
                // Read IV from the beginning of the file
                val iv = ByteArray(GCM_IV_LENGTH)
                fis.read(iv)
                
                val cipher = Cipher.getInstance(ALGORITHM)
                val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
                
                FileOutputStream(outputFile).use { fos ->
                    CipherInputStream(fis, cipher).use { cis ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (cis.read(buffer).also { bytesRead = it } != -1) {
                            fos.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Decrypts a video file to a temporary file for playback
     * @param encryptedFile The encrypted video file
     * @param pin The PIN to use for decryption
     * @param cacheDir The cache directory for temporary files
     * @return The decrypted temporary file, or null if decryption failed
     */
    fun decryptToTemp(encryptedFile: File, pin: String, cacheDir: File): File? {
        val tempFile = File(cacheDir, "temp_decrypted_${System.currentTimeMillis()}$DECRYPTED_EXTENSION")
        return if (decryptFile(encryptedFile, tempFile, pin)) {
            tempFile
        } else {
            tempFile.delete()
            null
        }
    }
    
    /**
     * Encrypts a video and writes it to a new MediaStore entry
     * @param contentResolver The content resolver
     * @param sourceFile The original video file
     * @param pin The PIN for encryption
     * @param displayName The display name for the encrypted file
     * @return The Uri of the encrypted file, or null if failed
     */
    fun encryptToMediaStore(
        contentResolver: ContentResolver,
        sourceFile: File,
        pin: String,
        displayName: String
    ): Uri? {
        return try {
            val key = deriveKeyFromPin(pin)
            val iv = generateIV()
            
            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
            
            val encryptedFileName = "${displayName.removeSuffix(".mp4")}$ENCRYPTED_EXTENSION"
            
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, encryptedFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/HiddenCam/Encrypted")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Files.getContentUri("external")
            }
            
            val uri = contentResolver.insert(collection, contentValues) ?: return null
            
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                FileInputStream(sourceFile).use { fis ->
                    // Write IV at the beginning
                    outputStream.write(iv)
                    
                    CipherOutputStream(outputStream, cipher).use { cos ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (fis.read(buffer).also { bytesRead = it } != -1) {
                            cos.write(buffer, 0, bytesRead)
                        }
                    }
                }
            } ?: run {
                contentResolver.delete(uri, null, null)
                return null
            }
            
            // Mark as complete
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                contentResolver.update(uri, contentValues, null, null)
            }
            
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Checks if a file is encrypted (has .enc extension)
     */
    fun isEncrypted(file: File): Boolean {
        return file.name.endsWith(ENCRYPTED_EXTENSION)
    }
    
    /**
     * Checks if a file is encrypted (by filename)
     */
    fun isEncrypted(fileName: String): Boolean {
        return fileName.endsWith(ENCRYPTED_EXTENSION)
    }
    
    /**
     * Gets the encrypted file extension
     */
    fun getEncryptedExtension(): String = ENCRYPTED_EXTENSION
    
    /**
     * Gets the decrypted file extension
     */
    fun getDecryptedExtension(): String = DECRYPTED_EXTENSION
    
    /**
     * Verifies if the provided PIN can decrypt the file
     * by attempting to decrypt a small portion
     */
    fun verifyPin(encryptedFile: File, pin: String): Boolean {
        return try {
            val key = deriveKeyFromPin(pin)
            
            FileInputStream(encryptedFile).use { fis ->
                val iv = ByteArray(GCM_IV_LENGTH)
                fis.read(iv)
                
                val cipher = Cipher.getInstance(ALGORITHM)
                val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
                
                CipherInputStream(fis, cipher).use { cis ->
                    // Try to read a small amount to verify
                    val testBuffer = ByteArray(1024)
                    cis.read(testBuffer)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
