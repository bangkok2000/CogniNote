package com.cogninote.app.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    onNavigateToNotes: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onCreateFolder: () -> Unit,
    onShowStatistics: () -> Unit,
    recentTags: List<String> = emptyList(),
    folders: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.fillMaxHeight()
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Icon(
                    imageVector = Icons.Default.Note,
                    contentDescription = "CogniNote",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "CogniNote",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Smart Note Taking",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Main Navigation
            item {
                DrawerSection(title = "Navigation") {
                    DrawerItem(
                        icon = Icons.Default.Home,
                        title = "All Notes",
                        onClick = onNavigateToNotes
                    )
                    DrawerItem(
                        icon = Icons.Default.Search,
                        title = "Search",
                        onClick = onNavigateToSearch
                    )
                    DrawerItem(
                        icon = Icons.Default.Description,
                        title = "Templates",
                        onClick = onNavigateToTemplates
                    )
                    DrawerItem(
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        onClick = onNavigateToSettings
                    )
                }
            }

            // Folders Section
            item {
                DrawerSection(
                    title = "Folders",
                    actionIcon = Icons.Default.Add,
                    onActionClick = onCreateFolder
                ) {
                    if (folders.isEmpty()) {
                        Text(
                            text = "No folders yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    } else {
                        folders.forEach { folder ->
                            DrawerItem(
                                icon = Icons.Default.Folder,
                                title = folder,
                                onClick = { /* Navigate to folder */ }
                            )
                        }
                    }
                }
            }

            // Recent Tags Section
            item {
                DrawerSection(title = "Recent Tags") {
                    if (recentTags.isEmpty()) {
                        Text(
                            text = "No tags yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    } else {
                        recentTags.take(5).forEach { tag ->
                            DrawerItem(
                                icon = Icons.Default.Tag,
                                title = "#$tag",
                                onClick = { /* Navigate to tag */ }
                            )
                        }
                    }
                }
            }

            // Statistics
            item {
                DrawerSection(title = "Insights") {
                    DrawerItem(
                        icon = Icons.Default.Analytics,
                        title = "Statistics",
                        onClick = onShowStatistics
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerSection(
    title: String,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (actionIcon != null && onActionClick != null) {
                IconButton(
                    onClick = onActionClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = "Add $title",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        
        content()
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(20.dp)
            )
        },
        label = { Text(title) },
        selected = false,
        onClick = onClick,
        modifier = modifier.padding(horizontal = 12.dp, vertical = 2.dp)
    )
}
