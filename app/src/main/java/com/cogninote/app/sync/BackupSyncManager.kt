package com.cogninote.app.sync

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.cogninote.app.data.repository.NoteRepository
import com.cogninote.app.data.entities.Note
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupSyncManager @Inject constructor(
    private val context: Context,
    private val noteRepository: NoteRepository
) {
    
    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    fun scheduleAutoBackup(intervalHours: Long = 24) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(intervalHours, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "auto_backup",
                ExistingPeriodicWorkPolicy.KEEP,
                backupRequest
            )
    }
    
    suspend fun createLocalBackup(): BackupResult {
        return try {
            _syncState.value = SyncState.BACKING_UP
            
            val notes = noteRepository.getAllNotes().first()
            val backup = NotesBackup(
                version = 1,
                timestamp = System.currentTimeMillis(),
                notes = notes.map { it.toBackupNote() }
            )
            
            val backupJson = json.encodeToString(backup)
            val backupFile = createBackupFile()
            backupFile.writeText(backupJson)
            
            _syncState.value = SyncState.IDLE
            BackupResult.Success(backupFile)
        } catch (e: Exception) {
            _syncState.value = SyncState.ERROR
            BackupResult.Error("Failed to create backup: ${e.message}")
        }
    }
    
    suspend fun restoreFromBackup(backupFile: File): BackupResult {
        return try {
            _syncState.value = SyncState.RESTORING
            
            val backupJson = backupFile.readText()
            val backup = json.decodeFromString<NotesBackup>(backupJson)
            
            // Clear existing notes (with user confirmation in UI)
            val existingNotes = noteRepository.getAllNotes().first()
            existingNotes.forEach { note ->
                noteRepository.deleteNote(note.id)
            }
            
            // Restore notes from backup
            backup.notes.forEach { backupNote ->
                val note = backupNote.toNote()
                noteRepository.insertNote(note)
            }
            
            _syncState.value = SyncState.IDLE
            BackupResult.Success(backupFile)
        } catch (e: Exception) {
            _syncState.value = SyncState.ERROR
            BackupResult.Error("Failed to restore backup: ${e.message}")
        }
    }
    
    suspend fun syncWithCloud(): SyncResult {
        return try {
            _syncState.value = SyncState.SYNCING
            
            // This would integrate with cloud storage providers
            // For now, we'll simulate the process
            
            val localNotes = noteRepository.getAllNotes().first()
            
            // In a real implementation, this would:
            // 1. Upload local changes to cloud
            // 2. Download remote changes
            // 3. Resolve conflicts
            // 4. Update local database
            
            _syncState.value = SyncState.IDLE
            SyncResult.Success(localNotes.size, 0, 0)
        } catch (e: Exception) {
            _syncState.value = SyncState.ERROR
            SyncResult.Error("Sync failed: ${e.message}")
        }
    }
    
    private fun createBackupFile(): File {
        val backupsDir = File(context.filesDir, "backups")
        if (!backupsDir.exists()) {
            backupsDir.mkdirs()
        }
        
        val timestamp = System.currentTimeMillis()
        return File(backupsDir, "cogninote_backup_$timestamp.json")
    }
    
    fun getLocalBackups(): List<File> {
        val backupsDir = File(context.filesDir, "backups")
        return if (backupsDir.exists()) {
            backupsDir.listFiles { _, name -> name.endsWith(".json") }?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }
}

@Serializable
data class NotesBackup(
    val version: Int,
    val timestamp: Long,
    val notes: List<BackupNote>
)

@Serializable
data class BackupNote(
    val id: String,
    val title: String,
    val content: String,
    val plainTextContent: String,
    val tags: List<String>,
    val folderId: String?,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val isDeleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val reminderAt: Long?,
    val attachments: List<String>
)

fun Note.toBackupNote(): BackupNote {
    return BackupNote(
        id = id,
        title = title,
        content = content,
        plainTextContent = plainTextContent,
        tags = tags,
        folderId = folderId,
        isPinned = isPinned,
        isArchived = isArchived,
        isDeleted = isDeleted,
        createdAt = createdAt.toEpochMilliseconds(),
        updatedAt = updatedAt.toEpochMilliseconds(),
        reminderAt = reminderAt?.toEpochMilliseconds(),
        attachments = attachments.map { it.toString() } // Convert to string for backup
    )
}

fun BackupNote.toNote(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        plainTextContent = plainTextContent,
        tags = tags,
        folderId = folderId,
        isPinned = isPinned,
        isArchived = isArchived,
        isDeleted = isDeleted,
        createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(createdAt),
        updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(updatedAt),
        reminderAt = reminderAt?.let { kotlinx.datetime.Instant.fromEpochMilliseconds(it) },
        attachments = emptyList() // Convert back from string if needed
    )
}

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // Auto backup logic would go here
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

enum class SyncState {
    IDLE,
    BACKING_UP,
    RESTORING,
    SYNCING,
    ERROR
}

sealed class BackupResult {
    data class Success(val file: File) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

sealed class SyncResult {
    data class Success(
        val totalNotes: Int,
        val uploaded: Int,
        val downloaded: Int
    ) : SyncResult()
    data class Error(val message: String) : SyncResult()
}
