package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [AgentSession::class, AgentMessage::class, AgentTask::class, HermesCronJob::class, HermesSkill::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AgentDatabase : RoomDatabase() {
    abstract fun agentDao(): AgentDao

    companion object {
        @Volatile
        private var INSTANCE: AgentDatabase? = null

        fun getDatabase(context: Context): AgentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgentDatabase::class.java,
                    "hermes_agent_os_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
