package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.ui.AgentOSMainScreen
import com.example.ui.AgentViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var viewModel: AgentViewModel
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private lateinit var speechLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[AgentViewModel::class.java]

        speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                if (spokenText != null) {
                    viewModel.setVoiceInputText(spokenText)
                }
            }
        }

        tts = TextToSpeech(this, this)

        lifecycleScope.launch {
            viewModel.ttsSpeakTrigger.collectLatest { textToSpeak ->
                if (isTtsReady) {
                    // Stripping any markdown markers before speaking for better voice quality
                    val cleanText = textToSpeak
                        .replace(Regex("[*#`_-]"), "")
                        .replace(Regex("\\[.*?]"), "")
                    tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "hermes_reply")
                }
            }
        }

        setContent {
            MyApplicationTheme(viewModel = viewModel) {
                AgentOSMainScreen(
                    viewModel = viewModel,
                    onStartVoiceInput = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak into Hermes terminal...")
                        }
                        try {
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Speech recognition is not supported on this device.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.US
            }
            isTtsReady = true
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

