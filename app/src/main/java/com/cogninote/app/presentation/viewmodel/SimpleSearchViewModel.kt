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
class SimpleSearchViewModel @Inject constructor(
    private val repository: SimplifiedNoteRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Search results based on query
    val searchResults: StateFlow<List<Note>> = _searchQuery
        .debounce(300) // Wait for user to stop typing
        .flatMapLatest { query ->
            repository.searchNotes(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    // All available tags for suggestions
    val availableTags: StateFlow<List<String>> = repository.getAllUsedTags()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun searchForTag(tag: String) {
        _searchQuery.value = "#$tag"
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
    }
}