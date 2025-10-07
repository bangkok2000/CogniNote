package com.cogninote.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cogninote.app.data.entities.Note
import com.cogninote.app.data.entities.Folder
import com.cogninote.app.data.entities.Tag
import com.cogninote.app.data.entities.NoteTemplate
import com.cogninote.app.data.entities.Task
import com.cogninote.app.data.entities.Converters
import com.cogninote.app.data.dao.NoteDao
import com.cogninote.app.data.dao.FolderDao
import com.cogninote.app.data.dao.TagDao
import com.cogninote.app.data.dao.TemplateDao
import com.cogninote.app.data.dao.TaskDao

@Database(
    entities = [Note::class], // Simplified - only notes
    version = 4, // Increment version for simplified schema
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CogniNoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    
    // REMOVED for simplicity:
    // - folderDao (using simple string folder names)
    // - tagDao (tags extracted from content automatically)  
    // - templateDao (using enum-based simple templates)
    // - taskDao (removing task management complexity)

    companion object {
        @Volatile
        private var INSTANCE: CogniNoteDatabase? = null
        private const val DATABASE_NAME = "cogninote_database"

        fun getDatabase(context: Context): CogniNoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CogniNoteDatabase::class.java,
                    DATABASE_NAME
                )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration() // For development only
                .build()
                
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                
                // Simplified indexes - only what we need for basic search and sorting
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_updated_at ON notes(updatedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_pinned ON notes(isPinned)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_folder ON notes(folder)")
                
                // Full-text search index on content and title
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_search ON notes(title, content, plainTextContent)")
            }
        }

        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}