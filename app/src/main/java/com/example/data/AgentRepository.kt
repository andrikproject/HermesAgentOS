package com.example.data

import kotlinx.coroutines.flow.Flow

class AgentRepository(private val agentDao: AgentDao) {
    val allSessions: Flow<List<AgentSession>> = agentDao.getAllSessions()
    val allTasks: Flow<List<AgentTask>> = agentDao.getAllTasks()

    suspend fun getSessionById(sessionId: Int): AgentSession? {
        return agentDao.getSessionById(sessionId)
    }

    suspend fun insertSession(session: AgentSession): Long {
        return agentDao.insertSession(session)
    }

    suspend fun deleteSession(session: AgentSession) {
        agentDao.deleteSession(session)
    }

    fun getMessagesForSession(sessionId: Int): Flow<List<AgentMessage>> {
        return agentDao.getMessagesForSession(sessionId)
    }

    suspend fun insertMessage(message: AgentMessage): Long {
        return agentDao.insertMessage(message)
    }

    suspend fun deleteMessagesForSession(sessionId: Int) {
        agentDao.deleteMessagesForSession(sessionId)
    }

    suspend fun getTaskById(taskId: Int): AgentTask? {
        return agentDao.getTaskById(taskId)
    }

    suspend fun insertTask(task: AgentTask): Long {
        return agentDao.insertTask(task)
    }

    suspend fun updateTask(task: AgentTask) {
        agentDao.updateTask(task)
    }

    suspend fun deleteTaskById(taskId: Int) {
        agentDao.deleteTaskById(taskId)
    }

    // --- Customizable Cron Jobs ---
    val allCronJobs: Flow<List<HermesCronJob>> = agentDao.getAllCronJobs()

    suspend fun insertCronJob(job: HermesCronJob): Long {
        return agentDao.insertCronJob(job)
    }

    suspend fun deleteCronJobById(id: Int) {
        agentDao.deleteCronJobById(id)
    }

    suspend fun updateCronJob(job: HermesCronJob) {
        agentDao.updateCronJob(job)
    }

    suspend fun clearAllCronJobs() {
        agentDao.clearAllCronJobs()
    }

    // --- Customizable Skills ---
    val allSkills: Flow<List<HermesSkill>> = agentDao.getAllSkills()

    suspend fun insertSkill(skill: HermesSkill): Long {
        return agentDao.insertSkill(skill)
    }

    suspend fun updateSkill(skill: HermesSkill) {
        agentDao.updateSkill(skill)
    }

    suspend fun deleteSkillById(id: Int) {
        agentDao.deleteSkillById(id)
    }

    suspend fun clearAllSkills() {
        agentDao.clearAllSkills()
    }
}
