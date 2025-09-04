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
    val tags: List<String> = emptyList(),
    val folderId: String? = null,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val isEncrypted: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val attachments: List<Attachment> = emptyList(),
    val backlinks: List<String> = emptyList(), // Note IDs that link to this note
    val outlinks: List<String> = emptyList(), // Note IDs this note links to
    val location: Location? = null,
    val reminderAt: Instant? = null,
    val color: String? = null,
    val wordCount: Int = 0,
    val readingTimeMinutes: Int = 0
) {
    companion object {
        fun generateNoteId(): String {
            return "note_${Clock.System.now().toEpochMilliseconds()}_${(1000..9999).random()}"
        }
    }
}

@Serializable
data class Attachment(
    val id: String,
    val type: AttachmentType,
    val name: String,
    val filePath: String,
    val size: Long,
    val mimeType: String,
    val createdAt: Instant
)

@Serializable
enum class AttachmentType {
    IMAGE, AUDIO, DOCUMENT, DRAWING, VIDEO
}

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val name: String? = null
)

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
    fun fromAttachmentList(value: List<Attachment>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toAttachmentList(value: String): List<Attachment> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromLocation(value: Location?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toLocation(value: String?): Location? {
        return value?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromInstant(value: Instant): Long {
        return value.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(value: Long): Instant {
        return Instant.fromEpochMilliseconds(value)
    }

    @TypeConverter
    fun fromInstantNullable(value: Instant?): Long? {
        return value?.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstantNullable(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }
}
