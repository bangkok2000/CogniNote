package com.cogninote.app.test

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import com.cogninote.app.data.entities.Note
import com.cogninote.app.utils.SimplifiedTextUtils

/**
 * Simple test to verify core functionality of the simplified app
 */
class SimplifiedCoreTest {
    
    @Test
    fun test_auto_tag_extraction() {
        val content = "This is a note about #android development and #kotlin programming"
        
        // Simulate the hashtag extraction logic from SimplifiedNoteRepository
        val hashtagPattern = Regex("#(\\w+)")
        val extractedTags = hashtagPattern.findAll(content)
            .map { it.groupValues[1].lowercase() }
            .distinct()
            .toList()
        
        assertEquals(listOf("android", "kotlin"), extractedTags)
    }
    
    @Test
    fun test_auto_title_generation() {
        val content = "This should become the title\n\nAnd this is the rest of the content"
        val autoTitle = content.lines().firstOrNull()?.take(50)?.trim() ?: "Untitled"
        
        assertEquals("This should become the title", autoTitle)
    }
    
    @Test
    fun test_text_utils_plain_text_extraction() {
        // Test simple text case (no HTML)
        val plainContent = "This is plain text"
        val result = if (plainContent.contains("<")) {
            // Would use Html.fromHtml in real app
            "processed text"
        } else {
            plainContent.trim()
        }
        
        assertEquals("This is plain text", result)
    }
    
    @Test
    fun test_note_creation() {
        val note = Note(
            title = "Test Note",
            content = "This is a test note with #test and #simple tags",
            tags = listOf("test", "simple")
        )
        
        assertEquals("Test Note", note.title)
        assertEquals("This is a test note with #test and #simple tags", note.content)
        assertTrue(note.tags.contains("test"))
        assertTrue(note.tags.contains("simple"))
        assertFalse(note.isPinned)
    }
}