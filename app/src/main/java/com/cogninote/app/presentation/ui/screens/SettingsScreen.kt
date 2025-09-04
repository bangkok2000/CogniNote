package com.cogninote.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
            // App Info Section
            SettingsSection(title = "App Info") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About CogniNote",
                    subtitle = "Version 1.0.0",
                    onClick = { showAboutDialog = true }
                )
                
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Privacy & Security",
                    subtitle = "Your data stays on your device",
                    onClick = { /* TODO: Show privacy info */ }
                )
            }

            // Data & Storage Section
            SettingsSection(title = "Data & Storage") {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Storage Usage",
                    subtitle = "View app storage details",
                    onClick = { /* TODO: Show storage info */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Backup,
                    title = "Export Data",
                    subtitle = "Export all notes as backup",
                    onClick = { /* TODO: Export functionality */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Restore,
                    title = "Import Data",
                    subtitle = "Import notes from backup",
                    onClick = { /* TODO: Import functionality */ }
                )
            }

            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = "System default",
                    onClick = { /* TODO: Theme settings */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.TextFields,
                    title = "Font Size",
                    subtitle = "Medium",
                    onClick = { /* TODO: Font size settings */ }
                )
            }

            // Editor Section
            SettingsSection(title = "Editor") {
                var autoSave by remember { mutableStateOf(true) }
                SettingsSwitchItem(
                    icon = Icons.Default.Save,
                    title = "Auto-save",
                    subtitle = "Automatically save changes",
                    checked = autoSave,
                    onCheckedChange = { autoSave = it }
                )
                
                var spellCheck by remember { mutableStateOf(true) }
                SettingsSwitchItem(
                    icon = Icons.Default.Spellcheck,
                    title = "Spell Check",
                    subtitle = "Check spelling while typing",
                    checked = spellCheck,
                    onCheckedChange = { spellCheck = it }
                )
            }

            // Security Section
            SettingsSection(title = "Security") {
                SettingsItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometric Lock",
                    subtitle = "Secure app with fingerprint",
                    onClick = { /* TODO: Biometric settings */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Auto-lock",
                    subtitle = "Lock app when inactive",
                    onClick = { /* TODO: Auto-lock settings */ }
                )
            }

            // Help & Support Section
            SettingsSection(title = "Help & Support") {
                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "User Guide",
                    subtitle = "Learn how to use CogniNote",
                    onClick = { /* TODO: Show user guide */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.BugReport,
                    title = "Report Issue",
                    subtitle = "Send feedback or report bugs",
                    onClick = { /* TODO: Feedback functionality */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "Rate App",
                    subtitle = "Rate CogniNote on Play Store",
                    onClick = { /* TODO: Open Play Store */ }
                )
            }
        }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About CogniNote") },
            text = {
                Column {
                    Text("CogniNote v1.0.0")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A privacy-first note-taking app with advanced AI features, all running locally on your device.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Built with ❤️ using Jetpack Compose",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
