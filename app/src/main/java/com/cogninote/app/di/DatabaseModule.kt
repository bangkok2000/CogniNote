package com.cogninote.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.cogninote.app.data.database.CogniNoteDatabase
import com.cogninote.app.data.dao.NoteDao
import com.cogninote.app.data.dao.FolderDao
import com.cogninote.app.data.dao.TagDao
import com.cogninote.app.data.dao.TemplateDao
import com.cogninote.app.data.dao.TaskDao
import com.cogninote.app.data.security.BiometricManager
import com.cogninote.app.services.ExportImportService
import com.cogninote.app.sharing.ShareManager
import javax.inject.Singleton
import java.security.SecureRandom

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabasePassphrase(@ApplicationContext context: Context): ByteArray {
        // For now, using a fixed development passphrase
        // TODO: Integrate with biometric authentication in Phase 1
        // This should be replaced with proper key derivation from user authentication
        return "CogniNoteDevPassphrase2024!".toByteArray()
    }

    @Provides
    @Singleton
    fun provideCogniNoteDatabase(
        @ApplicationContext context: Context,
        passphrase: ByteArray
    ): CogniNoteDatabase {
        return CogniNoteDatabase.getDatabase(context)
    }

    @Provides
    fun provideNoteDao(database: CogniNoteDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    fun provideFolderDao(database: CogniNoteDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    fun provideTagDao(database: CogniNoteDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    fun provideTemplateDao(database: CogniNoteDatabase): TemplateDao {
        return database.templateDao()
    }

    @Provides
    fun provideTaskDao(database: CogniNoteDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideBiometricManager(@ApplicationContext context: Context): BiometricManager {
        return BiometricManager(context)
    }

    @Provides
    @Singleton
    fun provideExportImportService(@ApplicationContext context: Context): ExportImportService {
        return ExportImportService(context)
    }

    @Provides
    @Singleton
    fun provideShareManager(@ApplicationContext context: Context): ShareManager {
        return ShareManager(context)
    }
}
