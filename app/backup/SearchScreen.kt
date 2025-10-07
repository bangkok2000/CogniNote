package com.cogninote.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cogninote.app.presentation.viewmodel.NotesViewModel
import com.cogninote.app.presentation.ui.components.NoteCard
import com.cogninote.app.presentation.ui.components.EmptyStateMessage
import com.cogninote.app.presentation.ui.components.TagRow

@Composable
fun SearchScreen(
    viewModel: NotesViewModel,
    onNoteClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()
    val selectedTags by viewModel.selectedTags.collectAsStateWithLifecycle()
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    
    // Auto-focus search field when screen appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::setSearchQuery,
            label = { Text("Search notes...") },
            placeholder = { Text("Enter keywords to search") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .focusRequester(focusRequester),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                }
            )
        )

        // Tag filters
        if (allTags.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Filter by tags",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                TagRow(
                    tags = allTags.take(10), // Show first 10 tags
                    onTagClick = { tag ->
                        if (tag in selectedTags) {
                            viewModel.removeSelectedTag(tag)
                        } else {
                            viewModel.addSelectedTag(tag)
                        }
                    }
                )
                
                // Selected tags
                if (selectedTags.isNotEmpty()) {
                    Text(
                        text = "Selected tags",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    
                    TagRow(
                        tags = selectedTags,
                        onTagClick = viewModel::removeSelectedTag
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }

        // Search results
        when {
            searchQuery.isBlank() && selectedTags.isEmpty() -> {
                EmptyStateMessage(
                    icon = Icons.Default.Search,
                    title = "Search your notes",
                    subtitle = "Enter keywords or select tags to find notes"
                )
            }
            notes.isEmpty() -> {
                EmptyStateMessage(
                    icon = Icons.Default.SearchOff,
                    title = "No results found",
                    subtitle = "Try different keywords or check your spelling"
                )
            }
            else -> {
                Column {
                    // Results header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${notes.size} result${if (notes.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (searchQuery.isNotBlank()) {
                            Text(
                                text = "for \"$searchQuery\"",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Results list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = notes,
                            key = { it.id }
                        ) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onNoteClick(note.id) },
                                onTogglePin = { viewModel.togglePin(note.id) },
                                onToggleArchive = { viewModel.toggleArchive(note.id) },
                                onDelete = { viewModel.deleteNote(note.id) },
                                onRestore = { viewModel.restoreNote(note.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}