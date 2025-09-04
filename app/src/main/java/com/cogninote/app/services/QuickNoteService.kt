package com.cogninote.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import com.cogninote.app.data.repository.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QuickNoteService : Service() {

    @Inject
    lateinit var noteRepository: NoteRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_QUICK_NOTE -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                
                if (!sharedText.isNullOrBlank()) {
                    createQuickNote(sharedSubject ?: "Quick Note", sharedText)
                }
            }
        }
        
        return START_NOT_STICKY
    }

    private fun createQuickNote(title: String, content: String) {
        serviceScope.launch {
            try {
                noteRepository.createNote(
                    title = title,
                    content = content,
                    tags = listOf("quick-capture")
                )
            } catch (e: Exception) {
                // Log error or show notification
            } finally {
                stopSelf()
            }
        }
    }

    companion object {
        const val ACTION_QUICK_NOTE = "com.cogninote.app.ACTION_QUICK_NOTE"
    }
}
