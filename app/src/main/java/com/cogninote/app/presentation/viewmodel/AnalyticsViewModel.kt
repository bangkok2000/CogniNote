package com.cogninote.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cogninote.app.data.repository.NoteRepository
import com.cogninote.app.data.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {
    
    private val _analytics = MutableStateFlow(AnalyticsData())
    val analytics = _analytics.asStateFlow()
    
    init {
        loadAnalytics()
    }
    
    private fun loadAnalytics() {
        viewModelScope.launch {
            combine(
                noteRepository.getAllNotes(),
                folderRepository.getAllFolders()
            ) { notes, folders ->
                AnalyticsData(
                    totalNotes = notes.size,
                    totalFolders = folders.size,
                    totalWords = notes.sumOf { it.wordCount },
                    averageWordsPerNote = if (notes.isNotEmpty()) notes.sumOf { it.wordCount } / notes.size else 0,
                    notesThisWeek = notes.count { 
                        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                        it.createdAt.toEpochMilliseconds() >= weekAgo 
                    },
                    notesThisMonth = notes.count { 
                        val monthAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
                        it.createdAt.toEpochMilliseconds() >= monthAgo 
                    },
                    mostUsedTags = notes.flatMap { it.tags }
                        .groupBy { it }
                        .mapValues { it.value.size }
                        .toList()
                        .sortedByDescending { it.second }
                        .take(5)
                        .map { it.first },
                    recentActivity = notes.sortedByDescending { it.updatedAt }.take(5),
                    notesByFolder = notes.groupBy { note ->
                        note.folderId?.let { folderId ->
                            folders.find { it.id == folderId }?.name ?: "Unknown Folder"
                        } ?: "Unorganized"
                    }.mapValues { it.value.size },
                    averageNotesPerDay = if (notes.isNotEmpty()) {
                        val daysSinceFirstNote = if (notes.isNotEmpty()) {
                            val firstNote = notes.minByOrNull { it.createdAt }
                            val days = (System.currentTimeMillis() - firstNote!!.createdAt.toEpochMilliseconds()) / (24 * 60 * 60 * 1000)
                            maxOf(1, days)
                        } else 1
                        notes.size.toFloat() / daysSinceFirstNote
                    } else 0f
                )
            }.collect { analyticsData ->
                _analytics.value = analyticsData
            }
        }
    }
    
    fun refreshAnalytics() {
        loadAnalytics()
    }
}

data class AnalyticsData(
    val totalNotes: Int = 0,
    val totalFolders: Int = 0,
    val totalWords: Int = 0,
    val averageWordsPerNote: Int = 0,
    val notesThisWeek: Int = 0,
    val notesThisMonth: Int = 0,
    val mostUsedTags: List<String> = emptyList(),
    val recentActivity: List<com.cogninote.app.data.entities.Note> = emptyList(),
    val notesByFolder: Map<String, Int> = emptyMap(),
    val averageNotesPerDay: Float = 0f
)
