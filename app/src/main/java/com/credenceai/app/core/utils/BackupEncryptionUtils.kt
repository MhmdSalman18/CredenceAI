package com.credenceai.app.core.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupEncryptionUtils {

    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val AES_GCM_ALGORITHM = "AES/GCM/NoPadding"
    private const val ITERATION_COUNT = 2000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
    private const val IV_LENGTH_BYTES = 12
    private const val TAG_LENGTH_BITS = 128

    fun encryptFile(inputFile: File, outputFile: File, passphrase: CharArray): Boolean {
        return try {
            // 1. Generate secure random salt and iv
            val random = SecureRandom()
            val salt = ByteArray(SALT_LENGTH_BYTES)
            random.nextBytes(salt)
            val iv = ByteArray(IV_LENGTH_BYTES)
            random.nextBytes(iv)

            // 2. Derive AES-256 key from passphrase
            val keySpec = PBEKeySpec(passphrase, salt, ITERATION_COUNT, KEY_LENGTH_BITS)
            val keyFactory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val secretKey = keyFactory.generateSecret(keySpec)
            val aesKey = SecretKeySpec(secretKey.encoded, "AES")

            // 3. Initialize Cipher in ENCRYPT Mode
            val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
            val parameterSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, parameterSpec)

            // 4. Read DB bytes and encrypt
            val dbBytes = FileInputStream(inputFile).use { it.readBytes() }
            val cipherText = cipher.doFinal(dbBytes)

            // 5. Write [salt] [iv] [ciphertext] to output file
            FileOutputStream(outputFile).use { fos ->
                fos.write(salt)
                fos.write(iv)
                fos.write(cipherText)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun decryptFile(inputFile: File, outputFile: File, passphrase: CharArray): Boolean {
        return try {
            // 1. Read input file
            val fileBytes = FileInputStream(inputFile).use { it.readBytes() }
            if (fileBytes.size < SALT_LENGTH_BYTES + IV_LENGTH_BYTES) {
                return false
            }

            // 2. Extract salt, iv, and ciphertext
            val salt = fileBytes.copyOfRange(0, SALT_LENGTH_BYTES)
            val iv = fileBytes.copyOfRange(SALT_LENGTH_BYTES, SALT_LENGTH_BYTES + IV_LENGTH_BYTES)
            val cipherText = fileBytes.copyOfRange(SALT_LENGTH_BYTES + IV_LENGTH_BYTES, fileBytes.size)

            // 3. Derive key from passphrase and extracted salt
            val keySpec = PBEKeySpec(passphrase, salt, ITERATION_COUNT, KEY_LENGTH_BITS)
            val keyFactory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val secretKey = keyFactory.generateSecret(keySpec)
            val aesKey = SecretKeySpec(secretKey.encoded, "AES")

            // 4. Initialize Cipher in DECRYPT Mode
            val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
            val parameterSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, aesKey, parameterSpec)

            // 5. Decrypt and write back to database path
            val plainText = cipher.doFinal(cipherText)
            FileOutputStream(outputFile).use { fos ->
                fos.write(plainText)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
