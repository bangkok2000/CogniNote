package com.cogninote.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.cogninote.app.data.entities.Note
import com.cogninote.app.data.repository.SimplifiedNoteRepository
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: SimplifiedNoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTags = MutableStateFlow<List<String>>(emptyList())
    val selectedTags: StateFlow<List<String>> = _selectedTags.asStateFlow()

    private val _currentFolder = MutableStateFlow<String?>(null)
    val currentFolder: StateFlow<String?> = _currentFolder.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.ALL)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Combine all filters to get filtered notes
    val notes: StateFlow<List<Note>> = combine(
        searchQuery,
        selectedTags,
        currentFolder,
        viewMode
    ) { query, tags, folder, mode ->
        FilterParams(query, tags, folder, mode)
    }.flatMapLatest { params ->
        when {
            params.query.isNotBlank() -> noteRepository.searchNotes(params.query)
            params.tags.isNotEmpty() -> noteRepository.getNotesByTags(params.tags)
            params.folder != null -> noteRepository.getNotesByFolder(params.folder)
            params.mode == ViewMode.PINNED -> noteRepository.getPinnedNotes()
            params.mode == ViewMode.ARCHIVED -> noteRepository.getArchivedNotes()
            params.mode == ViewMode.DELETED -> noteRepository.getDeletedNotes()
            else -> noteRepository.getAllNotes()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTags: StateFlow<List<String>> = noteRepository.getAllUsedTags()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val statistics: StateFlow<NoteStatistics> = combine(
        noteRepository.getTotalNotesCount(),
        noteRepository.getTodayNotesCount(),
        noteRepository.getTotalWordCount()
    ) { total, today, words ->
        NoteStatistics(total, today, words)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NoteStatistics(0, 0, 0)
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun addSelectedTag(tag: String) {
        _selectedTags.value = (_selectedTags.value + tag).distinct()
    }

    fun removeSelectedTag(tag: String) {
        _selectedTags.value = _selectedTags.value - tag
    }

    fun clearSelectedTags() {
        _selectedTags.value = emptyList()
    }

    fun setCurrentFolder(folderId: String?) {
        _currentFolder.value = folderId
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun createNote(
        title: String = "",
        content: String = "",
        tags: List<String> = emptyList(),
        folderId: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                noteRepository.createNote(title, content, tags, folderId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(noteId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun restoreNote(noteId: String) {
        viewModelScope.launch {
            try {
                noteRepository.restoreNote(noteId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun togglePin(noteId: String) {
        viewModelScope.launch {
            try {
                noteRepository.togglePin(noteId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun toggleArchive(noteId: String) {
        viewModelScope.launch {
            try {
                noteRepository.toggleArchive(noteId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun moveToFolder(noteId: String, folderId: String?) {
        viewModelScope.launch {
            try {
                noteRepository.moveToFolder(noteId, folderId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun duplicateNote(noteId: String) {
        viewModelScope.launch {
            try {
                noteRepository.duplicateNote(noteId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            try {
                noteRepository.emptyTrash()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    suspend fun updateNote(note: Note) {
        try {
            _isLoading.value = true
            noteRepository.updateNote(note)
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to update note"
        } finally {
            _isLoading.value = false
        }
    }

    private data class FilterParams(
        val query: String,
        val tags: List<String>,
        val folder: String?,
        val mode: ViewMode
    )
}

enum class ViewMode {
    ALL, PINNED, ARCHIVED, DELETED
}

data class NoteStatistics(
    val totalNotes: Int,
    val todayNotes: Int,
    val totalWords: Int
)
