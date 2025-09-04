package com.cogninote.app.data.repository

import com.cogninote.app.data.dao.FolderDao
import com.cogninote.app.data.entities.Folder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepository @Inject constructor(
    private val folderDao: FolderDao
) {
    fun getAllFolders(): Flow<List<Folder>> = folderDao.getAllFolders()
    
    fun getFolderById(id: String): Flow<Folder?> = folderDao.getFolderByIdFlow(id)
    
    fun getFoldersByParentId(parentId: String?): Flow<List<Folder>> = 
        folderDao.getSubfolders(parentId)
    
    suspend fun insertFolder(folder: Folder) = folderDao.insertFolder(folder)
    
    suspend fun updateFolder(folder: Folder) = folderDao.updateFolder(folder)
    
    suspend fun deleteFolder(folder: Folder) = folderDao.deleteFolder(folder)
    
    suspend fun getFolderCount(): Int = folderDao.getFoldersCount().first()
}
