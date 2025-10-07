package com.cogninote.app.data.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

// Simplified note templates - just 3 essential types
enum class SimpleNoteType(val displayName: String, val template: String) {
    QUICK_NOTE(
        displayName = "Quick Note", 
        template = ""
    ),
    DAILY_ENTRY(
        displayName = "Daily Entry",
        template = "# Today\n\n## What happened today?\n\n\n## Key thoughts\n\n\n## Tomorrow's focus\n\n"
    ),
    MEETING_NOTES(
        displayName = "Meeting Notes",
        template = "# Meeting: \n\n**Date:** Today\n**Participants:** \n\n## Agenda\n- \n\n## Notes\n\n\n## Action Items\n- [ ] \n- [ ] \n\n"
    )
}

// Simple container for template content
data class NoteFromTemplate(
    val type: SimpleNoteType,
    val content: String = type.template,
    val suggestedTags: List<String> = when (type) {
        SimpleNoteType.DAILY_ENTRY -> listOf("daily", "journal")
        SimpleNoteType.MEETING_NOTES -> listOf("meeting", "work")  
        SimpleNoteType.QUICK_NOTE -> emptyList()
    }
)