package com.cogninote.app.utils

import android.text.Html
import java.text.SimpleDateFormat
import java.util.*

object SimplifiedTextUtils {
    
    /**
     * Extracts plain text from HTML content (simplified version)
     */
    fun extractPlainText(htmlContent: String): String {
        return if (htmlContent.contains("<")) {
            Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT)
                .toString()
                .trim()
        } else {
            htmlContent.trim()
        }
    }
    
    /**
     * Simple word count (for basic functionality)
     */
    fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        
        return text.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .size
    }
    
    /**
     * Extracts hashtags from content
     */
    fun extractHashtags(content: String): List<String> {
        val hashtagPattern = Regex("#(\\w+)")
        return hashtagPattern.findAll(content)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
    }
    
    /**
     * Simple date formatting
     */
    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val now = Date()
        
        return when {
            isSameDay(date, now) -> "Today"
            isYesterday(date, now) -> "Yesterday"
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
        }
    }
    
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    private fun isYesterday(date: Date, now: Date): Boolean {
        val cal = Calendar.getInstance().apply { time = now }
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(date, cal.time)
    }
    
    /**
     * Simple text truncation
     */
    fun truncate(text: String, maxLength: Int): String {
        return if (text.length <= maxLength) {
            text
        } else {
            text.take(maxLength - 3) + "..."
        }
    }
}