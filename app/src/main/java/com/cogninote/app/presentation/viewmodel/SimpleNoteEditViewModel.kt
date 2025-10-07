package com.cogninote.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.cogninote.app.data.entities.Note
import com.cogninote.app.data.repository.SimplifiedNoteRepository
import com.cogninote.app.presentation.ui.components.formatting.TextFormat
import com.cogninote.app.presentation.ui.components.formatting.TextFormatUtils
import com.cogninote.app.presentation.ui.components.formatting.FormatResult
import javax.inject.Inject

@HiltViewModel
class SimpleNoteEditViewModel @Inject constructor(
    private val repository: SimplifiedNoteRepository
) : ViewModel() {
    
    private val _currentNote = MutableStateFlow<Note?>(null)
    val currentNote: StateFlow<Note?> = _currentNote.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _availableHashtags = MutableStateFlow<List<String>>(emptyList())
    val availableHashtags: StateFlow<List<String>> = _availableHashtags.asStateFlow()
    
    // Text formatting state
    private val _textFormattingResult = MutableStateFlow<FormatResult?>(null)
    val textFormattingResult: StateFlow<FormatResult?> = _textFormattingResult.asStateFlow()
    
    init {
        // Load available hashtags when ViewModel is created
        loadAvailableHashtags()
    }
    
    private fun loadAvailableHashtags() {
        viewModelScope.launch {
            repository.getAllNotes().collect { notes ->
                val allTags = notes.flatMap { it.tags }.distinct().sorted()
                _availableHashtags.value = allTags
            }
        }
    }
    
    fun loadNote(noteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getNoteByIdSync(noteId)?.let { note ->
                _currentNote.value = note
            }
            _isLoading.value = false
        }
    }
    
    fun createNewNote(title: String = "", content: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            // Clear any previous note state first
            _currentNote.value = null
            val note = repository.createNote(title, content)
            _currentNote.value = note
            _isLoading.value = false
        }
    }
    
    fun clearNote() {
        _currentNote.value = null
    }
    
    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
            _currentNote.value = note
        }
    }
    
    fun saveNote(title: String, content: String) {
        val note = _currentNote.value
        if (note != null) {
            val updatedNote = note.copy(
                title = title.ifEmpty { content.lines().firstOrNull()?.take(50)?.trim() ?: "Untitled" },
                content = content
            )
            updateNote(updatedNote)
        }
    }
    
    /**
     * Apply text formatting to the current text
     */
    fun applyTextFormatting(
        currentText: String,
        cursorPosition: Int,
        selectionStart: Int,
        selectionEnd: Int,
        format: TextFormat
    ) {
        val result = TextFormatUtils.applyFormatting(
            currentText = currentText,
            cursorPosition = cursorPosition,
            selectionStart = selectionStart,
            selectionEnd = selectionEnd,
            format = format
        )
        _textFormattingResult.value = result
    }
    
    /**
     * Clear the formatting result after it's been applied
     */
    fun clearFormattingResult() {
        _textFormattingResult.value = null
    }
    
    /**
     * Check if text contains markdown formatting
     */
    fun hasMarkdownFormatting(text: String): Boolean {
        val markdownPatterns = listOf(
            "\\*\\*.*?\\*\\*",  // Bold
            "\\*.*?\\*",        // Italic
            "__.*?__",          // Underline
            "~~.*?~~",          // Strikethrough
            "`.*?`",            // Inline code
            "```[\\s\\S]*?```", // Code block
            "^#+\\s+",          // Headings
            "^>\\s+",           // Quote
            "^[-*+]\\s+",       // Bullet list
            "^\\d+\\.\\s+",     // Numbered list
            "^-\\s+\\[[ x]\\]\\s+" // Checkbox
        )
        
        return markdownPatterns.any { pattern ->
            text.contains(Regex(pattern, RegexOption.MULTILINE))
        }
    }
    
    /**
     * Get markdown preview of text (simplified)
     */
    fun getMarkdownPreview(text: String): String {
        var preview = text
        
        // Simple markdown to text conversion for preview
        preview = preview
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1") // Bold
            .replace(Regex("\\*(.*?)\\*"), "$1")       // Italic
            .replace(Regex("__(.*?)__"), "$1")         // Underline
            .replace(Regex("~~(.*?)~~"), "$1")         // Strikethrough
            .replace(Regex("`(.*?)`"), "$1")           // Inline code
            .replace(Regex("```[\\s\\S]*?```"), "[Code Block]") // Code block
            .replace(Regex("^#+\\s+(.*)$", RegexOption.MULTILINE), "$1") // Headings
            .replace(Regex("^>\\s+(.*)$", RegexOption.MULTILINE), "$1") // Quote
            .replace(Regex("^[-*+]\\s+(.*)$", RegexOption.MULTILINE), "• $1") // Bullet list
            .replace(Regex("^\\d+\\.\\s+(.*)$", RegexOption.MULTILINE), "$1") // Numbered list
            .replace(Regex("^-\\s+\\[[ x]\\]\\s+(.*)$", RegexOption.MULTILINE), "$1") // Checkbox
        
        return preview
    }
}