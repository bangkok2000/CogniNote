package com.cogninote.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.cogninote.app.data.entities.Note
import com.cogninote.app.data.repository.NoteRepository
import com.cogninote.app.data.repository.TemplateRepository
import com.cogninote.app.utils.TextUtils
import javax.inject.Inject

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val _noteId = MutableStateFlow<String?>(null)
    val noteId: StateFlow<String?> = _noteId.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    private val _wordCount = MutableStateFlow(0)
    val wordCount: StateFlow<Int> = _wordCount.asStateFlow()

    private val _isNewNote = MutableStateFlow(true)
    val isNewNote: StateFlow<Boolean> = _isNewNote.asStateFlow()

    private val _folderId = MutableStateFlow<String?>(null)
    val folderId: StateFlow<String?> = _folderId.asStateFlow()

    private var originalNote: Note? = null

    fun resetForNewNote() {
        _noteId.value = null
        _title.value = ""
        _content.value = ""
        _tags.value = emptyList()
        _isNewNote.value = true
        _hasUnsavedChanges.value = false
        _wordCount.value = 0
        _error.value = null
        _folderId.value = null
        originalNote = null
    }

    fun setFolderId(folderId: String?) {
        _folderId.value = folderId
    }

    // Auto-save functionality
    private val autoSaveFlow = combine(
        title,
        content,
        tags
    ) { title, content, tags ->
        Triple(title, content, tags)
    }.drop(1) // Skip initial emission
        .debounce(2000) // Wait 2 seconds after last change
        .onEach { 
            if (hasUnsavedChanges.value) {
                autoSave()
            }
        }

    init {
        viewModelScope.launch {
            autoSaveFlow.collect()
        }
    }

    fun loadNote(noteId: String?) {
        if (noteId == null) {
            _isNewNote.value = true
            return
        }

        _noteId.value = noteId
        _isNewNote.value = false

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val note = noteRepository.getNoteByIdSuspend(noteId)
                
                if (note != null) {
                    originalNote = note
                    _title.value = note.title
                    _content.value = note.content
                    _tags.value = note.tags
                    updateWordCount(note.content)
                } else {
                    _error.value = "Note not found"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNoteFromTemplate(templateId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _isNewNote.value = true
                
                val templateContent = templateRepository.createNoteFromTemplate(templateId)
                val template = templateRepository.getTemplateById(templateId).first()
                
                if (template != null) {
                    _title.value = template.name
                    _content.value = templateContent
                    _tags.value = template.tags
                    updateWordCount(templateContent)
                    
                    // Reset states for new note
                    _noteId.value = null
                    originalNote = null
                    _hasUnsavedChanges.value = true // Mark as unsaved since it's new content
                } else {
                    _error.value = "Template not found"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
        checkForChanges()
    }

    fun updateContent(newContent: String) {
        _content.value = newContent
        updateWordCount(newContent)
        checkForChanges()
    }

    fun addTag(tag: String) {
        val trimmedTag = tag.trim()
        if (trimmedTag.isNotBlank() && trimmedTag !in _tags.value) {
            _tags.value = _tags.value + trimmedTag
            checkForChanges()
        }
    }

    fun removeTag(tag: String) {
        _tags.value = _tags.value - tag
        checkForChanges()
    }

    fun saveNote() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val currentNoteId = _noteId.value
                if (currentNoteId == null || _isNewNote.value) {
                    // Create new note
                    val newNote = noteRepository.createNote(
                        title = _title.value,
                        content = _content.value,
                        tags = _tags.value,
                        folderId = _folderId.value
                    )
                    _noteId.value = newNote.id
                    _isNewNote.value = false
                    originalNote = newNote
                } else {
                    // Update existing note
                    val updatedNote = originalNote?.copy(
                        title = _title.value,
                        content = _content.value,
                        tags = _tags.value
                    )
                    
                    if (updatedNote != null) {
                        noteRepository.updateNote(updatedNote)
                        originalNote = updatedNote
                    }
                }
                
                _hasUnsavedChanges.value = false
                _error.value = null
                
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun autoSave() {
        if (_isNewNote.value && _title.value.isBlank() && _content.value.isBlank()) {
            return // Don't auto-save empty new notes
        }
        
        saveNote()
    }

    fun deleteNote() {
        val currentNoteId = _noteId.value ?: return
        
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(currentNoteId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun extractLinksFromContent() {
        val links = TextUtils.extractNoteLinks(_content.value)
        val hashtags = TextUtils.extractHashtags(_content.value)
        
        // Auto-add hashtags as tags
        hashtags.forEach { hashtag ->
            addTag(hashtag)
        }
        
        // Update backlinks if note is saved
        val currentNoteId = _noteId.value
        if (currentNoteId != null && !_isNewNote.value) {
            viewModelScope.launch {
                noteRepository.updateBacklinks(currentNoteId, links)
            }
        }
    }

    private fun updateWordCount(content: String) {
        val plainText = TextUtils.extractPlainText(content)
        _wordCount.value = TextUtils.countWords(plainText)
    }

    private fun checkForChanges() {
        val original = originalNote
        _hasUnsavedChanges.value = if (original == null) {
            _title.value.isNotBlank() || _content.value.isNotBlank() || _tags.value.isNotEmpty()
        } else {
            _title.value != original.title ||
            _content.value != original.content ||
            _tags.value != original.tags
        }
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Save any pending changes when ViewModel is cleared
        if (_hasUnsavedChanges.value) {
            autoSave()
        }
    }
}
