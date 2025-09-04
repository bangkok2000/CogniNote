package com.cogninote.app.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cogninote.app.data.entities.Folder
import com.cogninote.app.data.entities.Note
import com.cogninote.app.presentation.ui.components.ModernNoteCard
import com.cogninote.app.presentation.ui.theme.*
import com.cogninote.app.presentation.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderContentScreen(
    folder: Folder,
    onNavigateBack: () -> Unit,
    onNoteClick: (String) -> Unit,
    onMoveNotesToFolder: (List<String>, String?) -> Unit,
    onCreateNote: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    var selectedNotes by remember { mutableStateOf(setOf<String>()) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var showAddNotesDialog by remember { mutableStateOf(false) }
    
    // Filter notes by folder
    val folderNotes = notes.filter { note ->
        note.folderId == folder.id
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Folder info header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${folderNotes.size} notes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selection mode controls
            if (selectedNotes.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedNotes.size} selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryBlue
                        )
                        
                        Row {
                            IconButton(
                                onClick = { showMoveDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DriveFileMove,
                                    contentDescription = "Move notes",
                                    tint = PrimaryBlue
                                )
                            }
                            
                            IconButton(
                                onClick = { selectedNotes = emptySet() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel selection",
                                    tint = PrimaryBlue
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Notes list
            if (folderNotes.isEmpty()) {
                EmptyFolderState(
                    folderName = folder.name,
                    onCreateNote = onCreateNote,
                    onAddExistingNotes = { showAddNotesDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(folderNotes) { note ->
                        ModernNoteCard(
                            note = note,
                            onClick = { 
                                if (selectedNotes.isNotEmpty()) {
                                    // Toggle selection
                                    selectedNotes = if (selectedNotes.contains(note.id)) {
                                        selectedNotes - note.id
                                    } else {
                                        selectedNotes + note.id
                                    }
                                } else {
                                    onNoteClick(note.id)
                                }
                            },
                            onTogglePin = { /* TODO: Implement pin toggle */ },
                            onToggleArchive = { /* TODO: Implement archive toggle */ },
                            onDelete = { /* TODO: Implement delete */ },
                            onRestore = { /* TODO: Implement restore */ }
                        )
                    }
                }
            }
        }
        
        // Move notes dialog
        if (showMoveDialog) {
            MoveNotesDialog(
                selectedNotes = selectedNotes.toList(),
                currentFolder = folder,
                onMoveNotes = { targetFolderId ->
                    onMoveNotesToFolder(selectedNotes.toList(), targetFolderId)
                    selectedNotes = emptySet()
                    showMoveDialog = false
                },
                onDismiss = { showMoveDialog = false }
            )
        }
        
        // Add existing notes dialog
        if (showAddNotesDialog) {
            AddExistingNotesDialog(
                allNotes = notes.filter { it.folderId != folder.id }, // Notes not in this folder
                onAddNotes = { noteIds ->
                    onMoveNotesToFolder(noteIds, folder.id)
                    showAddNotesDialog = false
                },
                onDismiss = { showAddNotesDialog = false }
            )
        }
    }
}

@Composable
private fun EmptyFolderState(
    folderName: String,
    onCreateNote: () -> Unit,
    onAddExistingNotes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No notes in $folderName",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add your first note to this folder",
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Create new note button
        Button(
            onClick = onCreateNote,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Note")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Add existing notes button
        OutlinedButton(
            onClick = onAddExistingNotes,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Existing Notes")
        }
    }
}

@Composable
private fun MoveNotesDialog(
    selectedNotes: List<String>,
    currentFolder: Folder,
    onMoveNotes: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    // TODO: Implement move notes dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move Notes") },
        text = { Text("Move ${selectedNotes.size} notes to another folder?") },
        confirmButton = {
            TextButton(onClick = { onMoveNotes(null) }) {
                Text("Move to Root")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddExistingNotesDialog(
    allNotes: List<Note>,
    onAddNotes: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedNotes by remember { mutableStateOf(setOf<String>()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Notes to Folder") },
        text = {
            Column {
                Text(
                    text = "Select notes to add to this folder:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (allNotes.isEmpty()) {
                    Text(
                        text = "No notes available to add",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allNotes) { note ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedNotes = if (selectedNotes.contains(note.id)) {
                                            selectedNotes - note.id
                                        } else {
                                            selectedNotes + note.id
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedNotes.contains(note.id)) {
                                        PrimaryBlue.copy(alpha = 0.1f)
                                    } else {
                                        CardBackground
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedNotes.contains(note.id),
                                        onCheckedChange = { isChecked ->
                                            selectedNotes = if (isChecked) {
                                                selectedNotes + note.id
                                            } else {
                                                selectedNotes - note.id
                                            }
                                        }
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = note.title.ifEmpty { "Untitled" },
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextPrimary
                                        )
                                        if (note.content.isNotEmpty()) {
                                            Text(
                                                text = note.content.take(100) + if (note.content.length > 100) "..." else "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAddNotes(selectedNotes.toList()) },
                enabled = selectedNotes.isNotEmpty()
            ) {
                Text("Add ${selectedNotes.size} Notes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
