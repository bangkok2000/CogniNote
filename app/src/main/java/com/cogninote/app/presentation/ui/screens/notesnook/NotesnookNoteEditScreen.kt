package com.cogninote.app.presentation.ui.screens.notesnook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cogninote.app.presentation.viewmodel.SimpleNoteEditViewModel
import com.cogninote.app.presentation.ui.components.formatting.TextFormat
import com.cogninote.app.presentation.ui.components.formatting.TextFormattingToolbar
import com.cogninote.app.presentation.ui.components.formatting.CompactFormattingToolbar
import com.cogninote.app.presentation.ui.components.formatting.KeyboardShortcutsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesnookNoteEditScreen(
    viewModel: SimpleNoteEditViewModel,
    noteId: String?,
    onNavigateBack: () -> Unit
) {
    val currentNote by viewModel.currentNote.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    
    var title by remember { mutableStateOf("") }
    var contentFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var isKeyboardVisible by remember { mutableStateOf(true) }
    var showFormattingToolbar by remember { mutableStateOf(false) }
    var showKeyboardShortcuts by remember { mutableStateOf(false) }
    
    // Observe formatting results
    val formattingResult by viewModel.textFormattingResult.collectAsStateWithLifecycle()
    
    val titleFocusRequester = remember { FocusRequester() }
    val contentFocusRequester = remember { FocusRequester() }
    
    // Load existing note or create new one
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.loadNote(noteId)
        } else {
            viewModel.clearNote()
            viewModel.createNewNote()
            title = ""
            contentFieldValue = TextFieldValue("")
        }
    }
    
    // Update local state when note loads
    LaunchedEffect(currentNote) {
        currentNote?.let { note ->
            if (noteId != null) {
                title = note.title
                contentFieldValue = TextFieldValue(
                    text = note.content,
                    selection = TextRange(note.content.length)
                )
            }
        }
    }
    
    // Handle formatting results
    LaunchedEffect(formattingResult) {
        formattingResult?.let { result ->
            contentFieldValue = TextFieldValue(
                text = result.newText,
                selection = TextRange(result.newCursorPosition)
            )
            viewModel.clearFormattingResult()
        }
    }
    
    // Auto-save on content change
    LaunchedEffect(title, contentFieldValue.text) {
        if (currentNote != null && (title.isNotEmpty() || contentFieldValue.text.isNotEmpty())) {
            viewModel.saveNote(title, contentFieldValue.text)
        }
    }
    
    // Handle keyboard shortcuts
    val handleKeyboardShortcut = { keyEvent: KeyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            val format = when {
                keyEvent.isCtrlPressed && keyEvent.key == Key.B -> TextFormat.BOLD
                keyEvent.isCtrlPressed && keyEvent.key == Key.I -> TextFormat.ITALIC
                keyEvent.isCtrlPressed && keyEvent.key == Key.U -> TextFormat.UNDERLINE
                keyEvent.isCtrlPressed && keyEvent.isShiftPressed && keyEvent.key == Key.X -> TextFormat.STRIKETHROUGH
                keyEvent.isCtrlPressed && keyEvent.key == Key.E -> TextFormat.CODE_INLINE
                keyEvent.isCtrlPressed && keyEvent.isShiftPressed && keyEvent.key == Key.E -> TextFormat.CODE_BLOCK
                keyEvent.isCtrlPressed && keyEvent.isShiftPressed && keyEvent.key == Key.Q -> TextFormat.QUOTE
                keyEvent.isCtrlPressed && keyEvent.isShiftPressed && keyEvent.key == Key.L -> TextFormat.BULLET_LIST
                keyEvent.isCtrlPressed && keyEvent.isShiftPressed && keyEvent.key == Key.N -> TextFormat.NUMBERED_LIST
                keyEvent.isCtrlPressed && keyEvent.isShiftPressed && keyEvent.key == Key.C -> TextFormat.CHECKBOX
                keyEvent.isCtrlPressed && keyEvent.key == Key.One -> TextFormat.HEADING_1
                keyEvent.isCtrlPressed && keyEvent.key == Key.Two -> TextFormat.HEADING_2
                keyEvent.isCtrlPressed && keyEvent.key == Key.Three -> TextFormat.HEADING_3
                keyEvent.isCtrlPressed && keyEvent.key == Key.K -> TextFormat.LINK
                else -> null
            }
            
            format?.let {
                viewModel.applyTextFormatting(
                    currentText = contentFieldValue.text,
                    cursorPosition = contentFieldValue.selection.start,
                    selectionStart = contentFieldValue.selection.start,
                    selectionEnd = contentFieldValue.selection.end,
                    format = it
                )
                true
            } ?: false
        } else false
    }
    
    Scaffold(
        topBar = {
            NotesnookEditorTopBar(
                title = title.ifEmpty { "Untitled" },
                onNavigateBack = onNavigateBack,
                isKeyboardVisible = isKeyboardVisible,
                onKeyboardToggle = {
                    if (isKeyboardVisible) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        isKeyboardVisible = false
                    } else {
                        contentFocusRequester.requestFocus()
                        isKeyboardVisible = true
                    }
                },
                showFormattingToolbar = showFormattingToolbar,
                onToggleFormattingToolbar = { showFormattingToolbar = !showFormattingToolbar },
                showKeyboardShortcuts = showKeyboardShortcuts,
                onToggleKeyboardShortcuts = { showKeyboardShortcuts = !showKeyboardShortcuts }
            )
        },
        bottomBar = {
            // Compact formatting toolbar at bottom
            CompactFormattingToolbar(
                isVisible = isKeyboardVisible && showFormattingToolbar,
                onFormatClick = { format ->
                    viewModel.applyTextFormatting(
                        currentText = contentFieldValue.text,
                        cursorPosition = contentFieldValue.selection.start,
                        selectionStart = contentFieldValue.selection.start,
                        selectionEnd = contentFieldValue.selection.end,
                        format = format
                    )
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Title field
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocusRequester),
                    textStyle = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 36.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box {
                            if (title.isEmpty()) {
                                Text(
                                    text = "Title",
                                    style = TextStyle(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.outline,
                                        lineHeight = 36.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Enhanced content field with formatting support
                BasicTextField(
                    value = contentFieldValue,
                    onValueChange = { newValue ->
                        contentFieldValue = newValue
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 400.dp)
                        .focusRequester(contentFocusRequester)
                        .onKeyEvent(handleKeyboardShortcut),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (contentFieldValue.text.isEmpty()) {
                                Text(
                                    text = "Start writing... Use Ctrl+B for bold, Ctrl+I for italic, or use the formatting toolbar",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.outline,
                                        lineHeight = 24.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Full formatting toolbar (when not in compact mode)
                TextFormattingToolbar(
                    isVisible = !isKeyboardVisible && showFormattingToolbar,
                    onFormatClick = { format ->
                        viewModel.applyTextFormatting(
                            currentText = contentFieldValue.text,
                            cursorPosition = contentFieldValue.selection.start,
                            selectionStart = contentFieldValue.selection.start,
                            selectionEnd = contentFieldValue.selection.end,
                            format = format
                        )
                        // Refocus content after formatting
                        contentFocusRequester.requestFocus()
                    }
                )
                
                // Keyboard shortcuts helper
                KeyboardShortcutsHelper(
                    isVisible = showKeyboardShortcuts
                )
                
                // Add bottom padding for keyboard
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
    
    // Auto-focus content field for new notes
    LaunchedEffect(Unit) {
        if (noteId == null) {
            contentFocusRequester.requestFocus()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesnookEditorTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    isKeyboardVisible: Boolean,
    onKeyboardToggle: () -> Unit,
    showFormattingToolbar: Boolean = false,
    onToggleFormattingToolbar: () -> Unit = {},
    showKeyboardShortcuts: Boolean = false,
    onToggleKeyboardShortcuts: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = onToggleFormattingToolbar) {
                Icon(
                    imageVector = if (showFormattingToolbar) Icons.Filled.FormatBold else Icons.Outlined.FormatBold,
                    contentDescription = if (showFormattingToolbar) "Hide formatting" else "Show formatting",
                    tint = if (showFormattingToolbar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onToggleKeyboardShortcuts) {
                Icon(
                    imageVector = if (showKeyboardShortcuts) Icons.Filled.Keyboard else Icons.Outlined.Keyboard,
                    contentDescription = if (showKeyboardShortcuts) "Hide shortcuts" else "Show shortcuts",
                    tint = if (showKeyboardShortcuts) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onKeyboardToggle) {
                Icon(
                    imageVector = if (isKeyboardVisible) Icons.Outlined.KeyboardHide else Icons.Outlined.Keyboard,
                    contentDescription = if (isKeyboardVisible) "Hide keyboard" else "Show keyboard"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun NotesnookEditorBottomBar(
    onFormatClick: () -> Unit,
    onInsertClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Format options
            IconButton(
                onClick = onFormatClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FormatBold,
                    contentDescription = "Add bold text",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = onInsertClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tag,
                    contentDescription = "Add hashtag",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Auto-save indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = "Saved",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Saved",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreHoriz,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}