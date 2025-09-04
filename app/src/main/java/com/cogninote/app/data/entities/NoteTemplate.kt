package com.cogninote.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "note_templates")
@TypeConverters(Converters::class)
data class NoteTemplate(
    @PrimaryKey
    val id: String = generateTemplateId(),
    val name: String,
    val description: String = "",
    val category: TemplateCategory,
    val content: String,
    val placeholders: List<String> = emptyList(), // Variables like {{date}}, {{title}}
    val tags: List<String> = emptyList(),
    val isBuiltIn: Boolean = false,
    val isPublic: Boolean = false,
    val usageCount: Int = 0,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val previewImage: String? = null
) {
    companion object {
        fun generateTemplateId(): String {
            return "template_${Clock.System.now().toEpochMilliseconds()}_${(1000..9999).random()}"
        }
    }
}

@Serializable
enum class TemplateCategory {
    MEETING,
    PROJECT,
    DAILY_JOURNAL,
    RESEARCH,
    BRAINSTORMING,
    TASK_LIST,
    BOOK_REVIEW,
    TRAVEL,
    RECIPE,
    CUSTOM
}

// Built-in templates
object BuiltInTemplates {
    val DAILY_JOURNAL = NoteTemplate(
        id = "builtin_daily_journal",
        name = "Daily Journal",
        description = "Simple daily reflection template",
        category = TemplateCategory.DAILY_JOURNAL,
        content = """
# Daily Journal - {{date}}

## How I'm feeling today
{{mood}}

## What happened today
- 
- 
- 

## What I learned
{{learnings}}

## Tomorrow's priorities
1. 
2. 
3. 

## Gratitude
I'm grateful for:
- 
- 
- 
        """.trimIndent(),
        placeholders = listOf("{{date}}", "{{mood}}", "{{learnings}}"),
        tags = listOf("journal", "daily", "reflection"),
        isBuiltIn = true
    )
    
    val MEETING_NOTES = NoteTemplate(
        id = "builtin_meeting_notes",
        name = "Meeting Notes",
        description = "Professional meeting template with action items",
        category = TemplateCategory.MEETING,
        content = """
# Meeting: {{meeting_title}}
**Date:** {{date}}
**Time:** {{time}}
**Attendees:** {{attendees}}

## Agenda
1. 
2. 
3. 

## Discussion Points
### Topic 1
- 

### Topic 2
- 

## Decisions Made
- 
- 

## Action Items
- [ ] {{action_1}} - Due: {{due_date_1}} - Assigned: {{assignee_1}}
- [ ] {{action_2}} - Due: {{due_date_2}} - Assigned: {{assignee_2}}

## Next Steps
- 

## Follow-up Meeting
Date: {{follow_up_date}}
        """.trimIndent(),
        placeholders = listOf("{{meeting_title}}", "{{date}}", "{{time}}", "{{attendees}}", "{{action_1}}", "{{due_date_1}}", "{{assignee_1}}", "{{action_2}}", "{{due_date_2}}", "{{assignee_2}}", "{{follow_up_date}}"),
        tags = listOf("meeting", "work", "action-items"),
        isBuiltIn = true
    )
    
    val PROJECT_PLANNING = NoteTemplate(
        id = "builtin_project_planning",
        name = "Project Planning",
        description = "Comprehensive project planning template",
        category = TemplateCategory.PROJECT,
        content = """
# Project: {{project_name}}

## Overview
**Start Date:** {{start_date}}
**End Date:** {{end_date}}
**Status:** {{status}}
**Priority:** {{priority}}

## Objectives
### Primary Goal
{{primary_goal}}

### Success Metrics
- 
- 
- 

## Scope
### In Scope
- 
- 

### Out of Scope
- 
- 

## Timeline & Milestones
| Milestone | Due Date | Status |
|-----------|----------|--------|
| {{milestone_1}} | {{date_1}} | {{status_1}} |
| {{milestone_2}} | {{date_2}} | {{status_2}} |

## Team & Responsibilities
- **Project Manager:** {{pm_name}}
- **Developer:** {{dev_name}}
- **Designer:** {{designer_name}}

## Resources Required
### Budget
{{budget}}

### Tools & Technology
- 
- 

## Risk Assessment
| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| {{risk_1}} | {{impact_1}} | {{prob_1}} | {{mitigation_1}} |

## Notes & Updates
{{project_notes}}
        """.trimIndent(),
        placeholders = listOf("{{project_name}}", "{{start_date}}", "{{end_date}}", "{{status}}", "{{priority}}", "{{primary_goal}}", "{{milestone_1}}", "{{date_1}}", "{{status_1}}", "{{milestone_2}}", "{{date_2}}", "{{status_2}}", "{{pm_name}}", "{{dev_name}}", "{{designer_name}}", "{{budget}}", "{{risk_1}}", "{{impact_1}}", "{{prob_1}}", "{{mitigation_1}}", "{{project_notes}}"),
        tags = listOf("project", "planning", "work"),
        isBuiltIn = true
    )
    
    val RESEARCH_NOTES = NoteTemplate(
        id = "builtin_research_notes",
        name = "Research Notes",
        description = "Academic and professional research template",
        category = TemplateCategory.RESEARCH,
        content = """
# Research: {{research_topic}}

## Research Question
{{research_question}}

## Hypothesis
{{hypothesis}}

## Sources
### Primary Sources
1. {{source_1}} - {{author_1}} ({{year_1}})
2. {{source_2}} - {{author_2}} ({{year_2}})

### Secondary Sources
1. 
2. 

## Key Findings
### Finding 1
**Source:** {{finding_source_1}}
**Summary:** {{finding_summary_1}}
**Relevance:** {{finding_relevance_1}}

### Finding 2
**Source:** {{finding_source_2}}
**Summary:** {{finding_summary_2}}
**Relevance:** {{finding_relevance_2}}

## Methodology
{{methodology}}

## Analysis
{{analysis}}

## Conclusions
{{conclusions}}

## Further Research
- 
- 
- 

## References
1. 
2. 
3. 
        """.trimIndent(),
        placeholders = listOf("{{research_topic}}", "{{research_question}}", "{{hypothesis}}", "{{source_1}}", "{{author_1}}", "{{year_1}}", "{{source_2}}", "{{author_2}}", "{{year_2}}", "{{finding_source_1}}", "{{finding_summary_1}}", "{{finding_relevance_1}}", "{{finding_source_2}}", "{{finding_summary_2}}", "{{finding_relevance_2}}", "{{methodology}}", "{{analysis}}", "{{conclusions}}"),
        tags = listOf("research", "academic", "study"),
        isBuiltIn = true
    )
    
    val BOOK_REVIEW = NoteTemplate(
        id = "builtin_book_review",
        name = "Book Review",
        description = "Comprehensive book review and note-taking template",
        category = TemplateCategory.BOOK_REVIEW,
        content = """
# Book Review: {{book_title}}

## Book Details
**Author:** {{author}}
**Publication Year:** {{year}}
**Pages:** {{pages}}
**Genre:** {{genre}}
**Rating:** {{rating}}/5 ⭐

## Summary
{{summary}}

## Key Themes
1. {{theme_1}}
2. {{theme_2}}
3. {{theme_3}}

## Important Quotes
> "{{quote_1}}"
> — Page {{page_1}}

> "{{quote_2}}"
> — Page {{page_2}}

## Key Takeaways
- {{takeaway_1}}
- {{takeaway_2}}
- {{takeaway_3}}

## Personal Reflection
### What I Liked
{{liked}}

### What I Didn't Like
{{disliked}}

### How This Applies to My Life
{{application}}

## Connections
### Similar Books
- {{similar_book_1}}
- {{similar_book_2}}

### Related Concepts
- {{concept_1}}
- {{concept_2}}

## Action Items
- [ ] {{action_1}}
- [ ] {{action_2}}

## Overall Thoughts
{{overall_thoughts}}

## Recommend to Others?
{{recommendation}}
        """.trimIndent(),
        placeholders = listOf("{{book_title}}", "{{author}}", "{{year}}", "{{pages}}", "{{genre}}", "{{rating}}", "{{summary}}", "{{theme_1}}", "{{theme_2}}", "{{theme_3}}", "{{quote_1}}", "{{page_1}}", "{{quote_2}}", "{{page_2}}", "{{takeaway_1}}", "{{takeaway_2}}", "{{takeaway_3}}", "{{liked}}", "{{disliked}}", "{{application}}", "{{similar_book_1}}", "{{similar_book_2}}", "{{concept_1}}", "{{concept_2}}", "{{action_1}}", "{{action_2}}", "{{overall_thoughts}}", "{{recommendation}}"),
        tags = listOf("book", "review", "reading"),
        isBuiltIn = true
    )
    
    fun getAllBuiltInTemplates(): List<NoteTemplate> {
        return listOf(
            DAILY_JOURNAL,
            MEETING_NOTES,
            PROJECT_PLANNING,
            RESEARCH_NOTES,
            BOOK_REVIEW
        )
    }
}
