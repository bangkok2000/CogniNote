package com.cogninote.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class Note(
    @PrimaryKey
    val id: String = generateNoteId(),
    val title: String = "",
    val content: String = "",
    val plainTextContent: String = "", // For search optimization
    val tags: List<String> = emptyList(), // Auto-extracted from #hashtags
    val folder: String? = null, // Simple folder name instead of ID
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val isPinned: Boolean = false
    
    // REMOVED for simplicity:
    // - isEncrypted, isArchived, isDeleted (soft delete complexity)
    // - attachments, backlinks, outlinks (advanced features)  
    // - location, reminderAt, color (feature bloat)
    // - wordCount, readingTimeMinutes (analytics bloat)
) {
    companion object {
        fun generateNoteId(): String {
            return "note_${Clock.System.now().toEpochMilliseconds()}_${(1000..9999).random()}"
        }
    }
}

// Simplified converters - only what we need for basic note-taking
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromInstant(value: Instant): Long {
        return value.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(value: Long): Instant {
        return Instant.fromEpochMilliseconds(value)
    }
}
