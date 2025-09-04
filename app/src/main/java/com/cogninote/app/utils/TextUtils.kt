package com.cogninote.app.utils

import android.text.Html
import kotlin.math.ceil

object TextUtils {
    
    private const val AVERAGE_READING_SPEED_WPM = 200 // words per minute
    
    /**
     * Extracts plain text from HTML content
     */
    fun extractPlainText(htmlContent: String): String {
        return Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT)
            .toString()
            .trim()
    }
    
    /**
     * Counts words in the given text
     */
    fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        
        return text.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .size
    }
    
    /**
     * Calculates estimated reading time in minutes
     */
    fun calculateReadingTime(wordCount: Int): Int {
        if (wordCount == 0) return 0
        return ceil(wordCount.toDouble() / AVERAGE_READING_SPEED_WPM).toInt()
    }
    
    /**
     * Extracts note links from content (e.g., [[Note Title]] or [[note-id]])
     */
    fun extractNoteLinks(content: String): List<String> {
        val linkPattern = Regex("""\[\[([^\]]+)\]\]""")
        return linkPattern.findAll(content)
            .map { it.groupValues[1].trim() }
            .toList()
    }
    
    /**
     * Extracts hashtags from content
     */
    fun extractHashtags(content: String): List<String> {
        val hashtagPattern = Regex("""#(\w+)""")
        return hashtagPattern.findAll(content)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
    }
    
    /**
     * Highlights search query in text
     */
    fun highlightSearchQuery(text: String, query: String): String {
        if (query.isBlank()) return text
        
        return text.replace(
            Regex(Regex.escape(query), RegexOption.IGNORE_CASE),
            "<mark>$query</mark>"
        )
    }
    
    /**
     * Truncates text to a specified length
     */
    fun truncateText(text: String, maxLength: Int, ellipsis: String = "..."): String {
        return if (text.length <= maxLength) {
            text
        } else {
            text.take(maxLength - ellipsis.length) + ellipsis
        }
    }
    
    /**
     * Extracts preview text from content (first few sentences)
     */
    fun extractPreview(content: String, maxLength: Int = 150): String {
        val plainText = extractPlainText(content)
        return truncateText(plainText, maxLength)
    }
    
    /**
     * Validates note title
     */
    fun isValidTitle(title: String): Boolean {
        return title.isNotBlank() && title.length <= 200
    }
    
    /**
     * Sanitizes file name for safe storage
     */
    fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .trim()
            .take(255)
    }
    
    /**
     * Generates a search-friendly version of text
     */
    fun generateSearchableText(text: String): String {
        return extractPlainText(text)
            .lowercase()
            .replace(Regex("[^\\w\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    /**
     * Checks if text contains search query
     */
    fun containsQuery(text: String, query: String): Boolean {
        if (query.isBlank()) return true
        return generateSearchableText(text).contains(query.lowercase())
    }
    
    /**
     * Formats file size in human readable format
     */
    fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "%.1f %s".format(size, units[unitIndex])
    }
}
