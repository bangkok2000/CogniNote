package com.cogninote.app.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.cogninote.app.data.entities.Note

@Dao
interface NoteDao {

    // Basic CRUD operations - simplified for daily use
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): Note?

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteByIdFlow(id: String): Flow<Note?>

    // Simple search across title and content
    @Query("""
        SELECT * FROM notes 
        WHERE (title LIKE '%' || :query || '%' OR 
               content LIKE '%' || :query || '%' OR 
               plainTextContent LIKE '%' || :query || '%' OR
               tags LIKE '%' || :query || '%')
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchNotes(query: String): Flow<List<Note>>

    // Simple folder filtering (using folder name, not ID)
    @Query("SELECT * FROM notes WHERE folder = :folderName ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByFolder(folderName: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinnedNotes(): Flow<List<Note>>

    // REMOVED for simplicity:
    // - isDeleted soft delete (just hard delete notes)
    // - isArchived (archive is feature bloat)
    // - reminderAt queries (removing reminder complexity)
    // - complex tag queries (simplified to basic text search)

    // Extract unique tags from all notes (for search suggestions)
    @Query("SELECT DISTINCT tags FROM notes WHERE tags != '' AND tags != '[]'")
    fun getAllUsedTags(): Flow<List<String>>

    // Basic operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :id")
    suspend fun togglePin(id: String, isPinned: Boolean)

    @Query("UPDATE notes SET folder = :folderName WHERE id = :id")
    suspend fun moveToFolder(id: String, folderName: String?)

    // Simple statistics for basic info
    @Query("SELECT COUNT(*) FROM notes")
    fun getTotalNotesCount(): Flow<Int>

    // REMOVED for simplicity:
    // - Soft delete system (isDeleted complexity)
    // - Archive system (isArchived feature bloat)
    // - Reminder system (reminderAt complexity)
    // - Backlink/outlink system (advanced linking complexity)
    // - Word count statistics (analytics bloat)
    // - Complex batch operations
}
