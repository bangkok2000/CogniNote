package com.cogninote.app.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cogninote.app.ai.ContentAssistant
import com.cogninote.app.ai.ContentSuggestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedRichTextEditor(
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentAssistant: ContentAssistant? = null,
    showAIAssistance: Boolean = true
) {
    var showFormatting by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<ContentSuggestion>>(emptyList()) }
    var aiSummary by remember { mutableStateOf<String?>(null) }
    
    // Update AI suggestions when content changes
    LaunchedEffect(content) {
        if (contentAssistant != null && content.length > 50) {
            suggestions = contentAssistant.suggestImprovements(content)
        }
    }

    Column(modifier = modifier) {
        // Formatting Toolbar
        if (showFormatting) {
            FormattingToolbar(
                onFormatAction = { action ->
                    val newContent = applyFormatting(content, action)
                    onContentChange(newContent)
                },
                onAISummary = {
                    if (contentAssistant != null && content.isNotEmpty()) {
                        aiSummary = contentAssistant.generateSummary(content)
                    }
                }
            )
        }

        // Main Text Editor
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            BasicTextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
                    .padding(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    if (content.isEmpty()) {
                        Text(
                            text = "Start writing your note...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            )
        }

        // Editor Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { showFormatting = !showFormatting }) {
                    Icon(
                        imageVector = Icons.Default.FormatBold,
                        contentDescription = "Toggle Formatting"
                    )
                }
                
                if (contentAssistant != null) {
                    IconButton(
                        onClick = {
                            if (content.isNotEmpty()) {
                                suggestions = contentAssistant.suggestImprovements(content)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Suggestions"
                        )
                    }
                }
            }

            Text(
                text = "${content.length} characters",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // AI Summary Card
        if (aiSummary != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(
                            onClick = { aiSummary = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Summary",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = aiSummary!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // AI Suggestions
        if (suggestions.isNotEmpty() && showAIAssistance) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(suggestions) { suggestion ->
                    SuggestionChip(
                        suggestion = suggestion,
                        onClick = {
                            // Handle suggestion action
                            when (suggestion.type) {
                                com.cogninote.app.ai.SuggestionType.STRUCTURE -> {
                                    // Add structure formatting
                                }
                                com.cogninote.app.ai.SuggestionType.TASK -> {
                                    // Extract tasks
                                }
                                else -> {
                                    // Other suggestions
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FormattingToolbar(
    onFormatAction: (FormattingAction) -> Unit,
    onAISummary: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            item {
                FormattingButton(
                    icon = Icons.Default.FormatBold,
                    contentDescription = "Bold",
                    onClick = { onFormatAction(FormattingAction.BOLD) }
                )
            }
            item {
                FormattingButton(
                    icon = Icons.Default.FormatItalic,
                    contentDescription = "Italic",
                    onClick = { onFormatAction(FormattingAction.ITALIC) }
                )
            }
            item {
                FormattingButton(
                    icon = Icons.Default.FormatListBulleted,
                    contentDescription = "Bullet List",
                    onClick = { onFormatAction(FormattingAction.BULLET_LIST) }
                )
            }
            item {
                FormattingButton(
                    icon = Icons.Default.FormatListNumbered,
                    contentDescription = "Numbered List",
                    onClick = { onFormatAction(FormattingAction.NUMBERED_LIST) }
                )
            }
            item {
                FormattingButton(
                    icon = Icons.Default.Title,
                    contentDescription = "Header",
                    onClick = { onFormatAction(FormattingAction.HEADER) }
                )
            }
            item {
                FormattingButton(
                    icon = Icons.Default.AutoAwesome,
                    contentDescription = "AI Summary",
                    onClick = onAISummary
                )
            }
        }
    }
}

@Composable
private fun FormattingButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuggestionChip(
    suggestion: ContentSuggestion,
    onClick: () -> Unit
) {
    val containerColor = when (suggestion.priority) {
        com.cogninote.app.ai.SuggestionPriority.HIGH -> MaterialTheme.colorScheme.errorContainer
        com.cogninote.app.ai.SuggestionPriority.MEDIUM -> MaterialTheme.colorScheme.primaryContainer
        com.cogninote.app.ai.SuggestionPriority.LOW -> MaterialTheme.colorScheme.surfaceVariant
    }

    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = suggestion.message,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2
            )
        },
        leadingIcon = {
            Icon(
                imageVector = when (suggestion.type) {
                    com.cogninote.app.ai.SuggestionType.STRUCTURE -> Icons.Default.ViewList
                    com.cogninote.app.ai.SuggestionType.TASK -> Icons.Default.Task
                    com.cogninote.app.ai.SuggestionType.REMINDER -> Icons.Default.Schedule
                    com.cogninote.app.ai.SuggestionType.TAG -> Icons.Default.Tag
                    com.cogninote.app.ai.SuggestionType.FORMATTING -> Icons.Default.FormatPaint
                    com.cogninote.app.ai.SuggestionType.CONTENT -> Icons.Default.AutoAwesome
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor
        )
    )
}

private enum class FormattingAction {
    BOLD, ITALIC, HEADER, BULLET_LIST, NUMBERED_LIST
}

private fun applyFormatting(content: String, action: FormattingAction): String {
    return when (action) {
        FormattingAction.BOLD -> content + "\n**Bold text**"
        FormattingAction.ITALIC -> content + "\n*Italic text*"
        FormattingAction.HEADER -> content + "\n# Header"
        FormattingAction.BULLET_LIST -> content + "\n- List item"
        FormattingAction.NUMBERED_LIST -> content + "\n1. Numbered item"
    }
}
