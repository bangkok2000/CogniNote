package com.cogninote.app.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.cogninote.app.data.entities.Tag

@Dao
interface TagDao {

    @Query("SELECT * FROM tags WHERE isDeleted = 0 ORDER BY usageCount DESC, name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE name = :name AND isDeleted = 0")
    suspend fun getTagByName(name: String): Tag?

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' AND isDeleted = 0 ORDER BY usageCount DESC, name ASC")
    fun searchTags(query: String): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE isDeleted = 0 ORDER BY usageCount DESC LIMIT :limit")
    fun getPopularTags(limit: Int = 10): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE usageCount = 0 AND isDeleted = 0")
    fun getUnusedTags(): Flow<List<Tag>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<Tag>)

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("UPDATE tags SET isDeleted = 1 WHERE name = :name")
    suspend fun softDeleteTag(name: String)

    @Query("DELETE FROM tags WHERE name = :name")
    suspend fun hardDeleteTag(name: String)

    @Query("UPDATE tags SET usageCount = usageCount + 1 WHERE name = :name")
    suspend fun incrementUsage(name: String)

    @Query("UPDATE tags SET usageCount = CASE WHEN usageCount > 0 THEN usageCount - 1 ELSE 0 END WHERE name = :name")
    suspend fun decrementUsage(name: String)

    @Query("UPDATE tags SET usageCount = 0 WHERE name = :name")
    suspend fun resetUsage(name: String)

    @Query("DELETE FROM tags WHERE usageCount = 0 AND isDeleted = 0")
    suspend fun cleanupUnusedTags()

    @Query("SELECT COUNT(*) FROM tags WHERE isDeleted = 0")
    fun getTagsCount(): Flow<Int>

    @Query("""
        UPDATE tags SET usageCount = (
            SELECT COUNT(*) FROM notes, json_each(notes.tags) as note_tags 
            WHERE note_tags.value = tags.name AND notes.isDeleted = 0
        ) WHERE name = :name
    """)
    suspend fun recalculateUsage(name: String)

    @Query("""
        UPDATE tags SET usageCount = (
            SELECT COUNT(*) FROM notes, json_each(notes.tags) as note_tags 
            WHERE note_tags.value = tags.name AND notes.isDeleted = 0
        )
    """)
    suspend fun recalculateAllUsages()
}
