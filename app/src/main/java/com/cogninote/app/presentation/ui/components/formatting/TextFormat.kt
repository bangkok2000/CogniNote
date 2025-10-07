package com.cogninote.app.presentation.ui.components.formatting

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Enum representing different text formatting options
 */
enum class TextFormat(
    val displayName: String,
    val icon: ImageVector,
    val markdownSyntax: String,
    val shortcutKey: String? = null
) {
    BOLD(
        displayName = "Bold",
        icon = Icons.Default.FormatBold,
        markdownSyntax = "**",
        shortcutKey = "Ctrl+B"
    ),
    ITALIC(
        displayName = "Italic", 
        icon = Icons.Default.FormatItalic,
        markdownSyntax = "*",
        shortcutKey = "Ctrl+I"
    ),
    UNDERLINE(
        displayName = "Underline",
        icon = Icons.Default.FormatUnderlined,
        markdownSyntax = "__",
        shortcutKey = "Ctrl+U"
    ),
    STRIKETHROUGH(
        displayName = "Strikethrough",
        icon = Icons.Default.FormatStrikethrough,
        markdownSyntax = "~~",
        shortcutKey = "Ctrl+Shift+X"
    ),
    CODE_INLINE(
        displayName = "Inline Code",
        icon = Icons.Default.Code,
        markdownSyntax = "`",
        shortcutKey = "Ctrl+E"
    ),
    CODE_BLOCK(
        displayName = "Code Block",
        icon = Icons.Default.DataObject,
        markdownSyntax = "```",
        shortcutKey = "Ctrl+Shift+E"
    ),
    QUOTE(
        displayName = "Quote",
        icon = Icons.Default.FormatQuote,
        markdownSyntax = "> ",
        shortcutKey = "Ctrl+Shift+Q"
    ),
    BULLET_LIST(
        displayName = "Bullet List",
        icon = Icons.Default.FormatListBulleted,
        markdownSyntax = "- ",
        shortcutKey = "Ctrl+Shift+L"
    ),
    NUMBERED_LIST(
        displayName = "Numbered List",
        icon = Icons.Default.FormatListNumbered,
        markdownSyntax = "1. ",
        shortcutKey = "Ctrl+Shift+N"
    ),
    CHECKBOX(
        displayName = "Checkbox",
        icon = Icons.Default.CheckBox,
        markdownSyntax = "- [ ] ",
        shortcutKey = "Ctrl+Shift+C"
    ),
    HEADING_1(
        displayName = "Heading 1",
        icon = Icons.Default.Title,
        markdownSyntax = "# ",
        shortcutKey = "Ctrl+1"
    ),
    HEADING_2(
        displayName = "Heading 2", 
        icon = Icons.Default.Title,
        markdownSyntax = "## ",
        shortcutKey = "Ctrl+2"
    ),
    HEADING_3(
        displayName = "Heading 3",
        icon = Icons.Default.Title,
        markdownSyntax = "### ",
        shortcutKey = "Ctrl+3"
    ),
    LINK(
        displayName = "Link",
        icon = Icons.Default.Link,
        markdownSyntax = "[text](url)",
        shortcutKey = "Ctrl+K"
    )
}

/**
 * Data class representing text formatting operation
 */
data class TextFormatOperation(
    val format: TextFormat,
    val selectionStart: Int,
    val selectionEnd: Int,
    val selectedText: String = ""
)

/**
 * Utility class for text formatting operations
 */
object TextFormatUtils {
    
    /**
     * Apply formatting to selected text or insert formatting at cursor
     */
    fun applyFormatting(
        currentText: String,
        cursorPosition: Int,
        selectionStart: Int,
        selectionEnd: Int,
        format: TextFormat
    ): FormatResult {
        val hasSelection = selectionStart != selectionEnd
        val selectedText = if (hasSelection) {
            currentText.substring(selectionStart, selectionEnd)
        } else {
            ""
        }
        
        return when (format) {
            TextFormat.BOLD, TextFormat.ITALIC, TextFormat.UNDERLINE, 
            TextFormat.STRIKETHROUGH, TextFormat.CODE_INLINE -> {
                applyWrapFormatting(currentText, selectionStart, selectionEnd, selectedText, format)
            }
            
            TextFormat.CODE_BLOCK -> {
                applyBlockFormatting(currentText, selectionStart, selectionEnd, selectedText, format)
            }
            
            TextFormat.QUOTE, TextFormat.BULLET_LIST, TextFormat.NUMBERED_LIST,
            TextFormat.CHECKBOX, TextFormat.HEADING_1, TextFormat.HEADING_2, TextFormat.HEADING_3 -> {
                applyLineFormatting(currentText, selectionStart, selectionEnd, format)
            }
            
            TextFormat.LINK -> {
                applyLinkFormatting(currentText, selectionStart, selectionEnd, selectedText)
            }
        }
    }
    
    private fun applyWrapFormatting(
        text: String,
        selectionStart: Int,
        selectionEnd: Int,
        selectedText: String,
        format: TextFormat
    ): FormatResult {
        val syntax = format.markdownSyntax
        
        if (selectedText.isEmpty()) {
            // Insert formatting markers at cursor
            val newText = text.substring(0, selectionStart) + 
                         syntax + syntax + 
                         text.substring(selectionStart)
            return FormatResult(
                newText = newText,
                newCursorPosition = selectionStart + syntax.length
            )
        } else {
            // Wrap selected text
            val newText = text.substring(0, selectionStart) + 
                         syntax + selectedText + syntax +
                         text.substring(selectionEnd)
            return FormatResult(
                newText = newText,
                newCursorPosition = selectionEnd + (syntax.length * 2)
            )
        }
    }
    
    private fun applyBlockFormatting(
        text: String,
        selectionStart: Int,
        selectionEnd: Int,
        selectedText: String,
        format: TextFormat
    ): FormatResult {
        val syntax = format.markdownSyntax
        val lineBreak = if (text.isNotEmpty() && !text.substring(0, selectionStart).endsWith("\n")) "\n" else ""
        
        val formattedText = if (selectedText.isEmpty()) {
            "$lineBreak$syntax\n\n$syntax"
        } else {
            "$lineBreak$syntax\n$selectedText\n$syntax"
        }
        
        val newText = text.substring(0, selectionStart) + 
                     formattedText +
                     text.substring(selectionEnd)
        
        return FormatResult(
            newText = newText,
            newCursorPosition = selectionStart + lineBreak.length + syntax.length + 1
        )
    }
    
    private fun applyLineFormatting(
        text: String,
        selectionStart: Int,
        selectionEnd: Int,
        format: TextFormat
    ): FormatResult {
        val syntax = format.markdownSyntax
        
        // Find the beginning of the current line
        val lineStart = text.lastIndexOf('\n', selectionStart - 1) + 1
        val lineEnd = text.indexOf('\n', selectionEnd).let { if (it == -1) text.length else it }
        val currentLine = text.substring(lineStart, lineEnd)
        
        // Check if line already has this formatting
        val alreadyFormatted = currentLine.trimStart().startsWith(syntax.trim())
        
        val newLine = if (alreadyFormatted) {
            // Remove formatting
            currentLine.replaceFirst(syntax, "")
        } else {
            // Add formatting
            syntax + currentLine
        }
        
        val newText = text.substring(0, lineStart) + 
                     newLine +
                     text.substring(lineEnd)
        
        return FormatResult(
            newText = newText,
            newCursorPosition = if (alreadyFormatted) {
                selectionStart - syntax.length
            } else {
                selectionStart + syntax.length
            }
        )
    }
    
    private fun applyLinkFormatting(
        text: String,
        selectionStart: Int,
        selectionEnd: Int,
        selectedText: String
    ): FormatResult {
        val linkText = selectedText.ifEmpty { "link text" }
        val linkSyntax = "[$linkText](url)"
        
        val newText = text.substring(0, selectionStart) + 
                     linkSyntax +
                     text.substring(selectionEnd)
        
        return FormatResult(
            newText = newText,
            newCursorPosition = selectionStart + linkSyntax.indexOf("url")
        )
    }
}

/**
 * Result of a text formatting operation
 */
data class FormatResult(
    val newText: String,
    val newCursorPosition: Int
)