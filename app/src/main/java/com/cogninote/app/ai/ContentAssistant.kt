package com.cogninote.app.ai

import android.content.Context
import com.cogninote.app.data.entities.Note
import com.cogninote.app.data.entities.Task
import com.cogninote.app.data.entities.TaskExtractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentAssistant @Inject constructor(
    private val context: Context
) {
    
    private val summaryService = SummaryService()
    private val keywordExtractor = KeywordExtractor()
    private val contentAnalyzer = ContentAnalyzer()
    
    /**
     * Generate an intelligent summary of note content
     */
    fun generateSummary(content: String, maxLength: Int = 200): String {
        return summaryService.summarize(content, maxLength)
    }
    
    /**
     * Extract key topics and concepts from note content
     */
    fun extractKeywords(content: String, maxKeywords: Int = 10): List<String> {
        return keywordExtractor.extract(content, maxKeywords)
    }
    
    /**
     * Suggest tags based on note content
     */
    fun suggestTags(content: String, existingTags: List<String> = emptyList()): List<String> {
        val keywords = extractKeywords(content, 15)
        val contentCategories = analyzeContentCategory(content)
        
        val suggestions = mutableSetOf<String>()
        
        // Add category-based tags
        suggestions.addAll(contentCategories)
        
        // Add keyword-based tags
        suggestions.addAll(keywords.take(8))
        
        // Filter out existing tags
        return suggestions.filter { it !in existingTags }.take(6)
    }
    
    /**
     * Extract action items and tasks from note content
     */
    fun extractTasks(noteId: String, content: String): List<Task> {
        return TaskExtractor.extractTasksFromContent(noteId, content)
    }
    
    /**
     * Analyze content sentiment and tone
     */
    fun analyzeSentiment(content: String): ContentSentiment {
        return contentAnalyzer.analyzeSentiment(content)
    }
    
    /**
     * Suggest content improvements
     */
    fun suggestImprovements(content: String): List<ContentSuggestion> {
        val suggestions = mutableListOf<ContentSuggestion>()
        
        // Check content length
        if (content.length < 50) {
            suggestions.add(
                ContentSuggestion(
                    type = SuggestionType.STRUCTURE,
                    message = "Consider adding more details to make your note more comprehensive",
                    priority = SuggestionPriority.LOW
                )
            )
        }
        
        // Check for headers
        if (content.length > 200 && !content.contains(Regex("^#+ ", RegexOption.MULTILINE))) {
            suggestions.add(
                ContentSuggestion(
                    type = SuggestionType.STRUCTURE,
                    message = "Add headers to better organize your content",
                    priority = SuggestionPriority.MEDIUM
                )
            )
        }
        
        // Check for action items
        val tasks = extractTasks("temp", content)
        if (tasks.isNotEmpty()) {
            suggestions.add(
                ContentSuggestion(
                    type = SuggestionType.TASK,
                    message = "Found ${tasks.size} potential action items. Convert them to tasks?",
                    priority = SuggestionPriority.HIGH
                )
            )
        }
        
        // Check for dates without reminders
        val dateMatches = Regex("\\d{4}-\\d{2}-\\d{2}").findAll(content)
        if (dateMatches.count() > 0) {
            suggestions.add(
                ContentSuggestion(
                    type = SuggestionType.REMINDER,
                    message = "Found dates in your note. Would you like to set reminders?",
                    priority = SuggestionPriority.MEDIUM
                )
            )
        }
        
        return suggestions
    }
    
    /**
     * Auto-complete text based on context
     */
    fun autoComplete(partialText: String, context: String = ""): List<String> {
        val completions = mutableListOf<String>()
        
        // Simple completion patterns
        when {
            partialText.endsWith("TODO") -> {
                completions.addAll(listOf(
                    "TODO: Review and update",
                    "TODO: Follow up with",
                    "TODO: Research",
                    "TODO: Complete by"
                ))
            }
            partialText.endsWith("Note") -> {
                completions.addAll(listOf(
                    "Note: Important to remember",
                    "Note: Follow up required",
                    "Note: Key insight",
                    "Note: Action needed"
                ))
            }
            partialText.contains("meeting", ignoreCase = true) -> {
                completions.addAll(listOf(
                    "meeting agenda",
                    "meeting notes",
                    "meeting action items",
                    "meeting follow-up"
                ))
            }
        }
        
        return completions.take(4)
    }
    
    /**
     * Analyze content category
     */
    private fun analyzeContentCategory(content: String): List<String> {
        val categories = mutableListOf<String>()
        val lowerContent = content.lowercase()
        
        when {
            lowerContent.contains(Regex("meeting|agenda|attendees|minutes")) -> categories.add("meeting")
            lowerContent.contains(Regex("project|milestone|deadline|deliverable")) -> categories.add("project")
            lowerContent.contains(Regex("research|study|analysis|findings")) -> categories.add("research")
            lowerContent.contains(Regex("idea|brainstorm|concept|innovation")) -> categories.add("brainstorming")
            lowerContent.contains(Regex("todo|task|action|complete")) -> categories.add("tasks")
            lowerContent.contains(Regex("journal|diary|reflection|thoughts")) -> categories.add("personal")
            lowerContent.contains(Regex("recipe|cooking|ingredients|instructions")) -> categories.add("recipe")
            lowerContent.contains(Regex("travel|trip|vacation|itinerary")) -> categories.add("travel")
            lowerContent.contains(Regex("book|read|author|chapter")) -> categories.add("reading")
        }
        
        return categories.ifEmpty { listOf("general") }
    }
}

/**
 * Simple extractive summarization service
 */
class SummaryService {
    fun summarize(content: String, maxLength: Int): String {
        if (content.length <= maxLength) return content
        
        val sentences = content.split(Regex("[.!?]+")).filter { it.trim().isNotEmpty() }
        if (sentences.isEmpty()) return content.take(maxLength)
        
        // Score sentences by length and position (simple heuristic)
        val scoredSentences = sentences.mapIndexed { index, sentence ->
            val positionScore = if (index < sentences.size / 3) 2.0 else 1.0
            val lengthScore = sentence.length.toDouble() / 100
            sentence to (positionScore + lengthScore)
        }.sortedByDescending { it.second }
        
        // Select top sentences that fit within maxLength
        val selectedSentences = mutableListOf<String>()
        var currentLength = 0
        
        for ((sentence, _) in scoredSentences) {
            if (currentLength + sentence.length + 2 <= maxLength) {
                selectedSentences.add(sentence.trim())
                currentLength += sentence.length + 2
            }
        }
        
        return if (selectedSentences.isNotEmpty()) {
            selectedSentences.joinToString(". ") + "."
        } else {
            content.take(maxLength - 3) + "..."
        }
    }
}

/**
 * Keyword extraction service
 */
class KeywordExtractor {
    private val commonWords = setOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i", "it", "for", "not", "on", "with", "he", "as", "you", "do", "at", "this", "but", "his", "by", "from", "they", "we", "say", "her", "she", "or", "an", "will", "my", "one", "all", "would", "there", "their"
    )
    
    fun extract(content: String, maxKeywords: Int): List<String> {
        // Simple frequency-based extraction
        val words = content.lowercase()
            .replace(Regex("[^a-zA-Z\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 3 && it !in commonWords }
        
        val wordFreq = words.groupingBy { it }.eachCount()
        
        return wordFreq.entries
            .sortedByDescending { it.value }
            .take(maxKeywords)
            .map { it.key }
    }
}

/**
 * Content analysis service
 */
class ContentAnalyzer {
    fun analyzeSentiment(content: String): ContentSentiment {
        val positiveWords = setOf("good", "great", "excellent", "amazing", "wonderful", "fantastic", "awesome", "brilliant", "perfect", "outstanding", "successful", "happy", "excited", "optimistic")
        val negativeWords = setOf("bad", "terrible", "awful", "horrible", "disappointing", "frustrated", "angry", "sad", "worried", "concerned", "problem", "issue", "difficult", "challenging")
        
        val lowerContent = content.lowercase()
        val words = lowerContent.split(Regex("\\W+"))
        
        val positiveCount = words.count { it in positiveWords }
        val negativeCount = words.count { it in negativeWords }
        
        val score = when {
            positiveCount > negativeCount -> (positiveCount - negativeCount).toDouble() / words.size
            negativeCount > positiveCount -> -(negativeCount - positiveCount).toDouble() / words.size
            else -> 0.0
        }
        
        return ContentSentiment(
            score = score,
            confidence = minOf(0.8, (positiveCount + negativeCount).toDouble() / words.size * 10)
        )
    }
}

data class ContentSentiment(
    val score: Double, // -1.0 to 1.0
    val confidence: Double // 0.0 to 1.0
)

data class ContentSuggestion(
    val type: SuggestionType,
    val message: String,
    val priority: SuggestionPriority,
    val actionable: Boolean = true
)

enum class SuggestionType {
    STRUCTURE,
    TASK,
    REMINDER,
    TAG,
    FORMATTING,
    CONTENT
}

enum class SuggestionPriority {
    LOW,
    MEDIUM,
    HIGH
}
