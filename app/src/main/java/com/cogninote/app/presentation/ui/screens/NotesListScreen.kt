package com.cogninote.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.content.Intent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.cogninote.app.data.entities.Note
import com.cogninote.app.presentation.viewmodel.NotesViewModel
import com.cogninote.app.presentation.viewmodel.ExportViewModel
import com.cogninote.app.presentation.viewmodel.ViewMode
import com.cogninote.app.presentation.ui.components.ModernNoteCard
import com.cogninote.app.presentation.ui.components.EmptyStateMessage
import com.cogninote.app.presentation.ui.components.ViewModeSelector
import com.cogninote.app.presentation.ui.components.ExportDialog
import com.cogninote.app.services.ExportFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    onNoteClick: (String) -> Unit
) {
    val exportViewModel: ExportViewModel = hiltViewModel()
    val context = LocalContext.current
    
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    val error by viewModel.error.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    
    var showViewModeSelector by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Statistics and view mode row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${statistics.totalNotes} notes • ${statistics.totalWords} words",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            FilledTonalButton(
                onClick = { showViewModeSelector = true }
            ) {
                Icon(
                    imageVector = when (viewMode) {
                        ViewMode.ALL -> Icons.Default.ViewList
                        ViewMode.PINNED -> Icons.Default.PushPin
                        ViewMode.ARCHIVED -> Icons.Default.Archive
                        ViewMode.DELETED -> Icons.Default.Delete
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (viewMode) {
                        ViewMode.ALL -> "All Notes"
                        ViewMode.PINNED -> "Pinned"
                        ViewMode.ARCHIVED -> "Archived"
                        ViewMode.DELETED -> "Deleted"
                    }
                )
            }
        }

        // Error handling
        error?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Notes list
        when {
            notes.isEmpty() && !isLoading -> {
                EmptyStateMessage(
                    icon = when (viewMode) {
                        ViewMode.ALL -> Icons.Default.Note
                        ViewMode.PINNED -> Icons.Default.PushPin
                        ViewMode.ARCHIVED -> Icons.Default.Archive
                        ViewMode.DELETED -> Icons.Default.Delete
                    },
                    title = when (viewMode) {
                        ViewMode.ALL -> "No notes yet"
                        ViewMode.PINNED -> "No pinned notes"
                        ViewMode.ARCHIVED -> "No archived notes"
                        ViewMode.DELETED -> "No deleted notes"
                    },
                    subtitle = when (viewMode) {
                        ViewMode.ALL -> "Create your first note to get started"
                        ViewMode.PINNED -> "Pin notes to keep them at the top"
                        ViewMode.ARCHIVED -> "Archived notes will appear here"
                        ViewMode.DELETED -> "Deleted notes will appear here"
                    }
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = notes,
                        key = { it.id }
                    ) { note ->
                                                        ModernNoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onTogglePin = { viewModel.togglePin(note.id) },
                            onToggleArchive = { viewModel.toggleArchive(note.id) },
                            onDelete = { viewModel.deleteNote(note.id) },
                            onRestore = { viewModel.restoreNote(note.id) },
                            onExport = {
                                selectedNote = note
                                showExportDialog = true
                            },
                            showArchiveActions = viewMode != ViewMode.DELETED
                        )
                    }
                }
            }
        }
    }

    // Export Dialog
    ExportDialog(
        isVisible = showExportDialog,
        onDismiss = { 
            showExportDialog = false
            selectedNote = null
        },
        onExport = { format, includeMetadata ->
            selectedNote?.let { note ->
                val intent = exportViewModel.exportNote(note, format, includeMetadata)
                intent?.let { 
                    context.startActivity(Intent.createChooser(it, "Export Note"))
                }
            }
        },
        noteCount = 1
    )

    // View Mode Selector Bottom Sheet
    if (showViewModeSelector) {
        ModalBottomSheet(
            onDismissRequest = { showViewModeSelector = false }
        ) {
            ViewModeSelector(
                currentMode = viewMode,
                onModeSelected = { mode ->
                    viewModel.setViewMode(mode)
                    showViewModeSelector = false
                }
            )
        }
    }
}
