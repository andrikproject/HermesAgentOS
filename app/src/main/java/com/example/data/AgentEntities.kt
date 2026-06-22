package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@JsonClass(generateAdapter = true)
data class ThinkingStep(
    val type: String, // "plan", "tool_call", "tool_output", "reasoning", "completion"
    val title: String,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "agent_sessions")
data class AgentSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdTime: Long = System.currentTimeMillis(),
    val agentPersona: String,
    val agentModel: String = "gemini-3.5-flash"
)

@Entity(tableName = "agent_messages")
data class AgentMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val sender: String, // "user", "agent"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val thinkingStepsJson: String? = null // Converters handle formatting or we can store raw strings
)

@Entity(tableName = "agent_tasks")
data class AgentTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val status: String, // "pending", "running", "completed", "failed"
    val createdTime: Long = System.currentTimeMillis(),
    val lastExecutedTime: Long? = null,
    val result: String? = null,
    val stepsJson: String? = null // JSON Array of ThinkingStep
)

@Entity(tableName = "hermes_cron_jobs")
data class HermesCronJob(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jobId: String,                  // e.g. "sync_daemon"
    val name: String,                  // e.g. "Workspace Sync Daemon"
    val description: String,           // Description text
    val cronExpression: String,        // e.g. "*/5 * * * *"
    val isEnabled: Boolean = true,
    val lastTriggered: Long = 0L,
    val isRunning: Boolean = false
)

@Entity(tableName = "hermes_skills")
data class HermesSkill(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val skillId: String,               // e.g. "web_search"
    val name: String,
    val description: String,
    val triggerCondition: String,      // e.g. "Triggered by search queries"
    val isEnabled: Boolean = true
)

class Converters {
    private val moshi: Moshi by lazy { Moshi.Builder().build() }
    private val stepListType = Types.newParameterizedType(List::class.java, ThinkingStep::class.java)
    
    @TypeConverter
    fun fromStepList(steps: List<ThinkingStep>?): String? {
        if (steps == null) return null
        return try {
            val adapter = moshi.adapter<List<ThinkingStep>>(stepListType)
            adapter.toJson(steps)
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toStepList(json: String?): List<ThinkingStep>? {
        if (json == null) return null
        return try {
            val adapter = moshi.adapter<List<ThinkingStep>>(stepListType)
            adapter.fromJson(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
