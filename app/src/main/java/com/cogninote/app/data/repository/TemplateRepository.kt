package com.cogninote.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import com.cogninote.app.data.dao.TemplateDao
import com.cogninote.app.data.entities.NoteTemplate
import com.cogninote.app.data.entities.TemplateCategory
import com.cogninote.app.data.entities.BuiltInTemplates
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepository @Inject constructor(
    private val templateDao: TemplateDao
) {

    fun getAllTemplates(): Flow<List<NoteTemplate>> = templateDao.getAllTemplates()

    fun getTemplatesByCategory(category: TemplateCategory): Flow<List<NoteTemplate>> = 
        templateDao.getTemplatesByCategory(category)

    fun getBuiltInTemplates(): Flow<List<NoteTemplate>> = templateDao.getBuiltInTemplates()

    fun getCustomTemplates(): Flow<List<NoteTemplate>> = templateDao.getCustomTemplates()

    fun getPublicTemplates(): Flow<List<NoteTemplate>> = templateDao.getPublicTemplates()

    fun getTemplateById(id: String): Flow<NoteTemplate?> = templateDao.getTemplateByIdFlow(id)

    fun searchTemplates(query: String): Flow<List<NoteTemplate>> = templateDao.searchTemplates(query)

    fun getAllCategories(): Flow<List<TemplateCategory>> = templateDao.getAllCategories()

    fun getCustomTemplateCount(): Flow<Int> = templateDao.getCustomTemplateCount()

    fun getMostUsedTemplates(limit: Int = 5): Flow<List<NoteTemplate>> = 
        templateDao.getMostUsedTemplates(limit)

    suspend fun createTemplate(
        name: String,
        description: String,
        category: TemplateCategory,
        content: String,
        placeholders: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        isPublic: Boolean = false
    ): NoteTemplate {
        val template = NoteTemplate(
            name = name,
            description = description,
            category = category,
            content = content,
            placeholders = placeholders,
            tags = tags,
            isBuiltIn = false,
            isPublic = isPublic,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        templateDao.insertTemplate(template)
        return template
    }

    suspend fun updateTemplate(template: NoteTemplate) {
        val updatedTemplate = template.copy(updatedAt = Clock.System.now())
        templateDao.updateTemplate(updatedTemplate)
    }

    suspend fun deleteTemplate(templateId: String) {
        templateDao.deleteTemplateById(templateId)
    }

    suspend fun duplicateTemplate(templateId: String): NoteTemplate? {
        val originalTemplate = templateDao.getTemplateById(templateId) ?: return null
        
        val duplicatedTemplate = originalTemplate.copy(
            id = NoteTemplate.generateTemplateId(),
            name = "${originalTemplate.name} (Copy)",
            isBuiltIn = false,
            isPublic = false,
            usageCount = 0,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        
        templateDao.insertTemplate(duplicatedTemplate)
        return duplicatedTemplate
    }

    suspend fun useTemplate(templateId: String): NoteTemplate? {
        val template = templateDao.getTemplateById(templateId) ?: return null
        templateDao.incrementUsageCount(templateId)
        return template
    }

    suspend fun createNoteFromTemplate(templateId: String, placeholderValues: Map<String, String> = emptyMap()): String {
        val template = templateDao.getTemplateById(templateId) ?: return ""
        templateDao.incrementUsageCount(templateId)
        
        var processedContent = template.content
        
        // Replace placeholders with provided values or default values
        template.placeholders.forEach { placeholder ->
            val value = placeholderValues[placeholder] ?: when (placeholder) {
                "{{date}}" -> getCurrentDate()
                "{{time}}" -> getCurrentTime()
                "{{datetime}}" -> getCurrentDateTime()
                else -> "" // Leave empty for manual input
            }
            processedContent = processedContent.replace(placeholder, value)
        }
        
        return processedContent
    }

    suspend fun initializeBuiltInTemplates() {
        val existingBuiltIns = templateDao.getAllBuiltInTemplatesSync()
        if (existingBuiltIns.isEmpty()) {
            templateDao.insertTemplates(BuiltInTemplates.getAllBuiltInTemplates())
        }
    }

    suspend fun updateBuiltInTemplates() {
        templateDao.replaceBuiltInTemplates(BuiltInTemplates.getAllBuiltInTemplates())
    }

    suspend fun exportTemplate(templateId: String): String? {
        val template = templateDao.getTemplateById(templateId) ?: return null
        // Simple string export for now - will implement proper JSON later
        return "Template: ${template.name}\nCategory: ${template.category}\nContent:\n${template.content}"
    }

    suspend fun importTemplate(templateJson: String): Result<NoteTemplate> {
        return try {
            // Simple import implementation - will enhance later
            val template = NoteTemplate(
                name = "Imported Template",
                description = "Imported from external source",
                category = TemplateCategory.CUSTOM,
                content = templateJson,
                isBuiltIn = false,
                usageCount = 0,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now()
            )
            templateDao.insertTemplate(template)
            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTemplateStatistics(): TemplateStatistics {
        val allTemplates = templateDao.getAllTemplates().first()
        val customCount = templateDao.getCustomTemplateCount().first()
        val builtInCount = allTemplates.count { it.isBuiltIn }
        val totalUsage = allTemplates.sumOf { it.usageCount }
        val mostUsed = allTemplates.maxByOrNull { it.usageCount }
        
        return TemplateStatistics(
            totalTemplates = allTemplates.size,
            customTemplates = customCount,
            builtInTemplates = builtInCount,
            totalUsage = totalUsage,
            mostUsedTemplate = mostUsed
        )
    }

    private fun getCurrentDate(): String {
        return Clock.System.now().toString().substringBefore('T')
    }

    private fun getCurrentTime(): String {
        return Clock.System.now().toString().substringAfter('T').substringBefore('.')
    }

    private fun getCurrentDateTime(): String {
        return Clock.System.now().toString()
    }
}

data class TemplateStatistics(
    val totalTemplates: Int,
    val customTemplates: Int,
    val builtInTemplates: Int,
    val totalUsage: Int,
    val mostUsedTemplate: NoteTemplate?
)
