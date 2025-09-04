package com.cogninote.app.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.cogninote.app.data.entities.Note

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id AND isDeleted = 0")
    suspend fun getNoteById(id: String): Note?

    @Query("SELECT * FROM notes WHERE id = :id AND isDeleted = 0")
    fun getNoteByIdFlow(id: String): Flow<Note?>

    @Query("""
        SELECT * FROM notes 
        WHERE isDeleted = 0 AND 
        (title LIKE '%' || :query || '%' OR 
         content LIKE '%' || :query || '%' OR 
         plainTextContent LIKE '%' || :query || '%')
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchNotes(query: String): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE isDeleted = 0 AND 
        (tags LIKE '%' || :tag1 || '%' OR tags LIKE '%' || :tag2 || '%' OR tags LIKE '%' || :tag3 || '%' OR tags LIKE '%' || :tag4 || '%' OR tags LIKE '%' || :tag5 || '%')
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getNotesByTags(tag1: String, tag2: String, tag3: String, tag4: String, tag5: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE folderId = :folderId AND isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByFolder(folderId: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isPinned = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getPinnedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isArchived = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getDeletedNotes(): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE reminderAt IS NOT NULL AND reminderAt > :currentTime AND isDeleted = 0
        ORDER BY reminderAt ASC
    """)
    fun getNotesWithReminders(currentTime: Long): Flow<List<Note>>

    @Query("""
        SELECT DISTINCT tags FROM notes 
        WHERE isDeleted = 0 AND tags != '' AND tags != '[]'
        ORDER BY tags
    """)
    fun getAllUsedTags(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<Note>)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("UPDATE notes SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteNote(id: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun hardDeleteNote(id: String)

    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :id")
    suspend fun togglePin(id: String, isPinned: Boolean)

    @Query("UPDATE notes SET isArchived = :isArchived WHERE id = :id")
    suspend fun toggleArchive(id: String, isArchived: Boolean)

    @Query("UPDATE notes SET folderId = :folderId WHERE id = :id")
    suspend fun moveToFolder(id: String, folderId: String?)

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun emptyTrash()

    @Query("UPDATE notes SET isDeleted = 0 WHERE id = :id")
    suspend fun restoreNote(id: String)

    // Backlink operations
    @Query("""
        SELECT * FROM notes 
        WHERE (id = :backlink1 OR id = :backlink2 OR id = :backlink3 OR id = :backlink4 OR id = :backlink5) AND isDeleted = 0
    """)
    fun getBacklinkedNotes(backlink1: String, backlink2: String, backlink3: String, backlink4: String, backlink5: String): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE (id = :outlink1 OR id = :outlink2 OR id = :outlink3 OR id = :outlink4 OR id = :outlink5) AND isDeleted = 0
    """)
    fun getOutlinkedNotes(outlink1: String, outlink2: String, outlink3: String, outlink4: String, outlink5: String): Flow<List<Note>>

    // Statistics
    @Query("SELECT COUNT(*) FROM notes WHERE isDeleted = 0")
    fun getTotalNotesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE isDeleted = 0 AND DATE(createdAt/1000, 'unixepoch') = DATE('now')")
    fun getTodayNotesCount(): Flow<Int>

    @Query("SELECT SUM(wordCount) FROM notes WHERE isDeleted = 0")
    fun getTotalWordCount(): Flow<Int>
}
