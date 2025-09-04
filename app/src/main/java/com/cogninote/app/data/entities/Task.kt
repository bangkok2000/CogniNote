package com.cogninote.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Entity(tableName = "tasks")
@TypeConverters(Converters::class)
data class Task(
    @PrimaryKey
    val id: String = generateTaskId(),
    val noteId: String, // Associated note
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM", // Simplified to String
    val dueDate: Instant? = null,
    val reminderAt: Instant? = null,
    val completedAt: Instant? = null,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val tags: List<String> = emptyList()
) {
    companion object {
        fun generateTaskId(): String {
            return "task_${Clock.System.now().toEpochMilliseconds()}_${(1000..9999).random()}"
        }
    }
}

@Serializable
data class Subtask(
    val id: String = generateSubtaskId(),
    val title: String,
    val isCompleted: Boolean = false,
    val completedAt: Instant? = null
) {
    companion object {
        fun generateSubtaskId(): String {
            return "subtask_${Clock.System.now().toEpochMilliseconds()}_${(100..999).random()}"
        }
    }
}

@Serializable
enum class TaskPriority(val value: Int, val displayName: String) {
    LOW(1, "Low"),
    MEDIUM(2, "Medium"),
    HIGH(3, "High"),
    URGENT(4, "Urgent")
}

@Serializable
data class RecurrenceRule(
    val frequency: RecurrenceFrequency,
    val interval: Int = 1, // Every N frequency units
    val endDate: Instant? = null,
    val maxOccurrences: Int? = null
)

@Serializable
enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

// Task extraction from note content
object TaskExtractor {
    private val taskPatterns = listOf(
        Regex("^\\s*[-*+]\\s*\\[\\s*\\]\\s*(.+)$", RegexOption.MULTILINE), // - [ ] Task
        Regex("^\\s*\\d+\\.\\s*\\[\\s*\\]\\s*(.+)$", RegexOption.MULTILINE), // 1. [ ] Task
        Regex("^\\s*TODO:?\\s*(.+)$", RegexOption.MULTILINE), // TODO: Task
        Regex("^\\s*TASK:?\\s*(.+)$", RegexOption.MULTILINE), // TASK: Task
        Regex("^\\s*Action:?\\s*(.+)$", RegexOption.MULTILINE), // Action: Task
    )

    private val dueDatePatterns = listOf(
        Regex("due:?\\s*(\\d{4}-\\d{2}-\\d{2})", RegexOption.IGNORE_CASE),
        Regex("by\\s+(\\d{4}-\\d{2}-\\d{2})", RegexOption.IGNORE_CASE),
        Regex("deadline:?\\s*(\\d{4}-\\d{2}-\\d{2})", RegexOption.IGNORE_CASE)
    )

    private val priorityPatterns = listOf(
        Regex("priority:?\\s*(low|medium|high|urgent)", RegexOption.IGNORE_CASE),
        Regex("\\b(low|medium|high|urgent)\\s*priority", RegexOption.IGNORE_CASE),
        Regex("!!!(.*)", RegexOption.IGNORE_CASE), // !!! for urgent
        Regex("!!(.*)", RegexOption.IGNORE_CASE),  // !! for high
        Regex("!(.*)", RegexOption.IGNORE_CASE)    // ! for medium
    )

    fun extractTasksFromContent(noteId: String, content: String): List<Task> {
        val tasks = mutableListOf<Task>()
        
        taskPatterns.forEach { pattern ->
            pattern.findAll(content).forEach { match ->
                val taskText = match.groupValues[1].trim()
                if (taskText.isNotEmpty()) {
                    val task = createTaskFromText(noteId, taskText)
                    tasks.add(task)
                }
            }
        }
        
        return tasks
    }

    private fun createTaskFromText(noteId: String, taskText: String): Task {
        var cleanText = taskText
        var dueDate: Instant? = null
        var priority = "MEDIUM"

        // Extract due date
        dueDatePatterns.forEach { pattern ->
            pattern.find(cleanText)?.let { match ->
                // Parse date and set dueDate
                cleanText = cleanText.replace(match.value, "").trim()
            }
        }

        // Extract priority
        priorityPatterns.forEach { pattern ->
            pattern.find(cleanText)?.let { match ->
                val priorityText = match.groupValues[1].lowercase()
                priority = when (priorityText) {
                    "low" -> "LOW"
                    "medium" -> "MEDIUM"
                    "high" -> "HIGH"
                    "urgent" -> "URGENT"
                    else -> "MEDIUM"
                }
                cleanText = cleanText.replace(match.value, "").trim()
            }
        }

        return Task(
            noteId = noteId,
            title = cleanText,
            priority = priority,
            dueDate = dueDate
        )
    }
}
