package com.cogninote.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
// import net.zetetic.database.sqlcipher.SQLiteDatabase

@HiltAndroidApp
class CogniNoteApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize SQLCipher - temporarily disabled
        // SQLiteDatabase.loadLibs(this)
        
        // Initialize AI models (lazy loading)
        initializeAIModels()
    }
    
    private fun initializeAIModels() {
        // TODO: Initialize TensorFlow Lite models in background
        // This will be implemented in Phase 2
    }
}
