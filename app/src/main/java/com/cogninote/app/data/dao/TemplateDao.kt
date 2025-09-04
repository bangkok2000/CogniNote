package com.cogninote.app.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.cogninote.app.data.entities.NoteTemplate
import com.cogninote.app.data.entities.TemplateCategory

@Dao
interface TemplateDao {

    @Query("SELECT * FROM note_templates ORDER BY usageCount DESC, name ASC")
    fun getAllTemplates(): Flow<List<NoteTemplate>>

    @Query("SELECT * FROM note_templates WHERE category = :category ORDER BY usageCount DESC, name ASC")
    fun getTemplatesByCategory(category: TemplateCategory): Flow<List<NoteTemplate>>

    @Query("SELECT * FROM note_templates WHERE isBuiltIn = 1 ORDER BY name ASC")
    fun getBuiltInTemplates(): Flow<List<NoteTemplate>>

    @Query("SELECT * FROM note_templates WHERE isBuiltIn = 0 ORDER BY usageCount DESC, name ASC")
    fun getCustomTemplates(): Flow<List<NoteTemplate>>

    @Query("SELECT * FROM note_templates WHERE isPublic = 1 ORDER BY usageCount DESC, name ASC")
    fun getPublicTemplates(): Flow<List<NoteTemplate>>

    @Query("SELECT * FROM note_templates WHERE id = :id")
    suspend fun getTemplateById(id: String): NoteTemplate?

    @Query("SELECT * FROM note_templates WHERE id = :id")
    fun getTemplateByIdFlow(id: String): Flow<NoteTemplate?>

    @Query("""
        SELECT * FROM note_templates 
        WHERE name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        OR EXISTS (SELECT 1 FROM json_each(tags) WHERE json_each.value LIKE '%' || :query || '%')
        ORDER BY usageCount DESC, name ASC
    """)
    fun searchTemplates(query: String): Flow<List<NoteTemplate>>

    @Query("SELECT DISTINCT category FROM note_templates ORDER BY category ASC")
    fun getAllCategories(): Flow<List<TemplateCategory>>

    @Query("SELECT COUNT(*) FROM note_templates WHERE isBuiltIn = 0")
    fun getCustomTemplateCount(): Flow<Int>

    @Query("SELECT * FROM note_templates ORDER BY usageCount DESC LIMIT :limit")
    fun getMostUsedTemplates(limit: Int = 5): Flow<List<NoteTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: NoteTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<NoteTemplate>)

    @Update
    suspend fun updateTemplate(template: NoteTemplate)

    @Delete
    suspend fun deleteTemplate(template: NoteTemplate)

    @Query("DELETE FROM note_templates WHERE id = :id")
    suspend fun deleteTemplateById(id: String)

    @Query("DELETE FROM note_templates WHERE isBuiltIn = 0")
    suspend fun deleteAllCustomTemplates()

    @Query("UPDATE note_templates SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsageCount(id: String)

    @Query("SELECT * FROM note_templates WHERE name = :name")
    suspend fun getTemplateByName(name: String): NoteTemplate?

    // Batch operations
    @Transaction
    suspend fun replaceBuiltInTemplates(templates: List<NoteTemplate>) {
        // Delete existing built-in templates
        val existingBuiltIns = getAllBuiltInTemplatesSync()
        existingBuiltIns.forEach { deleteTemplate(it) }
        
        // Insert new built-in templates
        insertTemplates(templates)
    }

    @Query("SELECT * FROM note_templates WHERE isBuiltIn = 1")
    suspend fun getAllBuiltInTemplatesSync(): List<NoteTemplate>
}
