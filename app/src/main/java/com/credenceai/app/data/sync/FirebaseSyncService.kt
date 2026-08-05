package com.credenceai.app.data.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import android.net.Uri
import timber.log.Timber

@Singleton
class FirebaseSyncService @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {
    private val BACKUP_FILENAME = "credence_backup.bin"

    fun getCurrentUser() = auth.currentUser

    suspend fun uploadBackup(backupFile: File): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            val storageRef = storage.reference.child("backups/${user.uid}/$BACKUP_FILENAME")
            val fileUri = Uri.fromFile(backupFile)
            storageRef.putFile(fileUri).await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error uploading backup to Firebase Storage")
            false
        }
    }

    suspend fun downloadBackup(targetFile: File): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            val storageRef = storage.reference.child("backups/${user.uid}/$BACKUP_FILENAME")
            storageRef.getFile(targetFile).await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error downloading backup from Firebase Storage")
            false
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getBackupMetadata(): com.google.firebase.storage.StorageMetadata? {
        val user = auth.currentUser ?: return null
        return try {
            val storageRef = storage.reference.child("backups/${user.uid}/$BACKUP_FILENAME")
            storageRef.metadata.await()
        } catch (e: Exception) {
            null
        }
    }
}
