package com.cogninote.app.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.cogninote.app.data.entities.Folder

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders WHERE isDeleted = 0 ORDER BY sortOrder ASC, name ASC")
    fun getAllFolders(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE id = :id AND isDeleted = 0")
    suspend fun getFolderById(id: String): Folder?

    @Query("SELECT * FROM folders WHERE id = :id AND isDeleted = 0")
    fun getFolderByIdFlow(id: String): Flow<Folder?>

    @Query("SELECT * FROM folders WHERE parentFolderId = :parentId AND isDeleted = 0 ORDER BY sortOrder ASC, name ASC")
    fun getSubfolders(parentId: String?): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentFolderId IS NULL AND isDeleted = 0 ORDER BY sortOrder ASC, name ASC")
    fun getRootFolders(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE name LIKE '%' || :query || '%' AND isDeleted = 0 ORDER BY name ASC")
    fun searchFolders(query: String): Flow<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<Folder>)

    @Update
    suspend fun updateFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("UPDATE folders SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteFolder(id: String)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun hardDeleteFolder(id: String)

    @Query("UPDATE folders SET parentFolderId = :newParentId WHERE id = :id")
    suspend fun moveFolder(id: String, newParentId: String?)

    @Query("UPDATE folders SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)

    @Query("SELECT COUNT(*) FROM folders WHERE isDeleted = 0")
    fun getFoldersCount(): Flow<Int>

    // Get folder hierarchy depth
    @Query("""
        WITH RECURSIVE folder_hierarchy(id, level) AS (
            SELECT id, 0 FROM folders WHERE id = :folderId
            UNION ALL
            SELECT f.id, fh.level + 1 
            FROM folders f 
            JOIN folder_hierarchy fh ON f.parentFolderId = fh.id
        )
        SELECT MAX(level) FROM folder_hierarchy
    """)
    suspend fun getFolderDepth(folderId: String): Int

    // Check if folder has subfolders
    @Query("SELECT COUNT(*) > 0 FROM folders WHERE parentFolderId = :folderId AND isDeleted = 0")
    suspend fun hasSubfolders(folderId: String): Boolean
}
