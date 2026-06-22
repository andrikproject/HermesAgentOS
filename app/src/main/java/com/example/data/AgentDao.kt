package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentDao {
    // --- Sessions ---
    @Query("SELECT * FROM agent_sessions ORDER BY createdTime DESC")
    fun getAllSessions(): Flow<List<AgentSession>>

    @Query("SELECT * FROM agent_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Int): AgentSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AgentSession): Long

    @Delete
    suspend fun deleteSession(session: AgentSession)

    // --- Messages ---
    @Query("SELECT * FROM agent_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Int): Flow<List<AgentMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AgentMessage): Long

    @Query("DELETE FROM agent_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Int)

    // --- Tasks ---
    @Query("SELECT * FROM agent_tasks ORDER BY createdTime DESC")
    fun getAllTasks(): Flow<List<AgentTask>>

    @Query("SELECT * FROM agent_tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): AgentTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AgentTask): Long

    @Update
    suspend fun updateTask(task: AgentTask)

    @Query("DELETE FROM agent_tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    // --- Scheduled Cron Jobs ---
    @Query("SELECT * FROM hermes_cron_jobs ORDER BY id ASC")
    fun getAllCronJobs(): Flow<List<HermesCronJob>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCronJob(job: HermesCronJob): Long

    @Query("DELETE FROM hermes_cron_jobs WHERE id = :id")
    suspend fun deleteCronJobById(id: Int)

    @Update
    suspend fun updateCronJob(job: HermesCronJob)

    @Query("DELETE FROM hermes_cron_jobs")
    suspend fun clearAllCronJobs()

    // --- Customizable Skills ---
    @Query("SELECT * FROM hermes_skills ORDER BY id ASC")
    fun getAllSkills(): Flow<List<HermesSkill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: HermesSkill): Long

    @Update
    suspend fun updateSkill(skill: HermesSkill)

    @Query("DELETE FROM hermes_skills WHERE id = :id")
    suspend fun deleteSkillById(id: Int)

    @Query("DELETE FROM hermes_skills")
    suspend fun clearAllSkills()
}
