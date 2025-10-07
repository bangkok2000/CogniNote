package com.cogninote.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Composable that renders text with hashtags as clickable chips
 */
@Composable
fun HashtagText(
    text: String,
    modifier: Modifier = Modifier,
    onHashtagClick: ((String) -> Unit)? = null,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    val hashtagPattern = remember { Regex("#\\w+") }
    val matches = remember(text) { hashtagPattern.findAll(text).toList() }
    
    if (matches.isEmpty()) {
        // No hashtags, render normal text
        Text(
            text = text,
            modifier = modifier,
            style = style
        )
    } else {
        // Text with hashtags - create annotated string
        val annotatedString = buildAnnotatedString {
            var lastIndex = 0
            
            for (match in matches) {
                // Add text before hashtag
                if (match.range.first > lastIndex) {
                    append(text.substring(lastIndex, match.range.first))
                }
                
                // Add hashtag with special styling
                pushStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )
                )
                pushStringAnnotation(
                    tag = "hashtag",
                    annotation = match.value.substring(1) // Remove # prefix
                )
                append(match.value)
                pop()
                pop()
                
                lastIndex = match.range.last + 1
            }
            
            // Add remaining text
            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
        
        ClickableText(
            text = annotatedString,
            modifier = modifier,
            style = style,
            onClick = { offset ->
                annotatedString.getStringAnnotations(
                    tag = "hashtag",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let { annotation ->
                    onHashtagClick?.invoke(annotation.item)
                }
            }
        )
    }
}

/**
 * Composable that displays hashtags as separate chips below text content
 */
@Composable
fun HashtagChips(
    tags: List<String>,
    modifier: Modifier = Modifier,
    onTagClick: ((String) -> Unit)? = null
) {
    if (tags.isNotEmpty()) {
        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(tags.size) { index ->
                val tag = tags[index]
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTagClick?.invoke(tag) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = "#$tag",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Enhanced text field that shows hashtag suggestions
 */
@Composable
fun HashtagInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Start typing...",
    availableHashtags: List<String> = emptyList(),
    onHashtagSelected: ((String) -> Unit)? = null,
    minLines: Int = 1,
    singleLine: Boolean = false
) {
    var showSuggestions by remember { mutableStateOf(false) }
    var cursorPosition by remember { mutableStateOf(0) }
    
    // Check if cursor is after a # to show suggestions
    LaunchedEffect(value, cursorPosition) {
        val textBeforeCursor = value.take(cursorPosition)
        val lastHashIndex = textBeforeCursor.lastIndexOf('#')
        showSuggestions = lastHashIndex != -1 && 
                        (lastHashIndex == textBeforeCursor.length - 1 || 
                         textBeforeCursor.substring(lastHashIndex + 1).all { it.isLetterOrDigit() })
    }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                // Update cursor position (simplified)
                cursorPosition = newValue.length
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            singleLine = singleLine
        )
        
        // Show hashtag suggestions
        if (showSuggestions && availableHashtags.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Suggested hashtags:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        items(availableHashtags.take(5).size) { index ->
                            val tag = availableHashtags.take(5)[index]
                            FilterChip(
                                onClick = {
                                    onHashtagSelected?.invoke(tag)
                                    showSuggestions = false
                                },
                                label = { Text("#$tag") },
                                selected = false
                            )
                        }
                    }
                }
            }
        }
    }
}