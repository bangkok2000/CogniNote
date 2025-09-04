package com.cogninote.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey
    val id: String = generateFolderId(),
    val name: String,
    val description: String = "",
    val parentFolderId: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val isDeleted: Boolean = false,
    val sortOrder: Int = 0,
    val isExpanded: Boolean = true, // For UI state
    val noteCount: Int = 0 // Cached note count
) {
    companion object {
        fun generateFolderId(): String {
            return "folder_${Clock.System.now().toEpochMilliseconds()}_${(1000..9999).random()}"
        }
    }
    
    /**
     * Check if this folder is a root folder (no parent)
     */
    fun isRoot(): Boolean = parentFolderId == null
    
    /**
     * Get the depth level of this folder in the hierarchy
     */
    fun getDepth(folders: List<Folder>): Int {
        if (parentFolderId == null) return 0
        
        val parent = folders.find { it.id == parentFolderId }
        return if (parent != null) parent.getDepth(folders) + 1 else 0
    }
    
    /**
     * Get all ancestor folders
     */
    fun getAncestors(folders: List<Folder>): List<Folder> {
        if (parentFolderId == null) return emptyList()
        
        val parent = folders.find { it.id == parentFolderId }
        return if (parent != null) {
            listOf(parent) + parent.getAncestors(folders)
        } else emptyList()
    }
    
    /**
     * Get all descendant folders
     */
    fun getDescendants(folders: List<Folder>): List<Folder> {
        return folders.filter { it.parentFolderId == this.id } +
               folders.filter { it.parentFolderId == this.id }.flatMap { it.getDescendants(folders) }
    }
    
    /**
     * Get the full path as a string
     */
    fun getPath(folders: List<Folder>): String {
        val ancestors = getAncestors(folders).reversed()
        return if (ancestors.isEmpty()) {
            name
        } else {
            ancestors.joinToString(" / ") { it.name } + " / " + name
        }
    }
    
    /**
     * Check if this folder can be moved to the target folder
     * (prevents circular references)
     */
    fun canMoveTo(targetFolderId: String?, folders: List<Folder>): Boolean {
        if (targetFolderId == null) return true // Can move to root
        if (targetFolderId == this.id) return false // Can't move to itself
        
        // Check if target is a descendant (would create circular reference)
        val descendants = getDescendants(folders)
        return targetFolderId !in descendants.map { it.id }
    }
}
