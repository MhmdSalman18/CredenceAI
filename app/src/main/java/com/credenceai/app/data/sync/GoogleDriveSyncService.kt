package com.credenceai.app.data.sync

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveSyncService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DRIVE_SCOPE = "oauth2:https://www.googleapis.com/auth/drive.appdata"
    private val BACKUP_FILENAME = "credence_backup.bin"

    fun getSignInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun getSignInClient(): com.google.android.gms.auth.api.signin.GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/drive.appdata"))
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val account = getSignInAccount() ?: return@withContext null
        try {
            // GoogleAuthUtil returns the token. If token is expired, clearToken should be called.
            GoogleAuthUtil.getToken(context, account.account!!, DRIVE_SCOPE)
        } catch (e: Exception) {
            Timber.e(e, "Error obtaining Google Drive OAuth Token")
            null
        }
    }

    suspend fun clearAccessToken(token: String) = withContext(Dispatchers.IO) {
        try {
            GoogleAuthUtil.clearToken(context, token)
        } catch (e: Exception) {
            Timber.e(e, "Error clearing access token")
        }
    }

    /**
     * Search for the backup file in appDataFolder. Returns fileId if found, null otherwise.
     */
    suspend fun findBackupFile(token: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=name='$BACKUP_FILENAME'")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Accept", "application/json")

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val filesArray = json.getJSONArray("files")
                if (filesArray.length() > 0) {
                    val fileObj = filesArray.getJSONObject(0)
                    return@withContext fileObj.getString("id")
                }
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                Timber.w("Token unauthorized in search, clearing token")
                clearAccessToken(token)
            } else {
                Timber.e("Drive search failed: Response Code %d", responseCode)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching for backup file on Google Drive")
        }
        null
    }

    /**
     * Upload backup file to Google Drive appDataFolder.
     */
    suspend fun uploadBackup(token: String, backupFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val fileId = findBackupFile(token)
            val fileBytes = FileInputStream(backupFile).use { it.readBytes() }

            return@withContext if (fileId != null) {
                // File exists -> Update it via PATCH
                Timber.d("Backup file found (ID: %s), performing update", fileId)
                updateBackupFile(token, fileId, fileBytes)
            } else {
                // File doesn't exist -> Create new via multipart POST
                Timber.d("Backup file not found, performing initial upload")
                createBackupFile(token, fileBytes)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uploading backup to Google Drive")
            false
        }
    }

    private suspend fun createBackupFile(token: String, fileBytes: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val boundary = "_____CredenceBackupBoundary_____"
            val url = URL("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")

            val metadata = JSONObject().apply {
                put("name", BACKUP_FILENAME)
                put("parents", listOf("appDataFolder"))
            }.toString()

            conn.outputStream.use { os ->
                // Write metadata part
                os.write(("--$boundary\r\n").toByteArray())
                os.write("Content-Type: application/json; charset=UTF-8\r\n\r\n".toByteArray())
                os.write(metadata.toByteArray())
                os.write("\r\n".toByteArray())

                // Write file bytes part
                os.write(("--$boundary\r\n").toByteArray())
                os.write("Content-Type: application/octet-stream\r\n\r\n".toByteArray())
                os.write(fileBytes)
                os.write("\r\n".toByteArray())

                os.write(("--$boundary--\r\n").toByteArray())
                os.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                Timber.d("Backup file created successfully on Google Drive")
                true
            } else {
                val errorStreamText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Timber.e("Backup file creation failed: Status %d, Error: %s", responseCode, errorStreamText)
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating database backup file")
            false
        }
    }

    private suspend fun updateBackupFile(token: String, fileId: String, fileBytes: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH"
            conn.doOutput = true
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/octet-stream")
            conn.setRequestProperty("Content-Length", fileBytes.size.toString())

            conn.outputStream.use { os ->
                os.write(fileBytes)
                os.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Timber.d("Backup file updated successfully on Google Drive")
                true
            } else {
                val errorStreamText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Timber.e("Backup file update failed: Status %d, Error: %s", responseCode, errorStreamText)
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating database backup file")
            false
        }
    }

    /**
     * Download backup file from Google Drive to local target file.
     */
    suspend fun downloadBackup(token: String, targetFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val fileId = findBackupFile(token) ?: run {
                Timber.e("Download aborted: backup file does not exist on Google Drive")
                return@withContext false
            }

            val url = URL("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Timber.d("Backup file downloaded successfully from Google Drive")
                true
            } else {
                val errorStreamText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Timber.e("Download failed: Status %d, Error: %s", responseCode, errorStreamText)
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error downloading database backup file")
            false
        }
    }
}
