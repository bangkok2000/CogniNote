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
    entities = [Note::class, Folder::class, Tag::class, NoteTemplate::class, Task::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CogniNoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao
    abstract fun templateDao(): TemplateDao
    abstract fun taskDao(): TaskDao

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
                
                // Create indexes for better query performance
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_folder ON notes(folderId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_created_at ON notes(createdAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_updated_at ON notes(updatedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_pinned ON notes(isPinned)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_archived ON notes(isArchived)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_deleted ON notes(isDeleted)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_reminder ON notes(reminderAt)")
                
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_folders_parent ON folders(parentFolderId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_folders_sort ON folders(sortOrder)")
                
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_tasks_note ON tasks(noteId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_tasks_completed ON tasks(isCompleted)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks(dueDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority)")
                
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_tags_usage ON tags(usageCount)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_tags_name ON tags(name)")
            }
        }

        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}