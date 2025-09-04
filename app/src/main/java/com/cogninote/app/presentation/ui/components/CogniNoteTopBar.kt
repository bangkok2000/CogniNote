package com.cogninote.app.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cogninote.app.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CogniNoteTopBar(
    currentRoute: String?,
    onNavigationClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSaveNote: (() -> Unit)? = null,
    onDeleteNote: (() -> Unit)? = null,
    onClearSearch: (() -> Unit)? = null,
    isNewNote: Boolean = false,
    hasUnsavedChanges: Boolean = false,
    isLoading: Boolean = false,
    hasSearchQuery: Boolean = false
) {
    val title = when {
        currentRoute?.startsWith("note/") == true -> if (isNewNote) "New Note" else "Edit Note"
        currentRoute?.startsWith("folder/") == true -> "Folder"
        currentRoute == "notes" -> "CogniNote"
        currentRoute == "search" -> "Search"
        currentRoute == "settings" -> "Settings"
        currentRoute == "templates" -> "Templates"
        currentRoute == "folders" -> "Folders"
        else -> "CogniNote"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(PrimaryBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Navigation Icon with modern styling
            ModernIconButton(
                icon = if (currentRoute != "notes" && currentRoute != null) {
                    Icons.Default.ArrowBack
                } else {
                    Icons.Default.Menu
                },
                contentDescription = if (currentRoute != "notes" && currentRoute != null) {
                    "Navigate back"
                } else {
                    "Menu"
                },
                onClick = onNavigationClick
            )
            
            // Title with gradient text effect
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Note editing actions
                if (currentRoute?.startsWith("note/") == true) {
                    // Delete button (only for existing notes)
                    if (!isNewNote && onDeleteNote != null) {
                        ModernIconButton(
                            icon = Icons.Default.Delete,
                            contentDescription = "Delete Note",
                            onClick = onDeleteNote
                        )
                    }
                    
                    // Save button
                    if (onSaveNote != null) {
                        ModernIconButton(
                            icon = if (isLoading) {
                                Icons.Default.Refresh // Will be replaced with loading indicator
                            } else {
                                Icons.Default.Save
                            },
                            contentDescription = "Save Note",
                            onClick = onSaveNote
                        )
                    }
                } else {
                    // Regular actions for other screens
                    if (currentRoute == "notes") {
                        ModernIconButton(
                            icon = Icons.Default.Search,
                            contentDescription = "Search",
                            onClick = onSearchClick
                        )
                    }
                    
                    // Clear search button for search screen
                    if (currentRoute == "search" && hasSearchQuery && onClearSearch != null) {
                        ModernIconButton(
                            icon = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            onClick = onClearSearch
                        )
                    }
                    
                    ModernIconButton(
                        icon = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        onClick = onMenuClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )

    IconButton(
        onClick = {
            isPressed = true
            onClick()
            isPressed = false
        },
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.1f))
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview
@Composable
fun CogniNoteTopBarPreview() {
    MaterialTheme {
        CogniNoteTopBar(
            currentRoute = "notes",
            onNavigationClick = {},
            onSearchClick = {},
            onMenuClick = {}
        )
    }
}
