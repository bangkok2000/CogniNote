package com.cogninote.app.data.repository

import kotlinx.coroutines.flow.Flow
import com.cogninote.app.data.entities.Note
import com.cogninote.app.data.dao.NoteDao
import com.cogninote.app.utils.SimplifiedTextUtils
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimplifiedNoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    
    // Basic note operations
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    
    fun getNoteById(id: String): Flow<Note?> = noteDao.getNoteByIdFlow(id)
    
    suspend fun getNoteByIdSync(id: String): Note? = noteDao.getNoteById(id)
    
    // Smart search - handles both regular text and hashtag searches  
    fun searchNotes(query: String): Flow<List<Note>> {
        val cleanQuery = query.trim()
        return if (cleanQuery.isEmpty()) {
            getAllNotes()
        } else {
            noteDao.searchNotes(cleanQuery)
        }
    }
    
    // Get notes by folder (simple folder name, not complex ID system)
    fun getNotesByFolder(folderName: String): Flow<List<Note>> = 
        noteDao.getNotesByFolder(folderName)
    
    fun getPinnedNotes(): Flow<List<Note>> = noteDao.getPinnedNotes()
    
    // Get all unique tags for search suggestions (extracted from content)
    fun getAllUsedTags(): Flow<List<String>> = noteDao.getAllUsedTags()
    
    // Create note with automatic processing
    suspend fun createNote(
        title: String = "",
        content: String = "",
        folder: String? = null
    ): Note {
        val note = Note(
            title = title.ifEmpty { 
                // Auto-generate title from content if empty
                content.lines().firstOrNull()?.take(50)?.trim() ?: "Untitled"
            },
            content = content,
            plainTextContent = SimplifiedTextUtils.extractPlainText(content),
            tags = extractHashtagsFromContent(content), // Auto-extract tags
            folder = folder,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        
        noteDao.insertNote(note)
        return note
    }
    
    // Update note with automatic processing
    suspend fun updateNote(note: Note) {
        val updatedNote = note.copy(
            plainTextContent = SimplifiedTextUtils.extractPlainText(note.content),
            tags = extractHashtagsFromContent(note.content), // Auto-update tags
            updatedAt = Clock.System.now()
        )
        noteDao.updateNote(updatedNote)
    }
    
    // Simple delete (no soft delete complexity)
    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }
    
    suspend fun deleteNoteById(id: String) {
        noteDao.getNoteById(id)?.let { note ->
            deleteNote(note)
        }
    }
    
    // Toggle pin status
    suspend fun togglePin(noteId: String) {
        val note = noteDao.getNoteById(noteId)
        note?.let {
            noteDao.togglePin(noteId, !it.isPinned)
        }
    }
    
    // Move to folder
    suspend fun moveToFolder(noteId: String, folderName: String?) {
        noteDao.moveToFolder(noteId, folderName)
    }
    
    // Basic statistics
    fun getTotalNotesCount(): Flow<Int> = noteDao.getTotalNotesCount()
    
    // Auto-extract hashtags from content (#hashtag becomes tag)
    private fun extractHashtagsFromContent(content: String): List<String> {
        val hashtagPattern = Regex("#(\\w+)")
        return hashtagPattern.findAll(content)
            .map { it.groupValues[1].lowercase() }
            .distinct()
            .toList()
    }
}

// Simple data class for basic stats (no complex analytics)
data class NoteStatistics(
    val totalNotes: Int,
    val todayNotes: Int = 0, // Removed complex today counting
    val totalWords: Int = 0  // Removed complex word counting
)