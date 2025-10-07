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
class SimpleNotesViewModel @Inject constructor(
    private val repository: SimplifiedNoteRepository
) : ViewModel() {
    
    // Simple state management - get all notes
    val notes: StateFlow<List<Note>> = repository.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
    
    fun deleteNoteById(noteId: String) {
        viewModelScope.launch {
            repository.deleteNoteById(noteId)
        }
    }
    
    fun togglePin(noteId: String) {
        viewModelScope.launch {
            repository.togglePin(noteId)
        }
    }
}