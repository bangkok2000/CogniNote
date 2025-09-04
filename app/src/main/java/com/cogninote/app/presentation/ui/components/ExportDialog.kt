package com.cogninote.app.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.cogninote.app.services.ExportFormat

@Composable
fun ExportDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onExport: (ExportFormat, Boolean) -> Unit,
    noteCount: Int = 1
) {
    if (isVisible) {
        var selectedFormat by remember { mutableStateOf(ExportFormat.PDF) }
        var includeMetadata by remember { mutableStateOf(true) }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = if (noteCount == 1) "Export Note" else "Export Notes",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    // Format selection
                    Text(
                        text = "Export Format",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    ExportFormat.values().forEach { format ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedFormat == format,
                                    onClick = { selectedFormat = format },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFormat == format,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = format.icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = format.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = format.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Metadata option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = includeMetadata,
                                onClick = { includeMetadata = !includeMetadata },
                                role = Role.Checkbox
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeMetadata,
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Include Metadata",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Creation date, word count, tags, etc.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onExport(selectedFormat, includeMetadata)
                        onDismiss()
                    }
                ) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

private val ExportFormat.icon: ImageVector
    get() = when (this) {
        ExportFormat.PDF -> Icons.Default.PictureAsPdf
        ExportFormat.PLAIN_TEXT -> Icons.Default.TextSnippet
        ExportFormat.MARKDOWN -> Icons.Default.Code
        ExportFormat.HTML -> Icons.Default.Language
        ExportFormat.JSON -> Icons.Default.DataObject
        ExportFormat.EVERNOTE_ENEX -> Icons.Default.Archive
    }

private val ExportFormat.displayName: String
    get() = when (this) {
        ExportFormat.PDF -> "PDF Document"
        ExportFormat.PLAIN_TEXT -> "Plain Text"
        ExportFormat.MARKDOWN -> "Markdown"
        ExportFormat.HTML -> "HTML"
        ExportFormat.JSON -> "JSON"
        ExportFormat.EVERNOTE_ENEX -> "Evernote ENEX"
    }

private val ExportFormat.description: String
    get() = when (this) {
        ExportFormat.PDF -> "Professional document with formatting"
        ExportFormat.PLAIN_TEXT -> "Unformatted text"
        ExportFormat.MARKDOWN -> "Markdown formatted text"
        ExportFormat.HTML -> "Web page format"
        ExportFormat.JSON -> "Structured data format"
        ExportFormat.EVERNOTE_ENEX -> "Evernote export format"
    }
