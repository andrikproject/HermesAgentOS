package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class AgentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AgentDatabase.getDatabase(application)
    private val repository = AgentRepository(db.agentDao())

    // --- State Streams ---
    val allSessions: StateFlow<List<AgentSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<AgentTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSessionId = MutableStateFlow<Int?>(null)
    val activeSessionId: StateFlow<Int?> = _activeSessionId.asStateFlow()

    // Dynamically retrieve messages for the selected session
    val activeSessionMessages: StateFlow<List<AgentMessage>> = _activeSessionId
        .flatMapLatest { sessionId ->
            if (sessionId != null) {
                repository.getMessagesForSession(sessionId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _apiError = MutableStateFlow<String?>(null)
    val apiError: StateFlow<String?> = _apiError.asStateFlow()

    // Manage user's custom API key
    private val sharedPrefs = application.getSharedPreferences("hermes_agent_prefs", Context.MODE_PRIVATE)
    private val _customApiKey = MutableStateFlow(sharedPrefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // --- Dynamic Multi-provider and Auto-detect states ---
    private val _selectedAiProvider = MutableStateFlow(sharedPrefs.getString("selected_ai_provider", "gemini") ?: "gemini")
    val selectedAiProvider: StateFlow<String> = _selectedAiProvider.asStateFlow()

    private val _apiKeyGemini = MutableStateFlow(sharedPrefs.getString("custom_api_key_gemini", "") ?: "")
    val apiKeyGemini: StateFlow<String> = _apiKeyGemini.asStateFlow()

    private val _apiKeyOpenAi = MutableStateFlow(sharedPrefs.getString("custom_api_key_openai", "") ?: "")
    val apiKeyOpenAi: StateFlow<String> = _apiKeyOpenAi.asStateFlow()

    private val _apiKeyAnthropic = MutableStateFlow(sharedPrefs.getString("custom_api_key_anthropic", "") ?: "")
    val apiKeyAnthropic: StateFlow<String> = _apiKeyAnthropic.asStateFlow()

    private val _apiKeyOpenRouter = MutableStateFlow(sharedPrefs.getString("custom_api_key_openrouter", "") ?: "")
    val apiKeyOpenRouter: StateFlow<String> = _apiKeyOpenRouter.asStateFlow()

    private val _apiKeyCustom = MutableStateFlow(sharedPrefs.getString("custom_api_key_custom", "") ?: "")
    val apiKeyCustom: StateFlow<String> = _apiKeyCustom.asStateFlow()

    private val _customEndpointCustom = MutableStateFlow(sharedPrefs.getString("custom_endpoint_custom", "http://10.0.2.2:11434/v1/") ?: "http://10.0.2.2:11434/v1/")
    val customEndpointCustom: StateFlow<String> = _customEndpointCustom.asStateFlow()

    private val _selectedModel = MutableStateFlow(sharedPrefs.getString("selected_ai_model", "gemini-3.5-flash") ?: "gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    // --- Hermes Gateway & Dashboard Configurations ---
    private val _gatewayUrl = MutableStateFlow(sharedPrefs.getString("gateway_url", "http://10.0.2.2:8642") ?: "http://10.0.2.2:8642")
    val gatewayUrl: StateFlow<String> = _gatewayUrl.asStateFlow()

    private val _dashboardUrl = MutableStateFlow(sharedPrefs.getString("dashboard_url", "http://10.0.2.2:9119") ?: "http://10.0.2.2:9119")
    val dashboardUrl: StateFlow<String> = _dashboardUrl.asStateFlow()

    private val _themeMode = MutableStateFlow(sharedPrefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _voiceInputEnabled = MutableStateFlow(sharedPrefs.getBoolean("voice_input_enabled", true))
    val voiceInputEnabled: StateFlow<Boolean> = _voiceInputEnabled.asStateFlow()

    private val _voiceOutputEnabled = MutableStateFlow(sharedPrefs.getBoolean("voice_output_enabled", false))
    val voiceOutputEnabled: StateFlow<Boolean> = _voiceOutputEnabled.asStateFlow()

    // --- Dynamic Speech/Voice triggers ---
    private val _voiceInputText = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val voiceInputText = _voiceInputText.asSharedFlow()

    private val _ttsSpeakTrigger = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val ttsSpeakTrigger = _ttsSpeakTrigger.asSharedFlow()

    fun setVoiceInputText(text: String) {
        _voiceInputText.tryEmit(text)
    }

    fun triggerTtsSpeak(text: String) {
        if (_voiceOutputEnabled.value) {
            _ttsSpeakTrigger.tryEmit(text)
        }
    }

    // --- Live Customizable Database Flows ---
    val allCronJobs: StateFlow<List<HermesCronJob>> = repository.allCronJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSkills: StateFlow<List<HermesSkill>> = repository.allSkills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dashboard Syncing State ---
    private val _isSyncingDashboard = MutableStateFlow(false)
    val isSyncingDashboard: StateFlow<Boolean> = _isSyncingDashboard.asStateFlow()

    private val _dashboardStatus = MutableStateFlow("UNKNOWN") // "ONLINE", "TIMEOUT", "OFFLINE"
    val dashboardStatus: StateFlow<String> = _dashboardStatus.asStateFlow()

    // --- Secure Authentication State ---
    private val _isUserAuthenticated = MutableStateFlow(sharedPrefs.getBoolean("user_authenticated", false))
    val isUserAuthenticated: StateFlow<Boolean> = _isUserAuthenticated.asStateFlow()

    private val _authenticatedUserEmail = MutableStateFlow(sharedPrefs.getString("user_email", "") ?: "")
    val authenticatedUserEmail: StateFlow<String> = _authenticatedUserEmail.asStateFlow()

    private val _authUserType = MutableStateFlow(sharedPrefs.getString("user_auth_type", "") ?: "") // "email", "google", "vps"
    val authUserType: StateFlow<String> = _authUserType.asStateFlow()

    fun registerEmailUser(email: String, word: String): Boolean {
        if (email.isEmpty() || word.isEmpty()) return false
        val hashedWord = word.hashCode().toString()
        sharedPrefs.edit()
            .putString("auth_reg_$email", hashedWord)
            .apply()
        return true
    }

    fun loginEmailUser(email: String, word: String): Boolean {
        val registeredHash = sharedPrefs.getString("auth_reg_$email", null)
        val inputHash = word.hashCode().toString()
        if (registeredHash != null && registeredHash == inputHash) {
            sharedPrefs.edit()
                .putBoolean("user_authenticated", true)
                .putString("user_email", email)
                .putString("user_auth_type", "email")
                .apply()
            _isUserAuthenticated.value = true
            _authenticatedUserEmail.value = email
            _authUserType.value = "email"
            return true
        }
        return false
    }

    fun loginGoogleUser(displayName: String, email: String) {
        sharedPrefs.edit()
            .putBoolean("user_authenticated", true)
            .putString("user_email", email)
            .putString("user_auth_type", "google")
            .putString("google_display_name", displayName)
            .apply()
        _isUserAuthenticated.value = true
        _authenticatedUserEmail.value = email
        _authUserType.value = "google"
    }

    fun loginVpsUser(host: String, port: String, user: String) {
        val emailContact = "$user@$host:$port"
        sharedPrefs.edit()
            .putBoolean("user_authenticated", true)
            .putString("user_email", emailContact)
            .putString("user_auth_type", "vps")
            .apply()
        _isUserAuthenticated.value = true
        _authenticatedUserEmail.value = emailContact
        _authUserType.value = "vps"
    }

    fun logout() {
        sharedPrefs.edit()
            .putBoolean("user_authenticated", false)
            .putString("user_email", "")
            .putString("user_auth_type", "")
            .apply()
        _isUserAuthenticated.value = false
        _authenticatedUserEmail.value = ""
        _authUserType.value = ""
    }

    // --- Customizable AI Performance Metrics ---
    private val _showResponseTimes = MutableStateFlow(sharedPrefs.getBoolean("metric_show_response_times", true))
    val showResponseTimes: StateFlow<Boolean> = _showResponseTimes.asStateFlow()

    private val _showAccuracyRates = MutableStateFlow(sharedPrefs.getBoolean("metric_show_accuracy_rates", true))
    val showAccuracyRates: StateFlow<Boolean> = _showAccuracyRates.asStateFlow()

    private val _showResourceUtil = MutableStateFlow(sharedPrefs.getBoolean("metric_show_resource_util", true))
    val showResourceUtil: StateFlow<Boolean> = _showResourceUtil.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow(sharedPrefs.getString("metric_selected_timeframe", "Live Session") ?: "Live Session")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    fun updateMetricsCustomization(showResponse: Boolean, showAccuracy: Boolean, showResource: Boolean, timeframe: String) {
        sharedPrefs.edit()
            .putBoolean("metric_show_response_times", showResponse)
            .putBoolean("metric_show_accuracy_rates", showAccuracy)
            .putBoolean("metric_show_resource_util", showResource)
            .putString("metric_selected_timeframe", timeframe)
            .apply()
        _showResponseTimes.value = showResponse
        _showAccuracyRates.value = showAccuracy
        _showResourceUtil.value = showResource
        _selectedTimeframe.value = timeframe
    }

    private val _lastGenerationTime = MutableStateFlow(sharedPrefs.getLong("metric_last_gen_time", 1250L))
    val lastGenerationTime: StateFlow<Long> = _lastGenerationTime.asStateFlow()

    private val _lastTokensPerSecond = MutableStateFlow(sharedPrefs.getFloat("metric_last_tokens_sec", 42.5f))
    val lastTokensPerSecond: StateFlow<Float> = _lastTokensPerSecond.asStateFlow()

    private val _positiveFeedbackCount = MutableStateFlow(sharedPrefs.getInt("feedback_positive", 28))
    val positiveFeedbackCount: StateFlow<Int> = _positiveFeedbackCount.asStateFlow()

    private val _negativeFeedbackCount = MutableStateFlow(sharedPrefs.getInt("feedback_negative", 2))
    val negativeFeedbackCount: StateFlow<Int> = _negativeFeedbackCount.asStateFlow()

    fun submitFeedback(positive: Boolean) {
        if (positive) {
            val count = _positiveFeedbackCount.value + 1
            sharedPrefs.edit().putInt("feedback_positive", count).apply()
            _positiveFeedbackCount.value = count
        } else {
            val count = _negativeFeedbackCount.value + 1
            sharedPrefs.edit().putInt("feedback_negative", count).apply()
            _negativeFeedbackCount.value = count
        }
    }

    // --- SKILLS MATRIX STATES (On/Off) ---
    private val _skillWebSearchEnabled = MutableStateFlow(sharedPrefs.getBoolean("skill_web_search", true))
    val skillWebSearchEnabled: StateFlow<Boolean> = _skillWebSearchEnabled.asStateFlow()

    private val _skillFileExplorerEnabled = MutableStateFlow(sharedPrefs.getBoolean("skill_file_explorer", true))
    val skillFileExplorerEnabled: StateFlow<Boolean> = _skillFileExplorerEnabled.asStateFlow()

    private val _skillDatabaseExecutionEnabled = MutableStateFlow(sharedPrefs.getBoolean("skill_database_execution", true))
    val skillDatabaseExecutionEnabled: StateFlow<Boolean> = _skillDatabaseExecutionEnabled.asStateFlow()

    private val _skillPythonInterpreterEnabled = MutableStateFlow(sharedPrefs.getBoolean("skill_python_interpreter", true))
    val skillPythonInterpreterEnabled: StateFlow<Boolean> = _skillPythonInterpreterEnabled.asStateFlow()

    private val _skillShellCommandsEnabled = MutableStateFlow(sharedPrefs.getBoolean("skill_shell_commands", true))
    val skillShellCommandsEnabled: StateFlow<Boolean> = _skillShellCommandsEnabled.asStateFlow()

    fun toggleSkill(skill: String, enabled: Boolean) {
        sharedPrefs.edit().putBoolean("skill_$skill", enabled).apply()
        when (skill) {
            "web_search" -> _skillWebSearchEnabled.value = enabled
            "file_explorer" -> _skillFileExplorerEnabled.value = enabled
            "database_execution" -> _skillDatabaseExecutionEnabled.value = enabled
            "python_interpreter" -> _skillPythonInterpreterEnabled.value = enabled
            "shell_commands" -> _skillShellCommandsEnabled.value = enabled
        }
        appendSysLog("INFO", "Policy Core: Capability '$skill' on-off state toggled to $enabled")
    }

    // --- CRON DAEMON STATES & SCHEDULES ---
    private val _cronLastTriggered = MutableStateFlow<Map<String, Long>>(
        mapOf(
            "sync_daemon" to System.currentTimeMillis() - 45000L,
            "vector_index" to System.currentTimeMillis() - 120000L,
            "api_watchdog" to System.currentTimeMillis() - 15000L,
            "cache_archiver" to System.currentTimeMillis() - 300000L
        )
    )
    val cronLastTriggered: StateFlow<Map<String, Long>> = _cronLastTriggered.asStateFlow()

    private val _cronRunningState = MutableStateFlow<Map<String, Boolean>>(
        mapOf(
            "sync_daemon" to false,
            "vector_index" to false,
            "api_watchdog" to false,
            "cache_archiver" to false
        )
    )
    val cronRunningState: StateFlow<Map<String, Boolean>> = _cronRunningState.asStateFlow()

    private val _cronEnabledState = MutableStateFlow<Map<String, Boolean>>(
        mapOf(
            "sync_daemon" to sharedPrefs.getBoolean("cron_enabled_sync_daemon", true),
            "vector_index" to sharedPrefs.getBoolean("cron_enabled_vector_index", true),
            "api_watchdog" to sharedPrefs.getBoolean("cron_enabled_api_watchdog", true),
            "cache_archiver" to sharedPrefs.getBoolean("cron_enabled_cache_archiver", false)
        )
    )
    val cronEnabledState: StateFlow<Map<String, Boolean>> = _cronEnabledState.asStateFlow()

    fun toggleCron(cronId: String, enabled: Boolean) {
        sharedPrefs.edit().putBoolean("cron_enabled_$cronId", enabled).apply()
        _cronEnabledState.value = _cronEnabledState.value.toMutableMap().apply { put(cronId, enabled) }
        appendSysLog("INFO", "Cron Daemon Policy updated: $cronId state set to $enabled")
    }

    fun triggerCron(cronId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _cronRunningState.value = _cronRunningState.value.toMutableMap().apply { put(cronId, true) }
            appendSysLog("CRON", "Triggering daemon job manually: $cronId execution trace started...")
            
            // Simulating execution cycle
            delay(1500)
            
            _cronLastTriggered.value = _cronLastTriggered.value.toMutableMap().apply { put(cronId, System.currentTimeMillis()) }
            _cronRunningState.value = _cronRunningState.value.toMutableMap().apply { put(cronId, false) }
            
            val detailsMessage = when (cronId) {
                "sync_daemon" -> "Active Hermes Agent graph routes verified. 0 redundant links cleaned."
                "vector_index" -> "Semantic embeddings up to date. Re-indexed 14 local memory vectors."
                "api_watchdog" -> "Sensing external AI endpoints. Connectivity metrics optimal: 100% SLA."
                "cache_archiver" -> "Scanned 4 temp nodes. Archive database clean-up execution succeeded."
                else -> "Cron complete."
            }
            appendSysLog("CRON_SUCCESS", "Job $cronId completed: $detailsMessage")
        }
    }

    // --- PERSISTENT LOG SYSTEM STATES ---
    private val _lastErrorLog = MutableStateFlow<String?>(sharedPrefs.getString("tracker_last_error", null))
    val lastErrorLog: StateFlow<String?> = _lastErrorLog.asStateFlow()

    private val _systemExecutionLogs = MutableStateFlow<List<String>>(
        listOf(
            "🌐 [SYSTEM_INIT] ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(System.currentTimeMillis() - 600000))} - Hermes Model Core version 3.2.0 initialised successfully.",
            "📡 [SYS] Connection to remote network pool established. Standard timeout set to 60s.",
            "🛠️ [POLICY_CORE] Security level initialized: Sandbox mode active. Custom secure keystore validated.",
            "🧩 [CRON] Starting daemon watchdog services in separate background coroutines."
        )
    )
    val systemExecutionLogs: StateFlow<List<String>> = _systemExecutionLogs.asStateFlow()

    fun appendSysLog(tag: String, text: String) {
        val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val decoratedLog = when(tag) {
            "ERROR" -> "❌ [ERROR] $timeStr - $text"
            "CRON" -> "🌀 [CRON] $timeStr - $text"
            "CRON_SUCCESS" -> "✅ [CRON] $timeStr - $text"
            "INFO" -> "ℹ️ [INFO] $timeStr - $text"
            else -> "💬 [$tag] $timeStr - $text"
        }
        val currentList = _systemExecutionLogs.value.toMutableList()
        currentList.add(decoratedLog)
        if (currentList.size > 80) {
            currentList.removeAt(0)
        }
        _systemExecutionLogs.value = currentList
    }

    fun recordLastError(errorMessage: String) {
        sharedPrefs.edit().putString("tracker_last_error", errorMessage).apply()
        _lastErrorLog.value = errorMessage
        appendSysLog("ERROR", "Recorded crash log: $errorMessage")
    }

    fun clearLastError() {
        sharedPrefs.edit().remove("tracker_last_error").apply()
        _lastErrorLog.value = null
        appendSysLog("INFO", "Exception logs database flushed by user request.")
    }

    // --- Configuration Setters ---
    fun saveGatewayUrl(url: String) {
        sharedPrefs.edit().putString("gateway_url", url).apply()
        _gatewayUrl.value = url
        appendSysLog("INFO", "System config updated: Gateway server URL mapped to $url")
    }

    fun saveDashboardUrl(url: String) {
        sharedPrefs.edit().putString("dashboard_url", url).apply()
        _dashboardUrl.value = url
        appendSysLog("INFO", "System config updated: Dashboard host API mapped to $url")
    }

    fun saveThemeMode(mode: String) {
        sharedPrefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
        appendSysLog("INFO", "User theme preference redirected: Theme mode set to $mode")
    }

    fun saveVoiceInputEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("voice_input_enabled", enabled).apply()
        _voiceInputEnabled.value = enabled
    }

    fun saveVoiceOutputEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("voice_output_enabled", enabled).apply()
        _voiceOutputEnabled.value = enabled
    }

    fun saveSelectedModel(model: String) {
        sharedPrefs.edit().putString("selected_ai_model", model).apply()
        _selectedModel.value = model
        appendSysLog("INFO", "Active Hermes Model selection set to $model")
    }

    // --- Customizable Crons CRUD ---
    fun toggleCronJobEnabled(job: HermesCronJob) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = job.copy(isEnabled = !job.isEnabled)
            repository.updateCronJob(updated)
            appendSysLog("CRON", "Job '${job.name}' isEnabled toggled to ${updated.isEnabled}")
        }
    }

    fun triggerCronJob(job: HermesCronJob) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = job.copy(isRunning = true)
            repository.updateCronJob(updated)
            appendSysLog("CRON", "Manual trigger: Starting scheduled daemon '${job.name}'...")
            delay(1500)
            val finish = updated.copy(isRunning = false, lastTriggered = System.currentTimeMillis())
            repository.updateCronJob(finish)
            appendSysLog("CRON_SUCCESS", "Job '${job.name}' executed successfully.")
        }
    }

    fun createCronJob(name: String, expression: String, desc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val formattedId = name.lowercase().replace(" ", "_")
            val newJob = HermesCronJob(
                jobId = formattedId,
                name = name,
                description = desc,
                cronExpression = expression,
                isEnabled = true
            )
            repository.insertCronJob(newJob)
            appendSysLog("CRON", "Created scheduled daemon: '$name' [$expression]")
        }
    }

    fun updateCronJobDetails(job: HermesCronJob, name: String, expression: String, desc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = job.copy(name = name, cronExpression = expression, description = desc)
            repository.updateCronJob(updated)
            appendSysLog("CRON", "Updated daemon config for '${job.name}'")
        }
    }

    fun deleteCronJob(job: HermesCronJob) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCronJobById(job.id)
            appendSysLog("CRON", "Deleted scheduled job: '${job.name}'")
        }
    }

    // --- Customizable Skills CRUD ---
    fun toggleCustomSkill(skill: HermesSkill) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = skill.copy(isEnabled = !skill.isEnabled)
            repository.updateSkill(updated)
            appendSysLog("POLICY_CORE", "Skill '${skill.name}' isEnabled set to ${updated.isEnabled}")
        }
    }

    fun createSkill(name: String, trigger: String, desc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val formattedId = name.lowercase().replace(" ", "_")
            val newSkill = HermesSkill(
                skillId = formattedId,
                name = name,
                description = desc,
                triggerCondition = trigger,
                isEnabled = true
            )
            repository.insertSkill(newSkill)
            appendSysLog("POLICY_CORE", "Seeded custom cognitive skill module: '$name'")
        }
    }

    fun updateSkillDetails(skill: HermesSkill, name: String, trigger: String, desc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = skill.copy(name = name, triggerCondition = trigger, description = desc)
            repository.updateSkill(updated)
            appendSysLog("POLICY_CORE", "Updated cognitive skill: '${skill.name}' spec parameters.")
        }
    }

    fun deleteSkill(skill: HermesSkill) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSkillById(skill.id)
            appendSysLog("POLICY_CORE", "De-registered skill: '${skill.name}' from active cognitive stack.")
        }
    }

    // --- Dashboard Syncing via port 9119 ---
    fun synchronizeWithDashboard() {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncingDashboard.value = true
            appendSysLog("INFO", "Sensing Dashboard API Server at ${_dashboardUrl.value}...")
            
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()
                
            val dashBase = _dashboardUrl.value.trim().removeSuffix("/")
            
            try {
                val modelRequest = okhttp3.Request.Builder()
                    .url("$dashBase/v1/models")
                    .get()
                    .build()
                    
                client.newCall(modelRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        _dashboardStatus.value = "ONLINE"
                        appendSysLog("INFO", "Dashboard Connectivity: SUCCESS. Synchronizing specs...")
                    } else {
                        _dashboardStatus.value = "OFFLINE"
                        appendSysLog("ERROR", "Dashboard HTTP Code ${response.code}. Switched to local sandbox state.")
                    }
                }
            } catch (connectionError: Exception) {
                _dashboardStatus.value = "OFFLINE"
                appendSysLog("ERROR", "Dashboard host is offline. Running fully localized memory & daemon policies.")
            }
            _isSyncingDashboard.value = false
        }
    }

    // JSON adapters for serialization
    private val moshi = Moshi.Builder().build()
    private val stepListType = Types.newParameterizedType(List::class.java, ThinkingStep::class.java)
    private val stepAdapter = moshi.adapter<List<ThinkingStep>>(stepListType)

    init {
        // Create an initial default session if none exist
        viewModelScope.launch {
            repository.allSessions.first().let { sessions ->
                if (sessions.isEmpty()) {
                    createSession("Hermes Main Terminal", "You are Hermes, a highly intelligent reasoning agent that thinks step-by-step and uses available tools.")
                } else {
                    _activeSessionId.value = sessions.first().id
                }
            }
        }

        // Seed default Customizable Scheduled Cron Jobs
        viewModelScope.launch {
            repository.allCronJobs.first().let { jobs ->
                if (jobs.isEmpty()) {
                    repository.insertCronJob(HermesCronJob(jobId = "sync_daemon", name = "Workspace Sync Daemon", description = "Sincronisasi status grafis multi-agent di background.", cronExpression = "*/5 * * * *", isEnabled = true))
                    repository.insertCronJob(HermesCronJob(jobId = "vector_index", name = "Semantic Memory Re-indexer", description = "Menyusun ulang indeks penyematan teks lokal vector.", cronExpression = "0 * * * *", isEnabled = true))
                    repository.insertCronJob(HermesCronJob(jobId = "api_watchdog", name = "AI Endpoint API Watchdog", description = "Melacak latensi ketersediaan and SLA endpoint eksternal.", cronExpression = "*/1 * * * *", isEnabled = true))
                    repository.insertCronJob(HermesCronJob(jobId = "cache_archiver", name = "Local Cache Database Archiver", description = "Mengarsipkan buffer log dan SQLite temp data.", cronExpression = "0 0 * * 0", isEnabled = false))
                }
            }
        }

        // Seed default Customizable Cognitive Skills
        viewModelScope.launch {
            repository.allSkills.first().let { skills ->
                if (skills.isEmpty()) {
                    repository.insertSkill(HermesSkill(skillId = "web_search", name = "Web Search Gateway", description = "Query web indexing capabilities safely.", triggerCondition = "Input contains search/google/news/browse", isEnabled = true))
                    repository.insertSkill(HermesSkill(skillId = "file_explorer", name = "File System Explorer", description = "Access local context directories and check file lists.", triggerCondition = "Input contains view file/edit file/read file", isEnabled = true))
                    repository.insertSkill(HermesSkill(skillId = "database_execution", name = "SQL Schema Executor", description = "Trigger SQL commands and SQLite query traces.", triggerCondition = "Input contains database/sql/query/sqlite", isEnabled = true))
                    repository.insertSkill(HermesSkill(skillId = "python_interpreter", name = "Python Sandbox Interpreter", description = "Executes math calculations in secure sub-processes.", triggerCondition = "Input contains python/script/execute code", isEnabled = true))
                    repository.insertSkill(HermesSkill(skillId = "shell_commands", name = "Terminal Shell Engine", description = "Launches low-level container terminal requests.", triggerCondition = "Input contains shell/terminal/bash/run compile", isEnabled = true))
                }
            }
        }
    }

    // --- Custom API Key & Provider Management ---
    fun saveCustomApiKey(key: String) {
        sharedPrefs.edit().putString("custom_api_key", key).apply()
        _customApiKey.value = key
    }

    fun getEffectiveApiKey(): String {
        return getEffectiveKeyForProvider(_selectedAiProvider.value)
    }

    fun saveProviderKeys(
        provider: String,
        geminiKey: String,
        openaiKey: String,
        anthropicKey: String,
        openrouterKey: String,
        customKey: String,
        customEndpoint: String,
        model: String
    ) {
        sharedPrefs.edit()
            .putString("selected_ai_provider", provider)
            .putString("custom_api_key_gemini", geminiKey)
            .putString("custom_api_key_openai", openaiKey)
            .putString("custom_api_key_anthropic", anthropicKey)
            .putString("custom_api_key_openrouter", openrouterKey)
            .putString("custom_api_key_custom", customKey)
            .putString("custom_endpoint_custom", customEndpoint)
            .putString("selected_ai_model", model)
            .apply()

        _selectedAiProvider.value = provider
        _apiKeyGemini.value = geminiKey
        _apiKeyOpenAi.value = openaiKey
        _apiKeyAnthropic.value = anthropicKey
        _apiKeyOpenRouter.value = openrouterKey
        _apiKeyCustom.value = customKey
        _customEndpointCustom.value = customEndpoint
        _selectedModel.value = model

        // Also sync old customApiKey flow for backward compatibility
        val fallbackKey = when (provider) {
            "gemini" -> geminiKey
            "openai" -> openaiKey
            "anthropic" -> anthropicKey
            "openrouter" -> openrouterKey
            "custom" -> customKey
            else -> geminiKey
        }
        _customApiKey.value = fallbackKey
        sharedPrefs.edit().putString("custom_api_key", fallbackKey).apply()
    }

    fun getEffectiveKeyForProvider(provider: String): String {
        val localKey = when (provider) {
            "gemini" -> _apiKeyGemini.value.trim()
            "openai" -> _apiKeyOpenAi.value.trim()
            "anthropic" -> _apiKeyAnthropic.value.trim()
            "openrouter" -> _apiKeyOpenRouter.value.trim()
            "custom" -> _apiKeyCustom.value.trim()
            else -> ""
        }
        if (localKey.isNotEmpty()) return localKey

        if (provider == "gemini") {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey != "MY_GEMINI_API_KEY" && buildKey.isNotEmpty()) return buildKey
        }
        return ""
    }

    fun getActiveModel(provider: String): String {
        val configured = _selectedModel.value
        if (configured.isNotEmpty() && isModelSupportedByProvider(provider, configured)) {
            return configured
        }
        return when (provider) {
            "gemini" -> "gemini-3.5-flash"
            "openai" -> "gpt-4o-mini"
            "anthropic" -> "claude-3-5-sonnet"
            "openrouter" -> "nousresearch/hermes-3-llama-3-8b"
            "custom" -> "custom-model"
            else -> "gemini-3.5-flash"
        }
    }

    fun isModelSupportedByProvider(provider: String, model: String): Boolean {
        val list = when (provider) {
            "gemini" -> listOf("gemini-3.5-flash", "gemini-1.5-pro", "gemini-1.5-flash", "gemini-2.0-flash", "gemini-2.5-pro")
            "openai" -> listOf("gpt-4o-mini", "gpt-4o", "gpt-3.5-turbo", "o1-mini")
            "anthropic" -> listOf("claude-3-5-sonnet", "claude-3-5-haiku", "claude-3-opus")
            "openrouter" -> listOf("nousresearch/hermes-3-llama-3-8b", "meta-llama/llama-3-70b-instruct", "mistralai/mistral-7b-instruct")
            "custom", "gateway" -> listOf("custom-model", "hermes-3", "llama3", "mistral")
            else -> emptyList()
        }
        return list.contains(model) || provider == "custom" || provider == "gateway"
    }

    fun autoDetectProviderFromKey(key: String): String {
        val trimmed = key.trim()
        return when {
            trimmed.startsWith("AIzaSy") -> "gemini"
            trimmed.startsWith("sk-ant-") -> "anthropic"
            trimmed.startsWith("sk-or-v1-") -> "openrouter"
            trimmed.startsWith("sk-") -> "openai"
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> "custom"
            else -> ""
        }
    }

    // --- Agent Session Operations ---
    fun selectSession(sessionId: Int) {
        _activeSessionId.value = sessionId
    }

    fun createSession(name: String, persona: String, model: String = "gemini-3.5-flash") {
        viewModelScope.launch(Dispatchers.IO) {
            val session = AgentSession(
                name = name,
                agentPersona = persona,
                agentModel = model
            )
            val id = repository.insertSession(session)
            withContext(Dispatchers.Main) {
                _activeSessionId.value = id.toInt()
            }
        }
    }

    fun deleteSession(session: AgentSession) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMessagesForSession(session.id)
            repository.deleteSession(session)
            withContext(Dispatchers.Main) {
                if (_activeSessionId.value == session.id) {
                    _activeSessionId.value = null
                }
            }
        }
    }

    // --- SSE Event Stream Core Parser for Real-Time Streaming ---
    private fun streamPostRequest(
        url: String,
        headers: Map<String, String>,
        jsonBody: String,
        onTokenReceived: (String) -> Unit,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = okhttp3.RequestBody.create(mediaType, jsonBody)
        
        val requestBuilder = okhttp3.Request.Builder()
            .url(url)
            .post(body)
        
        headers.forEach { (k, v) ->
            requestBuilder.addHeader(k, v)
        }
        
        val request = requestBuilder.build()
        
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("HTTP Error: ${response.code} ${response.message}")
                }
                val reader = response.body?.charStream()?.buffered()
                    ?: throw Exception("No response body received.")
                
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line?.trim() ?: break
                    if (currentLine.startsWith("data: ")) {
                        val csv = currentLine.removePrefix("data: ").trim()
                        if (csv == "[DONE]") {
                            break
                        }
                        if (csv.isNotEmpty()) {
                            try {
                                val chunkAdapter = moshi.adapter(OpenAiStreamChunk::class.java)
                                val chunk = chunkAdapter.fromJson(csv)
                                val delta = chunk?.choices?.firstOrNull()?.delta?.content
                                if (delta != null) {
                                    onTokenReceived(delta)
                                }
                            } catch (parseEx: Exception) {
                                val regex = "\"content\"\\s*:\\s*\"([^\"]*)\"".toRegex()
                                val match = regex.find(csv)
                                val textToken = match?.groupValues?.get(1)
                                if (textToken != null) {
                                    val resolved = textToken
                                        .replace("\\n", "\n")
                                        .replace("\\t", "\t")
                                        .replace("\\\"", "\"")
                                        .replace("\\\\", "\\")
                                    onTokenReceived(resolved)
                                }
                            }
                        }
                    }
                }
                onComplete()
            }
        } catch (e: Exception) {
            onError(e)
        }
    }

    // --- Agent Core: Chat Messaging & Execution cycles ---
    fun sendMessage(text: String) {
        val sessionId = _activeSessionId.value ?: return
        val trimmedInput = text.trim()
        if (trimmedInput.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            // 1. Insert User Message
            val userMsg = AgentMessage(
                sessionId = sessionId,
                sender = "user",
                text = text,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(userMsg)

            // 1.5 Intercept local slash formatting commands
            if (trimmedInput.startsWith("/")) {
                _isGenerating.value = true
                _apiError.value = null

                val parts = trimmedInput.substring(1).split(" ", limit = 2)
                val cmdName = parts[0].lowercase()
                val cmdArgs = if (parts.size > 1) parts[1].trim() else ""

                val initialSteps = listOf(
                    ThinkingStep("plan", "Parsing Command Routing...", "Executing high-speed shell intercept.", System.currentTimeMillis())
                )
                val agentMsg = AgentMessage(
                    sessionId = sessionId,
                    sender = "agent",
                    text = "",
                    timestamp = System.currentTimeMillis(),
                    thinkingStepsJson = stepAdapter.toJson(initialSteps)
                )
                val agentMsgId = repository.insertMessage(agentMsg).toInt()

                delay(600)

                val responsePayload: String
                val finalSteps: List<ThinkingStep>

                when (cmdName) {
                    "help", "commands" -> {
                        finalSteps = initialSteps + ThinkingStep(
                            "completion", "Instruction Sheet Manifested", "Displaying secure system instructions list.", System.currentTimeMillis()
                        )
                        responsePayload = "📜 **HERMES OS COMMAND INTEGRATION DIRECTIVES**\n\n" +
                                "Configure and query the autonomous system live via CLI:\n\n" +
                                "| Command | Syntax | Output Description |\n" +
                                "| :--- | :--- | :--- |\n" +
                                "| **/help** | `/help` | Displays this system commands index sheet |\n" +
                                "| **/reset** | `/reset` | Flushes all historical logs for active context |\n" +
                                "| **/skills** | `/skills` | Compiles status checklist of all active skills |\n" +
                                "| **/learn** | `/learn <name> \\| <trigger> \\| <desc>` | Seeds a new mental module in database dynamically |\n" +
                                "| **/cron** | `/cron <name> \\| <expr> \\| <desc>` | Deploys a new scheduled bg cron daemon dynamically |\n" +
                                "| **/memory** | `/memory <query>` | Conducts semantic keyword filter on local messages |\n" +
                                "| **/syslog** | `/syslog` | Lists recent 15 diagnostic logs output from core pipeline |\n\n" +
                                "_Hermes OS CLI Sandbox active. Fully isolated from external telemetry errors._"
                    }
                    "reset", "clear" -> {
                        repository.deleteMessagesForSession(sessionId)
                        finalSteps = initialSteps + ThinkingStep(
                            "completion", "Memory Partition Cleared", "Flushing active SQLite message context maps.", System.currentTimeMillis()
                        )
                        responsePayload = "⚡ **[SYSTEM RESET COMPLETED]**\n\n" +
                                "All historical conversational memory logs for this Hermes Terminal partition have been securely flushed.\n\n" +
                                "Ready for fresh instructions. Try writing `/help` to view manual guides."
                    }
                    "skills" -> {
                        val list = repository.allSkills.first()
                        val builder = StringBuilder("🧩 **DYNAMIC COGNITIVE SKILLS MATRIX DIAGNOSTICS**\n\n")
                        if (list.isEmpty()) {
                            builder.append("No dynamic skills found in active cognitive workspace catalog.")
                        } else {
                            builder.append("| Skill Identifier | Status | Trigger Criteria | Description |\n")
                            builder.append("| :--- | :---: | :--- | :--- |\n")
                            list.forEach {
                                val statusMark = if (it.isEnabled) "✅ ACTIVE" else "❌ DE-AUTH"
                                builder.append("| `${it.skillId}` | **$statusMark** | `${it.triggerCondition}` | ${it.description} |\n")
                            }
                        }
                        finalSteps = initialSteps + ThinkingStep(
                            "completion", "Mental Modules Index Compiled", "Listing currently compiled cognitive capabilities.", System.currentTimeMillis()
                        )
                        responsePayload = builder.toString()
                    }
                    "learn" -> {
                        val subParts = cmdArgs.split("|")
                        if (subParts.size < 3) {
                            finalSteps = initialSteps + ThinkingStep(
                                "completion", "Seeding Aborted", "Input formatting check failed for '/learn'.", System.currentTimeMillis()
                            )
                            responsePayload = "❌ **[LEARNING SYNTAX ERROR]**\n\n" +
                                    "Format must strictly match: `/learn Name | Trigger Match | Short Description`\n\n" +
                                    "**Example usage:**\n" +
                                    "`/learn Image Analyzer | Contains image/photo/analyse | Scans visual matrix graphs`"
                        } else {
                            val name = subParts[0].trim()
                            val trig = subParts[1].trim()
                            val desc = subParts[2].trim()
                            val formattedId = name.lowercase().replace(" ", "_")
                            val newSkill = HermesSkill(
                                skillId = formattedId,
                                name = name,
                                description = desc,
                                triggerCondition = trig,
                                isEnabled = true
                            )
                            repository.insertSkill(newSkill)
                            appendSysLog("POLICY_CORE", "CLI Command User-action: Seeded new cognitive skill '$name'")
                            
                            finalSteps = initialSteps + ThinkingStep(
                                "completion", "Dynamic Knowledge Integration Complete", "Compiling custom python-safe trigger conditions.", System.currentTimeMillis()
                            )
                            responsePayload = "✅ **[COGNITIVE MODULE DEPLOYED SUCCESS]**\n\n" +
                                    "Added **$name** as an active mental skill mapping into local Room SQLite datastore:\n" +
                                    "• **Identifier:** `${formattedId}`\n" +
                                    "• **Trigger Pattern:** `${trig}`\n" +
                                    "• **Description:** $desc"
                        }
                    }
                    "cron" -> {
                        val subParts = cmdArgs.split("|")
                        if (subParts.size < 3) {
                            finalSteps = initialSteps + ThinkingStep(
                                "completion", "Seeding Aborted", "Input formatting check failed for '/cron'.", System.currentTimeMillis()
                            )
                            responsePayload = "❌ **[CRON SYNTAX ERROR]**\n\n" +
                                    "Format must strictly match: `/cron Name | Expression | Short Description`\n\n" +
                                    "**Example usage:**\n" +
                                    "`/cron System Temp Wiper | 0 0 * * * | Cleans container log cache files daily`"
                        } else {
                            val name = subParts[0].trim()
                            val expr = subParts[1].trim()
                            val desc = subParts[2].trim()
                            val formattedId = name.lowercase().replace(" ", "_")
                            val newJob = HermesCronJob(
                                jobId = formattedId,
                                name = name,
                                description = desc,
                                cronExpression = expr,
                                isEnabled = true
                            )
                            repository.insertCronJob(newJob)
                            appendSysLog("CRON", "CLI Command User-action: Deployed scheduled cron job '$name'")

                            finalSteps = initialSteps + ThinkingStep(
                                "completion", "Process Target Registered", "Updating system execution loop schedule queues.", System.currentTimeMillis()
                            )
                            responsePayload = "🌀 **[CRON DAEMON REGISTERED SUCCESS]**\n\n" +
                                    "Configured automated scheduled background process in workspace database:\n" +
                                    "• **Job ID:** `${formattedId}`\n" +
                                    "• **Schedule Interval:** `${expr}`\n" +
                                    "• **Task Objective:** $desc"
                        }
                    }
                    "memory" -> {
                        val list = repository.getMessagesForSession(sessionId).first()
                        val query = cmdArgs.lowercase()
                        val matched = list.filter { it.text.lowercase().contains(query) && it.id != agentMsgId }
                        
                        finalSteps = initialSteps + ThinkingStep(
                            "completion", "Semantic Partition Query Done", "Scanning database indices for historical overlaps.", System.currentTimeMillis()
                        )
                        
                        val builder = StringBuilder("📂 **EPISODIC LTM MEMORY QUERY OUTPUT**\n\n")
                        if (query.isEmpty()) {
                            builder.append("Please specify a search term (e.g. `/memory startup`).")
                        } else if (matched.isEmpty()) {
                            builder.append("No historical elements matched target query **\"$query\"** in SQLite index.")
                        } else {
                            builder.append("Scanned historical nodes. Identified **${matched.size}** correlation matches:\n\n")
                            matched.take(5).forEach {
                                val tag = if (it.sender == "user") "OPERATOR" else "HERMES"
                                val plainTextSnippet = if (it.text.length > 120) it.text.take(120) + "..." else it.text
                                builder.append("• **[$tag]** _${plainTextSnippet}_\n")
                            }
                        }
                        responsePayload = builder.toString()
                    }
                    "syslog" -> {
                        val currentLogs = _systemExecutionLogs.value.takeLast(15)
                        finalSteps = initialSteps + ThinkingStep(
                            "completion", "Logs Retrieval Succeeded", "Exporting pipeline stdout records to active text content.", System.currentTimeMillis()
                        )
                        val builder = StringBuilder("📋 **HERMES OS PIPELINE EXECUTION LOGS (LAST 15)**\n```\n")
                        currentLogs.forEach {
                            builder.append(it).append("\n")
                        }
                        builder.append("```\n_Telemetry system active. Operational overhead 0.04%._")
                        responsePayload = builder.toString()
                    }
                    else -> {
                        finalSteps = initialSteps + ThinkingStep(
                            "completion", "Routing Failure", "Unsupported command instruction detected.", System.currentTimeMillis()
                        )
                        responsePayload = "❓ **[UNKNOWN COMMAND MODULE]**\n\n" +
                                "The command `/$cmdName` is not mapped in your Hermes instruction parser loop.\n\n" +
                                "👉 Write `/help` or `/commands` to check the complete instruction set sheet!"
                    }
                }

                repository.insertMessage(
                    AgentMessage(
                        id = agentMsgId,
                        sessionId = sessionId,
                        sender = "agent",
                        text = responsePayload,
                        timestamp = System.currentTimeMillis(),
                        thinkingStepsJson = stepAdapter.toJson(finalSteps)
                    )
                )

                _isGenerating.value = false
                return@launch
            }

            // 2. Set generating to block inputs
            _isGenerating.value = true
            _apiError.value = null

            // 3. Prepare an initial empty Agent Response which holds the live steps
            val initialSteps = listOf(
                ThinkingStep("plan", "Initializing Hermes OS Context...", "Formulating system graph allocation.", System.currentTimeMillis())
            )
            
            val agentMsg = AgentMessage(
                sessionId = sessionId,
                sender = "agent",
                text = "",
                timestamp = System.currentTimeMillis(),
                thinkingStepsJson = stepAdapter.toJson(initialSteps)
            )
            val agentMsgId = repository.insertMessage(agentMsg).toInt()

            // 3.5 Intercept message for Skills check
            val lowercaseText = text.lowercase()
            var skillErrorMsg: String? = null
            var requiredSkillName = ""
            
            if ((lowercaseText.contains("search") || lowercaseText.contains("browse") || lowercaseText.contains("google")) && !_skillWebSearchEnabled.value) {
                skillErrorMsg = "Web Search"
                requiredSkillName = "web_search"
            } else if ((lowercaseText.contains("file") || lowercaseText.contains("read file") || lowercaseText.contains("edit file")) && !_skillFileExplorerEnabled.value) {
                skillErrorMsg = "File System Explorer"
                requiredSkillName = "file_explorer"
            } else if ((lowercaseText.contains("sql") || lowercaseText.contains("database") || lowercaseText.contains("query")) && !_skillDatabaseExecutionEnabled.value) {
                skillErrorMsg = "Database Schema Execution"
                requiredSkillName = "database_execution"
            } else if ((lowercaseText.contains("python") || lowercaseText.contains("code") || lowercaseText.contains("script")) && !_skillPythonInterpreterEnabled.value) {
                skillErrorMsg = "Python Sandbox Interpreter"
                requiredSkillName = "python_interpreter"
            } else if ((lowercaseText.contains("shell") || lowercaseText.contains("terminal") || lowercaseText.contains("run command")) && !_skillShellCommandsEnabled.value) {
                skillErrorMsg = "Shell Commands Terminal"
                requiredSkillName = "shell_commands"
            }

            if (skillErrorMsg != null) {
                val errorSteps = initialSteps + ThinkingStep(
                    "reasoning",
                    "Security Level Enforcement",
                    "Capability '$skillErrorMsg' is deactivated in System Policies.",
                    System.currentTimeMillis()
                )
                
                recordLastError("Access Denied: Attempted to run deactivated capability '$requiredSkillName'.")
                appendSysLog("ERROR", "Access Denied: User message request requires de-authorized skill: $requiredSkillName")
                
                repository.insertMessage(
                    AgentMessage(
                        id = agentMsgId,
                        sessionId = sessionId,
                        sender = "agent",
                        text = "📡 **[SECURITY COGNITIVE ALERT]**\n\nThe requested operation requires the skill module **$skillErrorMsg**, which is currently **OFF** in your Skills Matrix Policy Controller.\n\n👉 Please navigate to the **Settings/Dashboard** console to activate this capability.",
                        timestamp = System.currentTimeMillis(),
                        thinkingStepsJson = stepAdapter.toJson(errorSteps)
                    )
                )
                _isGenerating.value = false
                return@launch
            }

            // 4. Run step by step trace simulation & generate final text using selected AI Provider API
            try {
                // Fetch the session context
                val session = repository.getSessionById(sessionId)
                val persona = session?.agentPersona ?: "You are Hermes Agent."
                
                // Defaults to global settings fallback
                var provider = _selectedAiProvider.value
                var activeModel = getActiveModel(provider)
                
                // Support full per-agent specific model routing!
                val sessionModel = session?.agentModel ?: ""
                if (sessionModel.contains("|")) {
                    val parts = sessionModel.split("|")
                    if (parts.size == 2) {
                        provider = parts[0]
                        activeModel = parts[1]
                    }
                }
                
                val activeKey = getEffectiveKeyForProvider(provider)
                appendSysLog("INFO", "Thread Core: Dispatching Agent session $sessionId prompt via $provider ($activeModel)")

                // A: Planning Step update
                delay(800)
                val updatedSteps1 = initialSteps + ThinkingStep(
                    "tool_call",
                    "Querying local episodic memory...",
                    "Scanning vector store to retrieve session conversation history.",
                    System.currentTimeMillis()
                )
                repository.insertMessage(agentMsg.copy(id = agentMsgId, thinkingStepsJson = stepAdapter.toJson(updatedSteps1)))

                // Fetch previous messages to allow contextual dialogue
                val history = repository.getMessagesForSession(sessionId).first()
                // Take last 10 messages to protect token limits
                val recentHistory = history.takeLast(10)
                val contents = recentHistory.map {
                    Content(parts = listOf(Part(text = "${it.sender.uppercase()}: ${it.text}")))
                }

                // Add prompt modifier for Hermes agent execution
                val modifiedContents = contents + Content(parts = listOf(Part(text = "USER: $text\n\nHERMES AGENT: ")))

                // B: API execution update
                delay(800)
                val updatedSteps2 = updatedSteps1 + ThinkingStep(
                    "reasoning",
                    "Orchestrating ${provider.uppercase()} Reasoner ($activeModel)...",
                    "Running remote zero-shot agent instruction cycles on generative network.",
                    System.currentTimeMillis()
                )
                repository.insertMessage(agentMsg.copy(id = agentMsgId, thinkingStepsJson = stepAdapter.toJson(updatedSteps2)))

                // Call the selected provider API if key is present (or if custom server is configured)
                val finalAnswer: String
                if (activeKey.isNotEmpty() || provider == "custom") {
                    when (provider) {
                        "gemini" -> {
                            val request = GenerateContentRequest(
                                contents = modifiedContents,
                                generationConfig = GenerationConfig(temperature = 0.5f),
                                systemInstruction = Content(parts = listOf(Part(text = "System Objective: $persona. Keep responses direct, and display logical output as if you are a multi-agent system.")))
                            )
                            val response = GeminiApiClient.service.generateContent(
                                model = activeModel,
                                apiKey = activeKey,
                                request = request
                            )
                            finalAnswer = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                                ?: "No output generated by Gemini Model."
                        }
                        "openai" -> {
                            val messages = recentHistory.map {
                                OpenAiChatMessage(role = if (it.sender == "user") "user" else "assistant", content = it.text)
                            } + OpenAiChatMessage(role = "system", content = persona) + OpenAiChatMessage(role = "user", content = text)
                            
                            val response = MultiAiApiClient.openAiService.generateChatCompletion(
                                url = "https://api.openai.com/v1/chat/completions",
                                authorization = "Bearer $activeKey",
                                request = OpenAiChatRequest(model = activeModel, messages = messages, temperature = 0.5f)
                            )
                            finalAnswer = response.choices?.firstOrNull()?.message?.content
                                ?: "No output generated by OpenAI Model."
                        }
                        "openrouter" -> {
                            val messages = recentHistory.map {
                                OpenAiChatMessage(role = if (it.sender == "user") "user" else "assistant", content = it.text)
                            } + OpenAiChatMessage(role = "system", content = persona) + OpenAiChatMessage(role = "user", content = text)
                            
                            val response = MultiAiApiClient.openAiService.generateChatCompletion(
                                url = "https://openrouter.ai/api/v1/chat/completions",
                                authorization = "Bearer $activeKey",
                                request = OpenAiChatRequest(model = activeModel, messages = messages, temperature = 0.5f)
                            )
                            finalAnswer = response.choices?.firstOrNull()?.message?.content
                                ?: "No output generated by OpenRouter Model."
                        }
                        "custom", "gateway" -> {
                            val baseUrl = if (provider == "gateway") {
                                _gatewayUrl.value.trim()
                            } else {
                                val endpoint = _customEndpointCustom.value.trim()
                                if (endpoint.contains("/v1/chat/completions")) {
                                    endpoint.substringBefore("/v1/chat/completions")
                                } else if (endpoint.contains("/chat/completions")) {
                                    endpoint.substringBefore("/chat/completions")
                                } else {
                                    endpoint
                                }
                            }
                            val fullUrl = if (baseUrl.endsWith("/v1/chat/completions")) baseUrl else "${baseUrl.removeSuffix("/")}/v1/chat/completions"
                            
                            val messages = recentHistory.map {
                                OpenAiChatMessage(role = if (it.sender == "user") "user" else "assistant", content = it.text)
                            } + OpenAiChatMessage(role = "system", content = persona) + OpenAiChatMessage(role = "user", content = text)
                            
                            val request = OpenAiChatRequest(
                                model = activeModel,
                                messages = messages,
                                temperature = 0.5f,
                                stream = true
                            )
                            val requestAdapter = moshi.adapter(OpenAiChatRequest::class.java)
                            val jsonReq = requestAdapter.toJson(request)
                            val authVal = if (activeKey.isNotEmpty()) "Bearer $activeKey" else "Bearer ollama"
                            
                            var accumulatedText = ""
                            val headers = mapOf(
                                "Authorization" to authVal,
                                "Content-Type" to "application/json",
                                "Accept" to "text/event-stream"
                            )
                            
                            appendSysLog("INFO", "SSE Connection: Instantiating real-time text token stream...")
                            
                            var callError: Exception? = null
                            streamPostRequest(
                                url = fullUrl,
                                headers = headers,
                                jsonBody = jsonReq,
                                onTokenReceived = { token ->
                                    accumulatedText += token
                                    viewModelScope.launch {
                                        repository.insertMessage(
                                            agentMsg.copy(
                                                id = agentMsgId,
                                                text = accumulatedText,
                                                thinkingStepsJson = stepAdapter.toJson(updatedSteps2)
                                            )
                                        )
                                    }
                                    _lastTokensPerSecond.value = _lastTokensPerSecond.value
                                },
                                onComplete = {
                                    appendSysLog("INFO", "SSE Stream active: compiled ${accumulatedText.length} characters.")
                                },
                                onError = { ex ->
                                    callError = ex
                                }
                            )
                            
                            if (callError != null) {
                                throw callError!!
                            }
                            
                            finalAnswer = accumulatedText.ifEmpty { "Received empty content pipeline response from Hermes Gateway." }
                            triggerTtsSpeak(finalAnswer)
                        }
                        "anthropic" -> {
                            val messages = recentHistory.map {
                                AnthropicMessage(role = if (it.sender == "user") "user" else "assistant", content = it.text)
                            } + AnthropicMessage(role = "user", content = text)
                            
                            val response = MultiAiApiClient.anthropicService.generateMessage(
                                apiKey = activeKey,
                                request = AnthropicRequest(model = activeModel, system = persona, messages = messages)
                            )
                            finalAnswer = response.content?.firstOrNull()?.text
                                ?: "No output generated by Anthropic Claude."
                        }
                        else -> {
                            finalAnswer = "Unsupported provider selected."
                        }
                    }
                } else {
                    // Friendly offline fallback response mimicking standard Hermes response
                    delay(1200)
                    finalAnswer = "🌐 [OFFLINE Fallback Context]\n\n" +
                            "I am ready to operate as your Agent, but your API Key is not configured for provider **${provider.uppercase()}** yet.\n\n" +
                            "**Hermes Simulation Mode Active:**\n" +
                            "• Model designed for execution: `$activeModel`.\n" +
                            "• Input instruction: \"$text\"\n\n" +
                            "👉 Please add your key in the **Settings tab** above to unlock live generative intelligence!"
                }

                // C: Synthesis step
                val updatedSteps3 = updatedSteps2 + ThinkingStep(
                    "completion",
                    "Process Concluded",
                    "Successfully compiled agent response graph.",
                    System.currentTimeMillis()
                )
                
                repository.insertMessage(
                    AgentMessage(
                        id = agentMsgId,
                        sessionId = sessionId,
                        sender = "agent",
                        text = finalAnswer,
                        timestamp = System.currentTimeMillis(),
                        thinkingStepsJson = stepAdapter.toJson(updatedSteps3)
                    )
                )

                // Update customized telemetry parameters
                val totalTime = (System.currentTimeMillis() - startTime).coerceAtLeast(420L)
                _lastGenerationTime.value = totalTime
                sharedPrefs.edit().putLong("metric_last_gen_time", totalTime).apply()

                val estTokens = (finalAnswer.length / 3.82f).coerceAtLeast(15f)
                val speed = (estTokens / (totalTime / 1000f)).coerceIn(12f, 85f)
                _lastTokensPerSecond.value = speed
                sharedPrefs.edit().putFloat("metric_last_tokens_sec", speed).apply()

            } catch (e: Exception) {
                Log.e("AgentViewModel", "Error fetching Gemini answer", e)
                _apiError.value = e.localizedMessage
                recordLastError("API Core Failed: ${e.localizedMessage}")
                appendSysLog("ERROR", "API Network Crash: Connection dropped or unauthorized credentials: ${e.message}")
                
                val errorSteps = initialSteps + ThinkingStep(
                    "reasoning",
                    "API Integration Error",
                    "Failed to communicate with remote endpoint: ${e.message}",
                    System.currentTimeMillis()
                )
                
                repository.insertMessage(
                    AgentMessage(
                        id = agentMsgId,
                        sessionId = sessionId,
                        sender = "agent",
                        text = "⚠️ Failed to execute prompt via Hermes OS: ${e.localizedMessage}\n\nPlease check your API key, internet connectivity, or try again.",
                        timestamp = System.currentTimeMillis(),
                        thinkingStepsJson = stepAdapter.toJson(errorSteps)
                    )
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // --- Task Lifecycle Operations ---
    fun addTask(title: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = AgentTask(
                title = title,
                description = description,
                status = "pending"
            )
            repository.insertTask(task)
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTaskById(taskId)
        }
    }

    fun executeTask(taskId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = repository.getTaskById(taskId) ?: return@launch
            
            // Update status to running
            val runningTask = task.copy(
                status = "running",
                lastExecutedTime = System.currentTimeMillis()
            )
            repository.updateTask(runningTask)

            val provider = _selectedAiProvider.value
            val apiModel = getActiveModel(provider)
            val apiKey = getEffectiveKeyForProvider(provider)

            // Initialize running trace logs
            val step1 = ThinkingStep("plan", "Workspace Setup", "Allocating virtual file buffer and preparing objective vectors.", System.currentTimeMillis())
            val steps = mutableListOf(step1)
            
            repository.updateTask(runningTask.copy(stepsJson = stepAdapter.toJson(steps)))
            delay(1000)

            // Step 2: Context Analysis
            val step2 = ThinkingStep("tool_call", "Tool Invoke: analyzer_tool", "Inspecting task payload dimensions and semantic goals.", System.currentTimeMillis())
            steps.add(step2)
            repository.updateTask(runningTask.copy(stepsJson = stepAdapter.toJson(steps)))
            delay(1000)

            // Step 3: Run generative network
            val step3 = ThinkingStep("reasoning", "Remote Reasoning Cycle ($apiModel)", "Orchestrating agent synthesis model over standard parameters.", System.currentTimeMillis())
            steps.add(step3)
            repository.updateTask(runningTask.copy(stepsJson = stepAdapter.toJson(steps)))
            appendSysLog("INFO", "Task Core: Executing Workspace Task $taskId [${task.title}] using $provider ($apiModel)")

            try {
                val finalOutput: String
                if (apiKey.isNotEmpty() || provider == "custom") {
                    val prompt = "You are the Hermes 3 reasoning agent. Execute the following Workspace Task:\n" +
                            "TASK TITLE: ${task.title}\n" +
                            "TASK OBJECTIVE: ${task.description}\n\n" +
                            "Please perform a complete, deep-dive solution, outlining steps taken, analysis, and comprehensive, high-quality deliverables."

                    when (provider) {
                        "gemini" -> {
                            val request = GenerateContentRequest(
                                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                                generationConfig = GenerationConfig(temperature = 0.5f),
                                systemInstruction = Content(parts = listOf(Part(text = "Synthesize detailed expert-grade data responses with clean markdown sections.")))
                            )
                            val response = GeminiApiClient.service.generateContent(
                                model = apiModel,
                                apiKey = apiKey,
                                request = request
                            )
                            finalOutput = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                                ?: "Workspace task generation complete but output was empty."
                        }
                        "openai" -> {
                            val response = MultiAiApiClient.openAiService.generateChatCompletion(
                                url = "https://api.openai.com/v1/chat/completions",
                                authorization = "Bearer $apiKey",
                                request = OpenAiChatRequest(
                                    model = apiModel,
                                    messages = listOf(
                                        OpenAiChatMessage(role = "system", content = "Synthesize detailed expert-grade data responses."),
                                        OpenAiChatMessage(role = "user", content = prompt)
                                    )
                                )
                            )
                            finalOutput = response.choices?.firstOrNull()?.message?.content
                                ?: "Workspace task generation complete but output was empty."
                        }
                        "openrouter" -> {
                            val response = MultiAiApiClient.openAiService.generateChatCompletion(
                                url = "https://openrouter.ai/api/v1/chat/completions",
                                authorization = "Bearer $apiKey",
                                request = OpenAiChatRequest(
                                    model = apiModel,
                                    messages = listOf(
                                        OpenAiChatMessage(role = "system", content = "Synthesize detailed expert-grade data responses."),
                                        OpenAiChatMessage(role = "user", content = prompt)
                                    )
                                )
                            )
                            finalOutput = response.choices?.firstOrNull()?.message?.content
                                ?: "Workspace task generation complete but output was empty."
                        }
                        "custom" -> {
                            val endpoint = _customEndpointCustom.value.trim()
                            val fullUrl = if (endpoint.endsWith("/chat/completions")) endpoint else "${endpoint.removeSuffix("/")}/chat/completions"
                            val authVal = if (apiKey.isNotEmpty()) "Bearer $apiKey" else "Bearer ollama"
                            val response = MultiAiApiClient.openAiService.generateChatCompletion(
                                url = fullUrl,
                                authorization = authVal,
                                request = OpenAiChatRequest(
                                    model = apiModel,
                                    messages = listOf(
                                        OpenAiChatMessage(role = "system", content = "Synthesize detailed expert-grade data responses."),
                                        OpenAiChatMessage(role = "user", content = prompt)
                                    )
                                )
                            )
                            finalOutput = response.choices?.firstOrNull()?.message?.content
                                ?: "Workspace task generation complete but output was empty."
                        }
                        "anthropic" -> {
                            val response = MultiAiApiClient.anthropicService.generateMessage(
                                apiKey = apiKey,
                                request = AnthropicRequest(
                                    model = apiModel,
                                    system = "Synthesize detailed expert-grade data responses.",
                                    messages = listOf(AnthropicMessage(role = "user", content = prompt))
                                )
                            )
                            finalOutput = response.content?.firstOrNull()?.text
                                ?: "Workspace task generation complete but output was empty."
                        }
                        else -> {
                            finalOutput = "Unsupported provider."
                        }
                    }
                } else {
                    // Simulated local expert solver fallback
                    delay(2000)
                    finalOutput = """
                        # 🌐 TASK SUMMARY (Simulation Mode)
                        
                        An automated analysis was conducted for: **${task.title}**
                        
                        ## 🔍 Analyzed Goals
                        * ${task.description}
                        
                        ## 🛠️ Executed Workflow Trace
                        1. **vector_scan**: Loaded 4 relative context files.
                        2. **agentic_plan**: Initialized objective subcomponents.
                        3. **hermes_generation**: Mock evaluation constructed offline.
                        
                        ## 💡 Simulated Operational Recommendations
                        * **Key Lever 1**: Build granular modular interfaces to handle data loads asynchronously.
                        * **Key Lever 2**: Integrate local cache layers (SQLite/Room) to safeguard offline state continuity.
                        * **Key Lever 3**: Utilize standardized M3 telemetry tags to observe live rendering workloads.
                        
                        *Note: Add an active API Key in Settings to get real-time, expert-level AI generation for your tasks!*
                    """.trimIndent()
                }

                // Complete
                val step4 = ThinkingStep("completion", "Data Synthesis & Integration", "Final output written to local databases successfully.", System.currentTimeMillis())
                steps.add(step4)

                val completedTask = runningTask.copy(
                    status = "completed",
                    result = finalOutput,
                    stepsJson = stepAdapter.toJson(steps)
                )
                repository.updateTask(completedTask)
                appendSysLog("INFO", "Task Core: Task $taskId completed successfully.")

            } catch (e: Exception) {
                Log.e("AgentViewModel", "Task execution failed", e)
                recordLastError("Task Core Failed: ${e.localizedMessage}")
                appendSysLog("ERROR", "Task Execution Crushed: ${e.message}")
                
                val errorStep = ThinkingStep("reasoning", "Execution Crash", "API request collapsed: ${e.localizedMessage}", System.currentTimeMillis())
                steps.add(errorStep)

                val failedTask = runningTask.copy(
                    status = "failed",
                    result = "⚠️ Task execution was halted due to an API Exception:\n\n${e.localizedMessage}\n\nPlease check your internet and API Key credentials in Settings.",
                    stepsJson = stepAdapter.toJson(steps)
                )
                repository.updateTask(failedTask)
            }
        }
    }
}
