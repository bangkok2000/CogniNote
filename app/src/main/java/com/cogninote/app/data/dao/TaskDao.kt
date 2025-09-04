package com.cogninote.app.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.cogninote.app.data.entities.Task

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, dueDate ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE noteId = :noteId ORDER BY isCompleted ASC, priority DESC, dueDate ASC")
    fun getTasksByNoteId(noteId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): Task?

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskByIdFlow(id: String): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC, dueDate ASC")
    fun getPendingTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate IS NOT NULL AND dueDate <= :timestamp AND isCompleted = 0 ORDER BY dueDate ASC")
    fun getOverdueTasks(timestamp: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate IS NOT NULL AND dueDate BETWEEN :startTime AND :endTime AND isCompleted = 0 ORDER BY dueDate ASC")
    fun getTasksDueInRange(startTime: Long, endTime: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE reminderAt IS NOT NULL AND reminderAt BETWEEN :startTime AND :endTime AND isCompleted = 0 ORDER BY reminderAt ASC")
    fun getTasksWithRemindersInRange(startTime: Long, endTime: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE priority = :priority AND isCompleted = 0 ORDER BY dueDate ASC")
    fun getTasksByPriority(priority: String): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND isCompleted = 0
        ORDER BY priority DESC, dueDate ASC
    """)
    fun searchPendingTasks(query: String): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE EXISTS (SELECT 1 FROM json_each(tags) WHERE json_each.value = :tag)
        AND isCompleted = 0
        ORDER BY priority DESC, dueDate ASC
    """)
    fun getTasksByTag(tag: String): Flow<List<Task>>

    @Query("""
        SELECT DISTINCT tags.value as tag FROM tasks, json_each(tasks.tags) as tags 
        WHERE tasks.isCompleted = 0 AND tags.value != ''
        ORDER BY tags.value
    """)
    fun getAllTaskTags(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("DELETE FROM tasks WHERE noteId = :noteId")
    suspend fun deleteTasksByNoteId(noteId: String)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTaskCompletion(id: String, isCompleted: Boolean, completedAt: Long?)

    @Query("UPDATE tasks SET priority = :priority WHERE id = :id")
    suspend fun updateTaskPriority(id: String, priority: String)

    @Query("UPDATE tasks SET dueDate = :dueDate WHERE id = :id")
    suspend fun updateTaskDueDate(id: String, dueDate: Long?)

    @Query("UPDATE tasks SET reminderAt = :reminderAt WHERE id = :id")
    suspend fun updateTaskReminder(id: String, reminderAt: Long?)

    // Statistics
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    fun getPendingTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    fun getCompletedTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE dueDate IS NOT NULL AND dueDate <= :timestamp AND isCompleted = 0")
    fun getOverdueTaskCount(timestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE dueDate IS NOT NULL AND dueDate BETWEEN :startTime AND :endTime AND isCompleted = 0")
    fun getTasksDueTodayCount(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0 AND priority = :priority")
    fun getTaskCountByPriority(priority: String): Flow<Int>

    // Batch operations
    @Transaction
    suspend fun completeTasksInNote(noteId: String) {
        val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        // This would need to be implemented with a custom query or multiple operations
    }

    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteAllCompletedTasks()
}
