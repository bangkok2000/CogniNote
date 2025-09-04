package com.cogninote.app.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun OverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onImportNotes: () -> Unit,
    onExportNotes: () -> Unit,
    onBackupData: () -> Unit,
    onRestoreData: () -> Unit,
    onShowStatistics: () -> Unit,
    onShowHelp: () -> Unit,
    onShowAbout: () -> Unit,
    currentRoute: String? = null
) {
    val context = LocalContext.current
    
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(200.dp)
    ) {
        when (currentRoute) {
            "notes" -> {
                // Notes screen specific actions
                MenuGroup(title = "Notes Actions") {
                    OverflowMenuItem(
                        icon = Icons.Default.FileUpload,
                        text = "Import Notes",
                        onClick = {
                            onImportNotes()
                            onDismiss()
                        }
                    )
                    OverflowMenuItem(
                        icon = Icons.Default.FileDownload,
                        text = "Export Notes",
                        onClick = {
                            onExportNotes()
                            onDismiss()
                        }
                    )
                }
            }
            
            "search" -> {
                // Search screen specific actions
                MenuGroup(title = "Search Options") {
                    OverflowMenuItem(
                        icon = Icons.Default.FilterList,
                        text = "Advanced Filters",
                        onClick = {
                            // TODO: Implement advanced filters
                            onDismiss()
                        }
                    )
                    OverflowMenuItem(
                        icon = Icons.Default.History,
                        text = "Search History",
                        onClick = {
                            // TODO: Implement search history
                            onDismiss()
                        }
                    )
                }
            }
            
            "settings" -> {
                // Settings screen specific actions
                MenuGroup(title = "Data Management") {
                    OverflowMenuItem(
                        icon = Icons.Default.Backup,
                        text = "Backup Data",
                        onClick = {
                            onBackupData()
                            onDismiss()
                        }
                    )
                    OverflowMenuItem(
                        icon = Icons.Default.Restore,
                        text = "Restore Data",
                        onClick = {
                            onRestoreData()
                            onDismiss()
                        }
                    )
                }
            }
        }

        // Common actions for all screens
        if (currentRoute != null) {
            HorizontalDivider()
        }
        
        MenuGroup(title = "App") {
            OverflowMenuItem(
                icon = Icons.Default.Analytics,
                text = "Statistics",
                onClick = {
                    onShowStatistics()
                    onDismiss()
                }
            )
            OverflowMenuItem(
                icon = Icons.Default.Help,
                text = "Help & Tips",
                onClick = {
                    onShowHelp()
                    onDismiss()
                }
            )
            OverflowMenuItem(
                icon = Icons.Default.Info,
                text = "About",
                onClick = {
                    onShowAbout()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun MenuGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun OverflowMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(20.dp),
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }
        },
        onClick = onClick,
        enabled = enabled
    )
}

// Dialog states for different actions
@Composable
fun rememberOverflowMenuState(): OverflowMenuState {
    return remember { OverflowMenuState() }
}

class OverflowMenuState {
    var showImportDialog by mutableStateOf(false)
    var showExportDialog by mutableStateOf(false)
    var showBackupDialog by mutableStateOf(false)
    var showRestoreDialog by mutableStateOf(false)
    var showStatisticsDialog by mutableStateOf(false)
    var showHelpDialog by mutableStateOf(false)
    var showAboutDialog by mutableStateOf(false)
    
    fun hideAllDialogs() {
        showImportDialog = false
        showExportDialog = false
        showBackupDialog = false
        showRestoreDialog = false
        showStatisticsDialog = false
        showHelpDialog = false
        showAboutDialog = false
    }
}
