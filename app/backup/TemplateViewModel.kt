package com.cogninote.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.cogninote.app.data.entities.NoteTemplate
import com.cogninote.app.data.entities.TemplateCategory
import com.cogninote.app.data.repository.TemplateRepository
import com.cogninote.app.data.repository.TemplateStatistics
import javax.inject.Inject

@HiltViewModel
class TemplateViewModel @Inject constructor(
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<TemplateCategory?>(null)
    val selectedCategory: StateFlow<TemplateCategory?> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Combine search and category filters
    val templates: StateFlow<List<NoteTemplate>> = combine(
        searchQuery,
        selectedCategory
    ) { query, category ->
        FilterParams(query, category)
    }.flatMapLatest { params ->
        when {
            params.query.isNotBlank() -> templateRepository.searchTemplates(params.query)
            params.category != null -> templateRepository.getTemplatesByCategory(params.category)
            else -> templateRepository.getAllTemplates()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categories: StateFlow<List<TemplateCategory>> = templateRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val builtInTemplates: StateFlow<List<NoteTemplate>> = templateRepository.getBuiltInTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customTemplates: StateFlow<List<NoteTemplate>> = templateRepository.getCustomTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val mostUsedTemplates: StateFlow<List<NoteTemplate>> = templateRepository.getMostUsedTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _statistics = MutableStateFlow<TemplateStatistics?>(null)
    val statistics: StateFlow<TemplateStatistics?> = _statistics.asStateFlow()

    init {
        initializeTemplates()
        loadStatistics()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun setSelectedCategory(category: TemplateCategory?) {
        _selectedCategory.value = category
    }

    fun loadTemplates() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Templates are loaded via StateFlow, this just triggers loading
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createTemplate(
        name: String,
        description: String,
        category: TemplateCategory,
        content: String,
        placeholders: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        isPublic: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                templateRepository.createTemplate(
                    name = name,
                    description = description,
                    category = category,
                    content = content,
                    placeholders = placeholders,
                    tags = tags,
                    isPublic = isPublic
                )
                _error.value = null
                loadStatistics()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTemplate(template: NoteTemplate) {
        viewModelScope.launch {
            try {
                templateRepository.updateTemplate(template)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            try {
                templateRepository.deleteTemplate(templateId)
                _error.value = null
                loadStatistics()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun duplicateTemplate(templateId: String) {
        viewModelScope.launch {
            try {
                templateRepository.duplicateTemplate(templateId)
                _error.value = null
                loadStatistics()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun useTemplate(templateId: String) {
        viewModelScope.launch {
            try {
                templateRepository.useTemplate(templateId)
                _error.value = null
                loadStatistics()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun createNoteFromTemplate(templateId: String, placeholderValues: Map<String, String> = emptyMap()): Flow<String> {
        return flow {
            try {
                val content = templateRepository.createNoteFromTemplate(templateId, placeholderValues)
                emit(content)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
                emit("")
            }
        }
    }

    fun exportTemplate(templateId: String): Flow<String?> {
        return flow {
            try {
                val exportedJson = templateRepository.exportTemplate(templateId)
                emit(exportedJson)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
                emit(null)
            }
        }
    }

    fun importTemplate(templateJson: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = templateRepository.importTemplate(templateJson)
                if (result.isSuccess) {
                    _error.value = null
                    loadStatistics()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Import failed"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getTemplateById(templateId: String): Flow<NoteTemplate?> {
        return templateRepository.getTemplateById(templateId)
    }

    private fun initializeTemplates() {
        viewModelScope.launch {
            try {
                templateRepository.initializeBuiltInTemplates()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                val stats = templateRepository.getTemplateStatistics()
                _statistics.value = stats
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun refreshBuiltInTemplates() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                templateRepository.updateBuiltInTemplates()
                _error.value = null
                loadStatistics()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private data class FilterParams(
        val query: String,
        val category: TemplateCategory?
    )
}
