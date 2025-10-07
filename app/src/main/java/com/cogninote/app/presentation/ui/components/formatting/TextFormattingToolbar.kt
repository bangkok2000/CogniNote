package com.cogninote.app.presentation.ui.components.formatting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Comprehensive text formatting toolbar with all formatting options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFormattingToolbar(
    isVisible: Boolean,
    onFormatClick: (TextFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Text Style Row
                Text(
                    text = "Text Style",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Basic formatting
                    FormatButton(
                        format = TextFormat.BOLD,
                        onClick = { onFormatClick(TextFormat.BOLD) }
                    )
                    FormatButton(
                        format = TextFormat.ITALIC,
                        onClick = { onFormatClick(TextFormat.ITALIC) }
                    )
                    FormatButton(
                        format = TextFormat.UNDERLINE,
                        onClick = { onFormatClick(TextFormat.UNDERLINE) }
                    )
                    FormatButton(
                        format = TextFormat.STRIKETHROUGH,
                        onClick = { onFormatClick(TextFormat.STRIKETHROUGH) }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Code formatting
                    FormatButton(
                        format = TextFormat.CODE_INLINE,
                        onClick = { onFormatClick(TextFormat.CODE_INLINE) }
                    )
                    FormatButton(
                        format = TextFormat.CODE_BLOCK,
                        onClick = { onFormatClick(TextFormat.CODE_BLOCK) }
                    )
                }
                
                // Headings Row
                Text(
                    text = "Headings",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FormatButton(
                        format = TextFormat.HEADING_1,
                        onClick = { onFormatClick(TextFormat.HEADING_1) }
                    )
                    FormatButton(
                        format = TextFormat.HEADING_2,
                        onClick = { onFormatClick(TextFormat.HEADING_2) }
                    )
                    FormatButton(
                        format = TextFormat.HEADING_3,
                        onClick = { onFormatClick(TextFormat.HEADING_3) }
                    )
                }
                
                // Lists and Blocks Row
                Text(
                    text = "Lists & Blocks",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FormatButton(
                        format = TextFormat.BULLET_LIST,
                        onClick = { onFormatClick(TextFormat.BULLET_LIST) }
                    )
                    FormatButton(
                        format = TextFormat.NUMBERED_LIST,
                        onClick = { onFormatClick(TextFormat.NUMBERED_LIST) }
                    )
                    FormatButton(
                        format = TextFormat.CHECKBOX,
                        onClick = { onFormatClick(TextFormat.CHECKBOX) }
                    )
                    FormatButton(
                        format = TextFormat.QUOTE,
                        onClick = { onFormatClick(TextFormat.QUOTE) }
                    )
                    FormatButton(
                        format = TextFormat.LINK,
                        onClick = { onFormatClick(TextFormat.LINK) }
                    )
                }
            }
        }
    }
}

/**
 * Individual format button component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormatButton(
    format: TextFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Card(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = format.icon,
                contentDescription = format.displayName,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Compact single-row formatting toolbar for mobile
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactFormattingToolbar(
    isVisible: Boolean,
    onFormatClick: (TextFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Most commonly used formatting options
                val commonFormats = listOf(
                    TextFormat.BOLD,
                    TextFormat.ITALIC,
                    TextFormat.UNDERLINE,
                    TextFormat.STRIKETHROUGH,
                    TextFormat.CODE_INLINE,
                    TextFormat.BULLET_LIST,
                    TextFormat.NUMBERED_LIST,
                    TextFormat.QUOTE,
                    TextFormat.LINK
                )
                
                commonFormats.forEach { format ->
                    FormatButton(
                        format = format,
                        onClick = { onFormatClick(format) },
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

/**
 * Keyboard shortcuts helper component
 */
@Composable
fun KeyboardShortcutsHelper(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Keyboard Shortcuts",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                val shortcuts = TextFormat.values().filter { it.shortcutKey != null }
                
                shortcuts.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEach { format ->
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = format.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = format.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = format.shortcutKey ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}