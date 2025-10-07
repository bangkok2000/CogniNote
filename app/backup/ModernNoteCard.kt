package com.cogninote.app.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cogninote.app.data.entities.Note
import com.cogninote.app.presentation.ui.theme.*
import com.cogninote.app.utils.TextUtils
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernNoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit,
    onExport: (() -> Unit)? = null,
    showArchiveActions: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else if (isHovered) 1.01f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    val animatedElevation by animateFloatAsState(
        targetValue = if (isHovered) 16f else if (note.isPinned) 8f else 4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    val cardBackgroundColor = if (note.isPinned) {
        PrimaryBlue.copy(alpha = 0.05f)
    } else {
        CardBackground
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .padding(vertical = 8.dp, horizontal = 6.dp)
    ) {
        // Clean, simple card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardBackgroundColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isHovered) 8.dp else 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with title and actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Title with gradient text effect
                    Text(
                        text = note.title.ifBlank { "Untitled" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = if (note.isPinned) PrimaryBlue else TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    )
                    
                    // Action buttons with modern styling
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Pinned indicator with pulse animation
                        if (note.isPinned) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                PrimaryBlue.copy(alpha = 0.2f),
                                                PrimaryBlue.copy(alpha = 0.1f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "Pinned",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        // Modern dropdown menu button
                        Box {
                            IconButton(
                                onClick = { showDropdownMenu = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(
                                        Color.Black.copy(alpha = if (isHovered) 0.05f else 0.02f)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            // Modern styled dropdown menu
                            DropdownMenu(
                                expanded = showDropdownMenu,
                                onDismissRequest = { showDropdownMenu = false },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.95f),
                                                Color.White.copy(alpha = 0.9f)
                                            )
                                        )
                                    )
                            ) {
                                if (showArchiveActions) {
                                    ModernDropdownMenuItem(
                                        text = if (note.isPinned) "Unpin" else "Pin",
                                        icon = Icons.Default.PushPin,
                                        iconTint = AccentOrange,
                                        onClick = {
                                            onTogglePin()
                                            showDropdownMenu = false
                                        }
                                    )
                                    
                                    ModernDropdownMenuItem(
                                        text = if (note.isArchived) "Unarchive" else "Archive",
                                        icon = if (note.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                        iconTint = AccentYellow,
                                        onClick = {
                                            onToggleArchive()
                                            showDropdownMenu = false
                                        }
                                    )
                                    
                                    ModernDropdownMenuItem(
                                        text = "Export",
                                        icon = Icons.Default.Share,
                                        iconTint = AccentBlue,
                                        onClick = {
                                            onExport?.invoke()
                                            showDropdownMenu = false
                                        }
                                    )
                                    
                                    ModernDropdownMenuItem(
                                        text = "Delete",
                                        icon = Icons.Default.Delete,
                                        iconTint = AccentRed,
                                        onClick = {
                                            onDelete()
                                            showDropdownMenu = false
                                        }
                                    )
                                } else {
                                    ModernDropdownMenuItem(
                                        text = "Restore",
                                        icon = Icons.Default.Restore,
                                        iconTint = AccentGreen,
                                        onClick = {
                                            onRestore()
                                            showDropdownMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content preview with fade effect
                if (note.plainTextContent.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawWithContent {
                                drawContent()
                                // Fade effect at the bottom
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.8f)
                                        ),
                                        startY = size.height * 0.7f,
                                        endY = size.height
                                    )
                                )
                            }
                    ) {
                        Text(
                            text = TextUtils.extractPreview(note.content),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 22.sp
                            ),
                            color = TextSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Modern tags
                if (note.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        note.tags.take(3).forEach { tag ->
                            ModernTag(tag = tag)
                        }
                        if (note.tags.size > 3) {
                            ModernTag(tag = "+${note.tags.size - 3}", isCounter = true)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Modern footer with stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date with dot indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PulsingDot(
                            color = if (note.isPinned) PrimaryBlue else AccentGreen,
                            size = 6f
                        )
                        Text(
                            text = formatDate(note.updatedAt),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextTertiary
                        )
                    }
                    
                    // Word count with animated counter
                    if (note.wordCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AnimatedCounter(
                                value = note.wordCount,
                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            )
                            Text(
                                text = "words",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernDropdownMenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
            }
        },
        onClick = onClick,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    )
}

@Composable
private fun ModernTag(
    tag: String,
    isCounter: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isCounter) {
        TextTertiary.copy(alpha = 0.2f)
    } else {
        PrimaryBlue.copy(alpha = 0.1f)
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (isCounter) tag else "#$tag",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (isCounter) TextSecondary else PrimaryBlue
        )
    }
}

private fun formatDate(instant: kotlinx.datetime.Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)} ${localDateTime.dayOfMonth}"
}
