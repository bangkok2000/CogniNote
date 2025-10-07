package com.cogninote.app.sharing

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.cogninote.app.data.entities.Note
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareManager @Inject constructor(
    private val context: Context
) {
    private val json = Json { prettyPrint = true }

    fun shareNoteAsText(note: Note): Intent {
        val shareText = buildString {
            appendLine(note.title)
            appendLine("=".repeat(note.title.length))
            appendLine()
            appendLine(note.plainTextContent)
            appendLine()
            if (note.tags.isNotEmpty()) {
                appendLine("Tags: ${note.tags.joinToString(", ")}")
            }
        }

        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, note.title)
        }
    }

    fun exportNoteAsPdf(note: Note): Intent {
        val pdfFile = createPdfFile(listOf(note), "note_${note.id}_${System.currentTimeMillis()}.pdf")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "${note.title}.pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun shareNoteAsHtml(note: Note): Intent {
        val htmlContent = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html>")
            appendLine("<head>")
            appendLine("  <meta charset=\"UTF-8\">")
            appendLine("  <title>${note.title}</title>")
            appendLine("  <style>")
            appendLine("    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }")
            appendLine("    .tags { color: #666; font-size: 0.9em; margin-top: 20px; }")
            appendLine("  </style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("  <h1>${note.title}</h1>")
            appendLine("  <div>")
            appendLine(note.content)
            appendLine("  </div>")
            if (note.tags.isNotEmpty()) {
                appendLine("  <div class=\"tags\">Tags: ${note.tags.joinToString(", ")}</div>")
            }
            appendLine("</body>")
            appendLine("</html>")
        }

        val htmlFile = createTempFile("note_${note.id}", ".html", htmlContent)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            htmlFile
        )

        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/html"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, note.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun shareNoteAsMarkdown(note: Note): Intent {
        val markdownContent = buildString {
            appendLine("# ${note.title}")
            appendLine()

            // Convert HTML to markdown (basic conversion)
            val markdownText = note.content
                .replace("<strong>", "**")
                .replace("</strong>", "**")
                .replace("<em>", "*")
                .replace("</em>", "*")
                .replace("<h1>", "# ")
                .replace("</h1>", "")
                .replace("<h2>", "## ")
                .replace("</h2>", "")
                .replace("<h3>", "### ")
                .replace("</h3>", "")
                .replace("<ul>", "")
                .replace("</ul>", "")
                .replace("<li>", "- ")
                .replace("</li>", "")
                .replace("<p>", "")
                .replace("</p>", "\n")
                .replace("<br>", "\n")
                .replace("&nbsp;", " ")

            appendLine(markdownText)
            appendLine()

            if (note.tags.isNotEmpty()) {
                appendLine("---")
                appendLine("**Tags:** ${note.tags.joinToString(", ")}")
            }
        }

        val mdFile = createTempFile("note_${note.id}", ".md", markdownContent)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            mdFile
        )

        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/markdown"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, note.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun exportNoteAsJson(note: Note): Intent {
        val exportData = ExportNote(
            id = note.id,
            title = note.title,
            content = note.content,
            plainTextContent = note.plainTextContent,
            tags = note.tags,
            isPinned = note.isPinned,
            isArchived = note.isArchived,
            createdAt = note.createdAt.toString(),
            updatedAt = note.updatedAt.toString(),
            reminderAt = note.reminderAt?.toString()
        )

        val jsonContent = json.encodeToString(exportData)
        val jsonFile = createTempFile("note_${note.id}", ".json", jsonContent)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            jsonFile
        )

        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "${note.title}.json")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun exportMultipleNotes(notes: List<Note>, format: ExportFormat): Intent {
        return when (format) {
            ExportFormat.JSON -> exportNotesAsJson(notes)
            ExportFormat.MARKDOWN -> exportNotesAsMarkdown(notes)
            ExportFormat.HTML -> exportNotesAsHtml(notes)
            ExportFormat.TEXT -> exportNotesAsText(notes)
            ExportFormat.PDF -> exportNotesAsPdf(notes)
        }
    }

    private fun exportNotesAsJson(notes: List<Note>): Intent {
        val exportData = NotesExport(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            notes = notes.map { note ->
                ExportNote(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    plainTextContent = note.plainTextContent,
                    tags = note.tags,
                    isPinned = note.isPinned,
                    isArchived = note.isArchived,
                    createdAt = note.createdAt.toString(),
                    updatedAt = note.updatedAt.toString(),
                    reminderAt = note.reminderAt?.toString()
                )
            }
        )

        val jsonContent = json.encodeToString(exportData)
        val jsonFile = createTempFile("cogninote_export", ".json", jsonContent)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            jsonFile
        )

        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "CogniNote Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun exportNotesAsMarkdown(notes: List<Note>): Intent {
        val markdownContent = buildString {
            appendLine("# CogniNote Export")
            appendLine()
            appendLine("Exported on: ${java.util.Date()}")
            appendLine("Total notes: ${notes.size}")
            appendLine()
            appendLine("---")
            appendLine()

            notes.forEach { note ->
                appendLine("## ${note.title}")
                appendLine()
                appendLine(note.plainTextContent)
                appendLine()
                if (note.tags.isNotEmpty()) {
                    appendLine("**Tags:** ${note.tags.joinToString(", ")}")
                    appendLine()
                }
                appendLine("---")
                appendLine()
            }
        }

        val mdFile = createTempFile("cogninote_export", ".md", markdownContent)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            mdFile
        )

        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/markdown"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "CogniNote Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun exportNotesAsHtml(notes: List<Note>): Intent {
        val htmlContent = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html>")
            appendLine("<head>")
            appendLine("  <meta charset=\"UTF-8\">")
            appendLine("  <title>CogniNote Export</title>")
            appendLine("  <style>")
            appendLine("    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; margin: 40px; }")
            appendLine("    .note { margin-bottom: 40px; padding: 20px; border-left: 4px solid #007AFF; }")
            appendLine("    .tags { color: #666; font-size: 0.9em; margin-top: 20px; }")
            appendLine("    .export-info { color: #666; font-size: 0.9em; margin-bottom: 40px; }")
            appendLine("  </style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("  <h1>CogniNote Export</h1>")
            appendLine("  <div class=\"export-info\">")
            appendLine("    Exported on: ${java.util.Date()}<br>")
            appendLine("    Total notes: ${notes.size}")
            appendLine("  </div>")

            notes.forEach { note ->
                appendLine("  <div class=\"note\">")
                appendLine("    <h2>${note.title}</h2>")
                appendLine("    <div>")
                appendLine(note.content)
                appendLine("    </div>")
                if (note.tags.isNotEmpty()) {
                    appendLine("    <div class=\"tags\">Tags: ${note.tags.joinToString(", ")}</div>")
                }
                appendLine("  </div>")
            }

            appendLine("</body>")
            appendLine("</html>")
        }

        val htmlFile = createTempFile("cogninote_export", ".html", htmlContent)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            htmlFile
        )

        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/html"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "CogniNote Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun exportNotesAsText(notes: List<Note>): Intent {
        val textContent = buildString {
            appendLine("CogniNote Export")
            appendLine("================")
            appendLine()
            appendLine("Exported on: ${java.util.Date()}")
            appendLine("Total notes: ${notes.size}")
            appendLine()
            appendLine("---")
            appendLine()

            notes.forEach { note ->
                appendLine(note.title)
                appendLine("=".repeat(note.title.length))
                appendLine()
                appendLine(note.plainTextContent)
                appendLine()
                if (note.tags.isNotEmpty()) {
                    appendLine("Tags: ${note.tags.joinToString(", ")}")
                    appendLine()
                }
                appendLine("---")
                appendLine()
            }
        }

        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textContent)
            putExtra(Intent.EXTRA_SUBJECT, "CogniNote Export")
        }
    }

    private fun exportNotesAsPdf(notes: List<Note>): Intent {
        val pdfFile = createPdfFile(notes, "cogninote_export_${System.currentTimeMillis()}.pdf")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "CogniNote Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun createPdfFile(notes: List<Note>, fileName: String): File {
        val tempDir = File(context.cacheDir, "shared_files")
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }

        val file = File(tempDir, fileName)
        val writer = PdfWriter(file)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        document.add(Paragraph("CogniNote Export"))
        document.add(Paragraph("Exported on: ${java.util.Date()}"))
        document.add(Paragraph("Total notes: ${notes.size}"))

        notes.forEach { note ->
            document.add(Paragraph(note.title))
            document.add(Paragraph(note.plainTextContent))
            if (note.tags.isNotEmpty()) {
                document.add(Paragraph("Tags: ${note.tags.joinToString(", ")}"))
            }
        }

        document.close()
        return file
    }

    private fun createTempFile(name: String, extension: String, content: String): File {
        val tempDir = File(context.cacheDir, "shared_files")
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }

        val file = File(tempDir, "$name$extension")
        file.writeText(content)
        return file
    }
}

@kotlinx.serialization.Serializable
data class ExportNote(
    val id: String,
    val title: String,
    val content: String,
    val plainTextContent: String,
    val tags: List<String>,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val reminderAt: String?
)

@kotlinx.serialization.Serializable
data class NotesExport(
    val version: Int,
    val exportedAt: Long,
    val notes: List<ExportNote>
)

enum class ExportFormat {
    JSON, MARKDOWN, HTML, TEXT, PDF
}