package com.cogninote.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.cogninote.app.data.database.CogniNoteDatabase
import com.cogninote.app.data.dao.NoteDao

import javax.inject.Singleton
import java.security.SecureRandom

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCogniNoteDatabase(
        @ApplicationContext context: Context
    ): CogniNoteDatabase {
        return CogniNoteDatabase.getDatabase(context)
    }

    @Provides
    fun provideNoteDao(database: CogniNoteDatabase): NoteDao {
        return database.noteDao()
    }

    // REMOVED for simplicity:
    // - FolderDao (using simple string folders)
    // - TagDao (auto-extracting tags from content)
    // - TemplateDao (using enum-based simple templates)
    // - TaskDao (removed task management)
    // - BiometricManager (removed biometric security)
    // - ExportImportService (simplified to basic text sharing)



    @Provides
    @Singleton
    fun provideSimplifiedNoteRepository(noteDao: NoteDao): com.cogninote.app.data.repository.SimplifiedNoteRepository {
        return com.cogninote.app.data.repository.SimplifiedNoteRepository(noteDao)
    }
}
