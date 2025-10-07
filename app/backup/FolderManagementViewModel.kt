package com.cogninote.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cogninote.app.data.entities.Folder
import com.cogninote.app.data.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderManagementViewModel @Inject constructor(
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadFolders()
    }

    fun loadFolders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                folderRepository.getAllFolders().collect { folderList ->
                    _folders.value = folderList
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load folders"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createFolder(name: String, parentId: String?) {
        viewModelScope.launch {
            try {
                val folder = Folder(
                    name = name,
                    parentFolderId = parentId
                )
                folderRepository.insertFolder(folder)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to create folder"
            }
        }
    }

    fun updateFolder(folder: Folder) {
        viewModelScope.launch {
            try {
                folderRepository.updateFolder(folder)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update folder"
            }
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            try {
                folderRepository.deleteFolder(folder)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete folder"
            }
        }
    }

    fun moveFolder(folder: Folder, newParentId: String?) {
        viewModelScope.launch {
            try {
                val updatedFolder = folder.copy(parentFolderId = newParentId)
                folderRepository.updateFolder(updatedFolder)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to move folder"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
