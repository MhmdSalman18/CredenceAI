package com.credenceai.app.presentation.ui.screens.backup

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceai.app.core.preferences.PreferencesManager
import com.credenceai.app.core.utils.BackupEncryptionUtils
import com.credenceai.app.data.local.db.CredenceDatabase
import com.credenceai.app.data.sync.FirebaseSyncService
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.credenceai.app.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class BackupUiState(
    val firebaseUser: FirebaseUser? = null,
    val lastBackupTime: String = "Never",
    val isLoading: Boolean = false,
    val backupSuccess: Boolean = false,
    val restoreSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncService: FirebaseSyncService,
    private val preferencesManager: PreferencesManager,
    private val database: CredenceDatabase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            refreshBackupStatus()
        }
    }

    private suspend fun refreshBackupStatus() {
        val currentUser = syncService.getCurrentUser()
        val localLastBackup = preferencesManager.lastBackupTime.first()

        _uiState.value = _uiState.value.copy(
            firebaseUser = currentUser,
            lastBackupTime = localLastBackup
        )

        if (currentUser != null) {
            try {
                syncService.getBackupMetadata()?.let { metadata ->
                    val cloudTime = metadata.updatedTimeMillis
                    if (cloudTime > 0) {
                        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        val cloudTimeString = sdf.format(Date(cloudTime))

                        // Sync cloud time to local if local is missing
                        if (localLastBackup == "Never") {
                            preferencesManager.setLastBackupTime(cloudTimeString)
                            _uiState.value = _uiState.value.copy(lastBackupTime = cloudTimeString)
                        } else {
                            // Optionally: Update local state if cloud is more recent
                            _uiState.value = _uiState.value.copy(lastBackupTime = cloudTimeString)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching backup metadata")
            }
        }
    }

    fun handleGoogleSignInResult(account: GoogleSignInAccount) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                firebaseAuth.signInWithCredential(credential).await()
                refreshBackupStatus()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                Timber.e(e, "Firebase Authentication failed")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Authentication failed: ${e.localizedMessage}"
                )
            }
        }
    }

    fun signOut() {
        syncService.signOut()
        getGoogleSignInClient().signOut()
        _uiState.value = _uiState.value.copy(firebaseUser = null)
    }

    fun setSignInError(error: Throwable) {
        val message = if (error is ApiException) {
            when (error.statusCode) {
                10 -> "Google Sign-In failed: DEVELOPER_ERROR (code 10). Ensure your app's package name and SHA-1 fingerprint are registered in Google Cloud Console."
                7 -> "Google Sign-In failed: Network Error. Please check your internet connection."
                12500 -> "Google Sign-In failed: General error (code 12500). Usually caused by incorrect SHA-1 registration or missing Google Play Services."
                12501 -> "Google Sign-In cancelled by user."
                else -> "Google Sign-In failed: ${error.message} (code ${error.statusCode})"
            }
        } else {
            "Google Sign-In failed: ${error.localizedMessage ?: error.message}"
        }
        if (error is ApiException && error.statusCode == 12501) {
            Timber.i("Google Sign-In cancelled by user")
        } else {
            Timber.e(error, "Google Sign-In Exception")
            _uiState.value = _uiState.value.copy(errorMessage = message)
        }
    }

    fun onBackupClick(passphrase: String) {
        val user = _uiState.value.firebaseUser
        if (user == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please sign in with Google first")
            return
        }
        if (passphrase.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Passphrase must be at least 6 characters")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, backupSuccess = false)

        viewModelScope.launch {
            try {
                checkpointDatabase()

                val dbFile = context.getDatabasePath("credence_db")
                if (!dbFile.exists()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Database file not found locally"
                    )
                    return@launch
                }

                val encryptedFile = File(context.cacheDir, "credence_backup_temp.bin")
                val encryptionSuccess = BackupEncryptionUtils.encryptFile(
                    inputFile = dbFile,
                    outputFile = encryptedFile,
                    passphrase = passphrase.toCharArray()
                )

                if (!encryptionSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to encrypt database backup"
                    )
                    return@launch
                }

                val uploadSuccess = syncService.uploadBackup(encryptedFile)
                encryptedFile.delete()

                if (uploadSuccess) {
                    val timeString = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                    preferencesManager.setLastBackupTime(timeString)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        backupSuccess = true,
                        lastBackupTime = timeString
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to upload backup to Firebase"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error executing backup workflow")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }

    fun onRestoreClick(passphrase: String) {
        val user = _uiState.value.firebaseUser
        if (user == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please sign in with Google first")
            return
        }
        if (passphrase.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your decryption passphrase")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, restoreSuccess = false)

        viewModelScope.launch {
            try {
                val tempEncryptedFile = File(context.cacheDir, "downloaded_backup.bin")
                val downloadSuccess = syncService.downloadBackup(tempEncryptedFile)

                if (!downloadSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to download backup or no backup found"
                    )
                    return@launch
                }

                // 3. Decrypt file
                val tempDecryptedFile = File(context.cacheDir, "decrypted_temp.db")
                val decryptSuccess = BackupEncryptionUtils.decryptFile(
                    inputFile = tempEncryptedFile,
                    outputFile = tempDecryptedFile,
                    passphrase = passphrase.toCharArray()
                )
                tempEncryptedFile.delete()

                if (!decryptSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Decryption failed. Please verify your password is correct."
                    )
                    return@launch
                }

                // 4. Close database connections and overwrite local SQLite files
                closeDatabaseConnections()

                val dbFile = context.getDatabasePath("credence_db")
                val walFile = context.getDatabasePath("credence_db-wal")
                val shmFile = context.getDatabasePath("credence_db-shm")

                // Delete SQLite helper cache logs to force loading from the main file
                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()
                if (dbFile.exists()) dbFile.delete()

                val overwriteSuccess = tempDecryptedFile.renameTo(dbFile)
                if (overwriteSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        restoreSuccess = true
                    )
                    // Trigger a program restart to safely boot Room with new data schema
                    restartApplication()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to overwrite local database file"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error executing restore workflow")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Restore failed: ${e.message}"
                )
            }
        }
    }

    private fun checkpointDatabase() {
        try {
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
            Timber.d("Database WAL Checkpoint completed successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error checkpointing database WAL")
        }
    }

    private fun closeDatabaseConnections() {
        try {
            database.close()
            Timber.d("Database closed successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error closing database")
        }
    }

    private fun restartApplication() {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
        Runtime.getRuntime().exit(0)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun dismissSuccess() {
        _uiState.value = _uiState.value.copy(backupSuccess = false, restoreSuccess = false)
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(
                context.getString(R.string.default_web_client_id)
            )            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }
}
