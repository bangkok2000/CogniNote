package com.cogninote.app.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cogninote.app.data.entities.Note
import com.cogninote.app.services.ExportFormat
import com.cogninote.app.services.ExportImportService
import com.cogninote.app.sharing.ShareManager
import com.cogninote.app.sharing.ExportFormat as ShareExportFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exportImportService: ExportImportService,
    private val shareManager: ShareManager
) : ViewModel() {
    
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()
    
    private val _exportError = MutableStateFlow<String?>(null)
    val exportError: StateFlow<String?> = _exportError.asStateFlow()
    
    fun exportNote(
        note: Note,
        format: ExportFormat,
        includeMetadata: Boolean = true
    ): Intent? {
        return try {
            shareManager.exportMultipleNotes(listOf(note), format.toShareFormat())
        } catch (e: Exception) {
            _exportError.value = e.message ?: "Export failed"
            null
        }
    }
    
    fun exportNotes(
        notes: List<Note>,
        format: ExportFormat,
        includeMetadata: Boolean = true
    ): Intent? {
        return try {
            shareManager.exportMultipleNotes(notes, format.toShareFormat())
        } catch (e: Exception) {
            _exportError.value = e.message ?: "Export failed"
            null
        }
    }
    
    private fun ExportFormat.toShareFormat(): ShareExportFormat {
        return when (this) {
            ExportFormat.PDF -> ShareExportFormat.PDF
            ExportFormat.PLAIN_TEXT -> ShareExportFormat.TEXT
            ExportFormat.MARKDOWN -> ShareExportFormat.MARKDOWN
            ExportFormat.HTML -> ShareExportFormat.HTML
            ExportFormat.JSON -> ShareExportFormat.JSON
            ExportFormat.EVERNOTE_ENEX -> ShareExportFormat.JSON // Fallback
        }
    }
    
    fun exportNotesToFile(
        notes: List<Note>,
        format: ExportFormat,
        includeMetadata: Boolean = true
    ) {
        viewModelScope.launch {
            _isExporting.value = true
            _exportError.value = null
            
            try {
                val result = exportImportService.exportNotes(notes, format, includeMetadata)
                result.fold(
                    onSuccess = { uri ->
                        // File saved successfully
                        // Could show a toast or notification here
                    },
                    onFailure = { error ->
                        _exportError.value = error.message ?: "Export failed"
                    }
                )
            } catch (e: Exception) {
                _exportError.value = e.message ?: "Export failed"
            } finally {
                _isExporting.value = false
            }
        }
    }
    
    fun clearError() {
        _exportError.value = null
    }
}
