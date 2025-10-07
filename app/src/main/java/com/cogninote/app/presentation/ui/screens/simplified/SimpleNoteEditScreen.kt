package com.cogninote.app.presentation.ui.screens.simplified

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cogninote.app.presentation.viewmodel.SimpleNoteEditViewModel
import com.cogninote.app.presentation.ui.components.HashtagText
import com.cogninote.app.presentation.ui.components.HashtagChips

@Composable
fun SimpleNoteEditScreen(
    viewModel: SimpleNoteEditViewModel,
    noteId: String?,
    onSaveComplete: () -> Unit
) {
    val currentNote by viewModel.currentNote.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val availableHashtags by viewModel.availableHashtags.collectAsStateWithLifecycle()
    
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var showHashtagHelper by remember { mutableStateOf(false) }
    
    // Load existing note or create new one
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.loadNote(noteId)
        } else {
            viewModel.clearNote() // Clear previous state
            viewModel.createNewNote()
            // Reset local state for new note
            title = ""
            content = ""
        }
    }
    
    // Update local state when note loads (only for existing notes)
    LaunchedEffect(currentNote) {
        currentNote?.let { note ->
            if (noteId != null) { // Only update for existing notes, not new ones
                title = note.title
                content = note.content
            }
        }
    }
    
    // Auto-save on content change
    LaunchedEffect(title, content) {
        if (currentNote != null && (title.isNotEmpty() || content.isNotEmpty())) {
            viewModel.saveNote(title, content)
        }
    }
    
    val contentFocusRequester = remember { FocusRequester() }
    
    // Auto-focus on content field for immediate typing
    LaunchedEffect(Unit) {
        contentFocusRequester.requestFocus()
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            // Elegant title input
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) {
                            Text(
                                "Title (optional)",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Hashtag helper section
            AnimatedVisibility(
                visible = availableHashtags.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tag,
                                    contentDescription = "Hashtags",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Hashtags",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = { showHashtagHelper = !showHashtagHelper }
                            ) {
                                Text(
                                    text = if (showHashtagHelper) "Hide" else "Show",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        AnimatedVisibility(visible = showHashtagHelper) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap hashtags to add them to your note:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(availableHashtags.take(8)) { tag ->
                                        FilterChip(
                                            onClick = {
                                                val newContent = if (content.isEmpty()) {
                                                    "#$tag "
                                                } else {
                                                    "$content #$tag "
                                                }
                                                content = newContent
                                            },
                                            label = { Text("#$tag") },
                                            selected = content.contains("#$tag"),
                                            modifier = Modifier
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Enhanced content input with hashtag visualization
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    BasicTextField(
                        value = content,
                        onValueChange = { newContent ->
                            content = newContent
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .focusRequester(contentFocusRequester),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (content.isEmpty()) {
                                    Column {
                                        Text(
                                            "Start typing your thoughts...",
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                lineHeight = 24.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "💡 Use #hashtags to organize your notes",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                } else {
                                    // Show content with hashtags highlighted
                                    HashtagText(
                                        text = content,
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            lineHeight = 24.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                                // Invisible text field for input
                                Box(modifier = Modifier.fillMaxSize()) {
                                    innerTextField()
                                }
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Auto-save indicator with elegant styling
            AnimatedVisibility(
                visible = title.isNotEmpty() || content.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Saved",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Automatically saved",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}