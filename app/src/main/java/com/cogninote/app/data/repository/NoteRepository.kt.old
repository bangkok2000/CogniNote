package com.cogninote.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import com.cogninote.app.data.dao.NoteDao
import com.cogninote.app.data.dao.TagDao
import com.cogninote.app.data.dao.FolderDao
import com.cogninote.app.data.entities.Note
import com.cogninote.app.utils.TextUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val tagDao: TagDao,
    private val folderDao: FolderDao
) {

    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    fun getNoteById(id: String): Flow<Note?> = noteDao.getNoteByIdFlow(id)

    suspend fun getNoteByIdSuspend(id: String): Note? = noteDao.getNoteById(id)

    fun searchNotes(query: String): Flow<List<Note>> {
        return if (query.isBlank()) {
            getAllNotes()
        } else {
            noteDao.searchNotes(query.trim())
        }
    }

    fun getNotesByTags(tags: List<String>): Flow<List<Note>> {
        return if (tags.isEmpty()) {
            getAllNotes()
        } else {
            // Get all notes and filter by tags in the repository layer
            getAllNotes().map { allNotes ->
                allNotes.filter { note ->
                    // Check if the note contains any of the selected tags
                    tags.any { selectedTag ->
                        note.tags.contains(selectedTag)
                    }
                }
            }
        }
    }

    fun getNotesByFolder(folderId: String): Flow<List<Note>> = noteDao.getNotesByFolder(folderId)

    fun getPinnedNotes(): Flow<List<Note>> = noteDao.getPinnedNotes()

    fun getArchivedNotes(): Flow<List<Note>> = noteDao.getArchivedNotes()

    fun getDeletedNotes(): Flow<List<Note>> = noteDao.getDeletedNotes()

    fun getNotesWithReminders(): Flow<List<Note>> {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        return noteDao.getNotesWithReminders(currentTime)
    }

    fun getAllUsedTags(): Flow<List<String>> {
        return noteDao.getAllUsedTags().map { tagJsonList ->
            tagJsonList.flatMap { tagJson ->
                try {
                    // Parse the JSON string to get individual tags
                    val tags = Json.decodeFromString<List<String>>(tagJson)
                    tags.filter { it.isNotBlank() }
                } catch (e: Exception) {
                    // If parsing fails, return empty list
                    emptyList()
                }
            }.distinct().sorted()
        }
    }

    suspend fun createNote(
        title: String = "",
        content: String = "",
        tags: List<String> = emptyList(),
        folderId: String? = null,
        color: String? = null
    ): Note {
        val plainTextContent = TextUtils.extractPlainText(content)
        val wordCount = TextUtils.countWords(plainTextContent)
        val readingTime = TextUtils.calculateReadingTime(wordCount)

        val note = Note(
            title = title,
            content = content,
            plainTextContent = plainTextContent,
            tags = tags,
            folderId = folderId,
            color = color,
            wordCount = wordCount,
            readingTimeMinutes = readingTime
        )

        insertNote(note)
        updateTagUsages(emptyList(), tags)
        return note
    }

    suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }

    suspend fun updateNote(note: Note) {
        val existingNote = noteDao.getNoteById(note.id)
        val oldTags = existingNote?.tags ?: emptyList()
        
        val updatedNote = note.copy(
            updatedAt = Clock.System.now(),
            plainTextContent = TextUtils.extractPlainText(note.content),
            wordCount = TextUtils.countWords(TextUtils.extractPlainText(note.content)),
            readingTimeMinutes = TextUtils.calculateReadingTime(
                TextUtils.countWords(TextUtils.extractPlainText(note.content))
            )
        )
        
        noteDao.updateNote(updatedNote)
        updateTagUsages(oldTags, note.tags)
    }

    suspend fun deleteNote(noteId: String) {
        val note = noteDao.getNoteById(noteId)
        note?.let {
            noteDao.softDeleteNote(noteId)
            updateTagUsages(it.tags, emptyList())
        }
    }

    suspend fun hardDeleteNote(noteId: String) {
        val note = noteDao.getNoteById(noteId)
        note?.let {
            noteDao.hardDeleteNote(noteId)
            updateTagUsages(it.tags, emptyList())
        }
    }

    suspend fun restoreNote(noteId: String) {
        val note = noteDao.getNoteById(noteId)
        note?.let {
            noteDao.restoreNote(noteId)
            updateTagUsages(emptyList(), it.tags)
        }
    }

    suspend fun togglePin(noteId: String) {
        val note = noteDao.getNoteById(noteId)
        note?.let {
            noteDao.togglePin(noteId, !it.isPinned)
        }
    }

    suspend fun toggleArchive(noteId: String) {
        val note = noteDao.getNoteById(noteId)
        note?.let {
            noteDao.toggleArchive(noteId, !it.isArchived)
        }
    }

    suspend fun moveToFolder(noteId: String, folderId: String?) {
        noteDao.moveToFolder(noteId, folderId)
    }

    suspend fun emptyTrash() {
        // Get all deleted notes to update tag usage
        val deletedNotes = noteDao.getDeletedNotes()
        
        noteDao.emptyTrash()
        
        // Update tag usages for all deleted notes
        deletedNotes.collect { notes ->
            notes.forEach { note ->
                updateTagUsages(note.tags, emptyList())
            }
        }
    }

    suspend fun duplicateNote(noteId: String): Note? {
        val originalNote = noteDao.getNoteById(noteId) ?: return null
        
        val duplicatedNote = originalNote.copy(
            id = Note.generateNoteId(),
            title = "${originalNote.title} (Copy)",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            isPinned = false
        )
        
        insertNote(duplicatedNote)
        updateTagUsages(emptyList(), duplicatedNote.tags)
        return duplicatedNote
    }

    suspend fun updateBacklinks(noteId: String, newOutlinks: List<String>) {
        val note = noteDao.getNoteById(noteId) ?: return
        val oldOutlinks = note.outlinks

        // Update the note with new outlinks
        val updatedNote = note.copy(
            outlinks = newOutlinks,
            updatedAt = Clock.System.now()
        )
        noteDao.updateNote(updatedNote)

        // Update backlinks in referenced notes
        val removedLinks = oldOutlinks - newOutlinks.toSet()
        val addedLinks = newOutlinks - oldOutlinks.toSet()

        // Remove backlinks from notes no longer referenced
        removedLinks.forEach { targetNoteId ->
            val targetNote = noteDao.getNoteById(targetNoteId)
            targetNote?.let { target ->
                val updatedBacklinks = target.backlinks - noteId
                val updatedTarget = target.copy(
                    backlinks = updatedBacklinks,
                    updatedAt = Clock.System.now()
                )
                noteDao.updateNote(updatedTarget)
            }
        }

        // Add backlinks to newly referenced notes
        addedLinks.forEach { targetNoteId ->
            val targetNote = noteDao.getNoteById(targetNoteId)
            targetNote?.let { target ->
                val updatedBacklinks = (target.backlinks + noteId).distinct()
                val updatedTarget = target.copy(
                    backlinks = updatedBacklinks,
                    updatedAt = Clock.System.now()
                )
                noteDao.updateNote(updatedTarget)
            }
        }
    }

    fun getBacklinkedNotes(noteId: String): Flow<List<Note>> {
        return flow {
            val note = noteDao.getNoteById(noteId)
            if (note != null && note.backlinks.isNotEmpty()) {
                val paddedBacklinks = note.backlinks.take(5) + List(5 - note.backlinks.size.coerceAtMost(5)) { "" }
                noteDao.getBacklinkedNotes(
                    paddedBacklinks[0], paddedBacklinks[1], paddedBacklinks[2], paddedBacklinks[3], paddedBacklinks[4]
                ).collect { emit(it) }
            } else {
                emit(emptyList())
            }
        }
    }

    fun getOutlinkedNotes(noteId: String): Flow<List<Note>> {
        return flow {
            val note = noteDao.getNoteById(noteId)
            if (note != null && note.outlinks.isNotEmpty()) {
                val paddedOutlinks = note.outlinks.take(5) + List(5 - note.outlinks.size.coerceAtMost(5)) { "" }
                noteDao.getOutlinkedNotes(
                    paddedOutlinks[0], paddedOutlinks[1], paddedOutlinks[2], paddedOutlinks[3], paddedOutlinks[4]
                ).collect { emit(it) }
            } else {
                emit(emptyList())
            }
        }
    }

    // Statistics
    fun getTotalNotesCount(): Flow<Int> = noteDao.getTotalNotesCount()
    fun getTodayNotesCount(): Flow<Int> = noteDao.getTodayNotesCount()
    fun getTotalWordCount(): Flow<Int> = noteDao.getTotalWordCount()

    private suspend fun updateTagUsages(oldTags: List<String>, newTags: List<String>) {
        val removedTags = oldTags - newTags.toSet()
        val addedTags = newTags - oldTags.toSet()

        // Decrement usage for removed tags
        removedTags.forEach { tag ->
            tagDao.decrementUsage(tag)
        }

        // Increment usage for added tags  
        addedTags.forEach { tag ->
            val existingTag = tagDao.getTagByName(tag)
            if (existingTag == null) {
                tagDao.insertTag(com.cogninote.app.data.entities.Tag(name = tag, usageCount = 1))
            } else {
                tagDao.incrementUsage(tag)
            }
        }
    }
}
