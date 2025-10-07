package com.cogninote.app.services

import android.content.Context
import android.net.Uri
import com.cogninote.app.data.entities.Note
import com.cogninote.app.data.entities.NoteTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportImportService @Inject constructor(
    private val context: Context
) {
    
    /**
     * Export notes to various formats
     */
    suspend fun exportNotes(
        notes: List<Note>,
        format: ExportFormat,
        includeMetadata: Boolean = true
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
            
            when (format) {
                ExportFormat.JSON -> exportToJson(notes, timestamp, includeMetadata)
                ExportFormat.MARKDOWN -> exportToMarkdown(notes, timestamp, includeMetadata)
                ExportFormat.PLAIN_TEXT -> exportToPlainText(notes, timestamp, includeMetadata)
                ExportFormat.HTML -> exportToHtml(notes, timestamp, includeMetadata)
                ExportFormat.PDF -> exportToPdf(notes, timestamp, includeMetadata)
                ExportFormat.EVERNOTE_ENEX -> exportToEvernote(notes, timestamp)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Import notes from various formats
     */
    suspend fun importNotes(
        uri: Uri,
        format: ImportFormat
    ): Result<List<Note>> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open file"))
            
            val content = inputStream.bufferedReader().use { it.readText() }
            
            when (format) {
                ImportFormat.JSON -> importFromJson(content)
                ImportFormat.MARKDOWN -> importFromMarkdown(content)
                ImportFormat.PLAIN_TEXT -> importFromPlainText(content)
                ImportFormat.EVERNOTE_ENEX -> importFromEvernote(content)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Export templates
     */
    suspend fun exportTemplates(
        templates: List<NoteTemplate>,
        format: ExportFormat = ExportFormat.JSON
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
            
            when (format) {
                ExportFormat.JSON -> exportTemplatesToJson(templates, timestamp)
                else -> Result.failure(Exception("Templates can only be exported as JSON"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun exportToJson(
        notes: List<Note>,
        timestamp: String,
        includeMetadata: Boolean
    ): Result<Uri> {
        val jsonArray = JSONArray()
        
        notes.forEach { note ->
            val noteJson = JSONObject().apply {
                put("title", note.title)
                put("content", note.content)
                put("tags", JSONArray(note.tags))
                
                if (includeMetadata) {
                    put("createdAt", note.createdAt.toString())
                    put("updatedAt", note.updatedAt.toString())
                    put("isPinned", note.isPinned)
                    put("isArchived", note.isArchived)
                    put("wordCount", note.wordCount)
                }
            }
            jsonArray.put(noteJson)
        }
        
        val exportData = JSONObject().apply {
            put("version", "1.0")
            put("exportDate", Clock.System.now().toString())
            put("app", "CogniNote")
            put("notes", jsonArray)
        }
        
        return saveToFile(exportData.toString(), "cogninote_export_$timestamp.json")
    }
    
    private fun exportToMarkdown(
        notes: List<Note>,
        timestamp: String,
        includeMetadata: Boolean
    ): Result<Uri> {
        val markdown = buildString {
            appendLine("# CogniNote Export")
            appendLine("Exported on: ${Clock.System.now()}")
            appendLine("Total notes: ${notes.size}")
            appendLine()
            
            notes.forEach { note ->
                appendLine("## ${note.title}")
                appendLine()
                
                if (includeMetadata && note.tags.isNotEmpty()) {
                    appendLine("**Tags:** ${note.tags.joinToString(", ")}")
                    appendLine()
                }
                
                if (includeMetadata) {
                    val localDateTime = note.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    appendLine("**Created:** $localDateTime")
                    appendLine()
                }
                
                appendLine(note.content)
                appendLine()
                appendLine("---")
                appendLine()
            }
        }
        
        return saveToFile(markdown, "cogninote_export_$timestamp.md")
    }
    
    private fun exportToPlainText(
        notes: List<Note>,
        timestamp: String,
        includeMetadata: Boolean
    ): Result<Uri> {
        val text = buildString {
            appendLine("CogniNote Export")
            appendLine("Exported on: ${Clock.System.now()}")
            appendLine("Total notes: ${notes.size}")
            appendLine("=" * 50)
            appendLine()
            
            notes.forEach { note ->
                appendLine(note.title.uppercase())
                appendLine("-" * note.title.length)
                appendLine()
                
                if (includeMetadata && note.tags.isNotEmpty()) {
                    appendLine("Tags: ${note.tags.joinToString(", ")}")
                    appendLine()
                }
                
                if (includeMetadata) {
                    val localDateTime = note.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    appendLine("Created: $localDateTime")
                    appendLine()
                }
                
                appendLine(note.plainTextContent)
                appendLine()
                appendLine("=" * 50)
                appendLine()
            }
        }
        
        return saveToFile(text, "cogninote_export_$timestamp.txt")
    }
    
    private fun exportToHtml(
        notes: List<Note>,
        timestamp: String,
        includeMetadata: Boolean
    ): Result<Uri> {
        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html>")
            appendLine("<head>")
            appendLine("<meta charset='UTF-8'>")
            appendLine("<title>CogniNote Export</title>")
            appendLine("<style>")
            appendLine("""
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
                h1 { color: #2563EB; border-bottom: 2px solid #2563EB; padding-bottom: 10px; }
                h2 { color: #1F2937; margin-top: 30px; }
                .metadata { background: #F1F5F9; padding: 10px; border-radius: 5px; margin: 10px 0; }
                .content { line-height: 1.6; margin: 20px 0; }
                .separator { border-top: 1px solid #E5E7EB; margin: 30px 0; }
            """.trimIndent())
            appendLine("</style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("<h1>CogniNote Export</h1>")
            appendLine("<p>Exported on: ${Clock.System.now()}</p>")
            appendLine("<p>Total notes: ${notes.size}</p>")
            
            notes.forEach { note ->
                appendLine("<div class='separator'></div>")
                appendLine("<h2>${note.title}</h2>")
                
                if (includeMetadata) {
                    appendLine("<div class='metadata'>")
                    if (note.tags.isNotEmpty()) {
                        appendLine("<p><strong>Tags:</strong> ${note.tags.joinToString(", ")}</p>")
                    }
                    val localDateTime = note.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    appendLine("<p><strong>Created:</strong> $localDateTime</p>")
                    appendLine("</div>")
                }
                
                appendLine("<div class='content'>")
                appendLine(note.content.replace("\n", "<br>"))
                appendLine("</div>")
            }
            
            appendLine("</body>")
            appendLine("</html>")
        }
        
        return saveToFile(html, "cogninote_export_$timestamp.html")
    }
    
    private fun exportToEvernote(
        notes: List<Note>,
        timestamp: String
    ): Result<Uri> {
        val enex = buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<en-export export-date=\"${Clock.System.now()}\" application=\"CogniNote\">")
            
            notes.forEach { note ->
                appendLine("<note>")
                appendLine("<title><![CDATA[${note.title}]]></title>")
                appendLine("<content><![CDATA[")
                appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                appendLine("<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">")
                appendLine("<en-note>")
                appendLine(note.content.replace("\n", "<br/>"))
                appendLine("</en-note>")
                appendLine("]]></content>")
                
                note.tags.forEach { tag ->
                    appendLine("<tag><![CDATA[$tag]]></tag>")
                }
                
                appendLine("<created>${note.createdAt}</created>")
                appendLine("<updated>${note.updatedAt}</updated>")
                appendLine("</note>")
            }
            
            appendLine("</en-export>")
        }
        
        return saveToFile(enex, "cogninote_export_$timestamp.enex")
    }
    
    private fun exportTemplatesToJson(
        templates: List<NoteTemplate>,
        timestamp: String
    ): Result<Uri> {
        val jsonArray = JSONArray()
        
        templates.forEach { template ->
            val templateJson = JSONObject().apply {
                put("name", template.name)
                put("description", template.description)
                put("category", template.category.name)
                put("content", template.content)
                put("tags", JSONArray(template.tags))
                put("placeholders", JSONArray(template.placeholders))
                put("isBuiltIn", template.isBuiltIn)
            }
            jsonArray.put(templateJson)
        }
        
        val exportData = JSONObject().apply {
            put("version", "1.0")
            put("exportDate", Clock.System.now().toString())
            put("app", "CogniNote")
            put("type", "templates")
            put("templates", jsonArray)
        }
        
        return saveToFile(exportData.toString(), "cogninote_templates_$timestamp.json")
    }
    
    private fun importFromJson(content: String): Result<List<Note>> {
        return try {
            val jsonObject = JSONObject(content)
            val notesArray = jsonObject.getJSONArray("notes")
            val notes = mutableListOf<Note>()
            
            for (i in 0 until notesArray.length()) {
                val noteJson = notesArray.getJSONObject(i)
                val tags = mutableListOf<String>()
                
                if (noteJson.has("tags")) {
                    val tagsArray = noteJson.getJSONArray("tags")
                    for (j in 0 until tagsArray.length()) {
                        tags.add(tagsArray.getString(j))
                    }
                }
                
                val note = Note(
                    title = noteJson.getString("title"),
                    content = noteJson.getString("content"),
                    plainTextContent = noteJson.getString("content"),
                    tags = tags,
                    createdAt = if (noteJson.has("createdAt")) {
                        kotlinx.datetime.Instant.parse(noteJson.getString("createdAt"))
                    } else Clock.System.now(),
                    updatedAt = Clock.System.now()
                )
                notes.add(note)
            }
            
            Result.success(notes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun importFromMarkdown(content: String): Result<List<Note>> {
        return try {
            val notes = mutableListOf<Note>()
            val sections = content.split(Regex("^## ", RegexOption.MULTILINE))
            
            sections.drop(1).forEach { section ->
                val lines = section.trim().split("\n")
                if (lines.isNotEmpty()) {
                    val title = lines[0].trim()
                    val contentLines = lines.drop(1).joinToString("\n").trim()
                    
                    if (contentLines.isNotEmpty()) {
                        notes.add(Note(
                            title = title,
                            content = contentLines,
                            plainTextContent = contentLines
                        ))
                    }
                }
            }
            
            Result.success(notes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun importFromPlainText(content: String): Result<List<Note>> {
        return try {
            val notes = mutableListOf<Note>()
            val sections = content.split(Regex("^=+$", RegexOption.MULTILINE))
            
            sections.forEach { section ->
                val lines = section.trim().split("\n")
                if (lines.isNotEmpty()) {
                    val title = lines[0].trim()
                    val contentLines = lines.drop(1).joinToString("\n").trim()
                    
                    if (contentLines.isNotEmpty()) {
                        notes.add(Note(
                            title = title,
                            content = contentLines,
                            plainTextContent = contentLines
                        ))
                    }
                }
            }
            
            Result.success(notes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun importFromEvernote(content: String): Result<List<Note>> {
        // Simplified Evernote import - would need proper XML parsing in production
        return try {
            val notes = mutableListOf<Note>()
            // Basic implementation - would need XML parser for full support
            notes.add(Note(
                title = "Imported from Evernote",
                content = "Evernote import functionality would be implemented here",
                plainTextContent = "Evernote import functionality would be implemented here"
            ))
            Result.success(notes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun exportToPdf(
        notes: List<Note>,
        timestamp: String,
        includeMetadata: Boolean
    ): Result<Uri> {
        return try {
            // For now, create a simple text file with PDF extension
            // This can be enhanced later with proper PDF generation
            val content = buildString {
                appendLine("CogniNote Export")
                appendLine("================")
                appendLine()
                appendLine("Exported on: ${Clock.System.now()}")
                appendLine("Total notes: ${notes.size}")
                appendLine()
                appendLine("---")
                appendLine()
                
                notes.forEach { note ->
                    appendLine(note.title)
                    appendLine("=".repeat(note.title.length))
                    appendLine()
                    appendLine(note.content.ifEmpty { note.plainTextContent })
                    appendLine()
                    if (note.tags.isNotEmpty()) {
                        appendLine("Tags: ${note.tags.joinToString(", ")}")
                        appendLine()
                    }
                    if (includeMetadata) {
                        appendLine("Created: ${note.createdAt}")
                        appendLine("Updated: ${note.updatedAt}")
                        appendLine("Words: ${note.wordCount}")
                        if (note.isPinned) appendLine("📌 Pinned")
                        if (note.isArchived) appendLine("📁 Archived")
                        appendLine()
                    }
                    appendLine("---")
                    appendLine()
                }
            }
            
            saveToFile(content, "cogninote_export_$timestamp.pdf")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun saveToFile(content: String, filename: String): Result<Uri> {
        return try {
            val file = File(context.getExternalFilesDir(null), filename)
            FileOutputStream(file).use { it.write(content.toByteArray()) }
            Result.success(Uri.fromFile(file))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

enum class ExportFormat {
    JSON, MARKDOWN, PLAIN_TEXT, HTML, PDF, EVERNOTE_ENEX
}

enum class ImportFormat {
    JSON, MARKDOWN, PLAIN_TEXT, EVERNOTE_ENEX
}

private operator fun String.times(n: Int): String = this.repeat(n)
