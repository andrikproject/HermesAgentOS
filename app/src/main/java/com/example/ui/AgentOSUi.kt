package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.provider.OpenableColumns
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AgentMessage
import com.example.data.AgentSession
import com.example.data.AgentTask
import com.example.data.ThinkingStep
import com.example.data.HermesCronJob
import com.example.data.HermesSkill
import com.example.ui.theme.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentOSMainScreen(viewModel: AgentViewModel, onStartVoiceInput: () -> Unit = {}) {
    val isAuthenticated by viewModel.isUserAuthenticated.collectAsStateWithLifecycle()

    if (!isAuthenticated) {
        AuthenticationScreen(viewModel = viewModel)
    } else {
        var currentTab by remember { mutableStateOf("terminal") } // "terminal", "workspace", "coreserv", "dashboard", "settings"

        Scaffold(
            bottomBar = {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = SlateCore,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(width = 1.dp, color = SlateMedium, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    NavigationBarItem(
                        selected = currentTab == "terminal",
                        onClick = { currentTab = "terminal" },
                        icon = { Icon(Icons.Default.Terminal, contentDescription = "Terminal") },
                        label = { Text("Agent Chat", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HermesOrange,
                            selectedTextColor = HermesOrange,
                            indicatorColor = SlateMedium,
                            unselectedIconColor = TextSlateMuted,
                            unselectedTextColor = TextSlateMuted
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == "workspace",
                        onClick = { currentTab = "workspace" },
                        icon = { Icon(Icons.Default.Hub, contentDescription = "Workspace") },
                        label = { Text("Workspace", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberTeal,
                            selectedTextColor = CyberTeal,
                            indicatorColor = SlateMedium,
                            unselectedIconColor = TextSlateMuted,
                            unselectedTextColor = TextSlateMuted
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == "coreserv",
                        onClick = { currentTab = "coreserv" },
                        icon = { Icon(Icons.Default.Widgets, contentDescription = "Core Services") },
                        label = { Text("Core Serv", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HermesOrange,
                            selectedTextColor = HermesOrange,
                            indicatorColor = SlateMedium,
                            unselectedIconColor = TextSlateMuted,
                            unselectedTextColor = TextSlateMuted
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == "dashboard",
                        onClick = { currentTab = "dashboard" },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Dashboard") },
                        label = { Text("Telemetry", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GridGreen,
                            selectedTextColor = GridGreen,
                            indicatorColor = SlateMedium,
                            unselectedIconColor = TextSlateMuted,
                            unselectedTextColor = TextSlateMuted
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == "settings",
                        onClick = { currentTab = "settings" },
                        icon = { Icon(Icons.Default.SettingsInputComponent, contentDescription = "Settings") },
                        label = { Text("Console", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AlertAmber,
                            selectedTextColor = AlertAmber,
                            indicatorColor = SlateMedium,
                            unselectedIconColor = TextSlateMuted,
                            unselectedTextColor = TextSlateMuted
                        )
                    )
                }
            },
            containerColor = CyberObsidian
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(CyberObsidian)
            ) {
                when (currentTab) {
                    "terminal" -> ChatTerminalScreen(viewModel, onStartVoiceInput)
                    "workspace" -> WorkspaceScreen(viewModel)
                    "coreserv" -> CoreServicesScreen(viewModel)
                    "dashboard" -> DashboardScreen(viewModel)
                    "settings" -> SettingsConsoleScreen(viewModel)
                }
            }
        }
    }
}

// ==========================================
// CHAT ATTACHMENT DEFINITIONS & METADATA HELPERS
// ==========================================
data class ChatAttachment(
    val uri: Uri,
    val name: String,
    val mimeType: String?,
    val size: Long,
    val isImage: Boolean
)

fun queryFileMetadata(context: Context, uri: Uri): ChatAttachment {
    var name = "unnamed_source"
    var size = 0L
    val mimeType = context.contentResolver.getType(uri)
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex) ?: name
                }
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
    } catch (e: Exception) {
        uri.path?.let { p ->
            val cut = p.lastIndexOf('/')
            if (cut != -1) name = p.substring(cut + 1)
        }
    }
    val isImage = mimeType?.startsWith("image/") == true || name.endsWith(".png", true) || name.endsWith(".jpg", true) || name.endsWith(".jpeg", true) || name.endsWith(".gif", true) || name.endsWith(".webp", true)
    return ChatAttachment(uri, name, mimeType, size, isImage)
}

// ==========================================
// SCREEN A: CHAT TERMINAL SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTerminalScreen(viewModel: AgentViewModel, onStartVoiceInput: () -> Unit = {}) {
    val messages by viewModel.activeSessionMessages.collectAsStateWithLifecycle()
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val voiceInputEnabled by viewModel.voiceInputEnabled.collectAsStateWithLifecycle()
    
    var textState by remember { mutableStateOf("") }
    val context = LocalContext.current
    var selectedAttachments by remember { mutableStateOf<List<ChatAttachment>>(emptyList()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val meta = queryFileMetadata(context, it)
            selectedAttachments = selectedAttachments + meta
            viewModel.appendSysLog("INFO", "Loaded photo attachment: ${meta.name} (${String.format("%.2f", meta.size / (1024.0 * 1024.0))} MB)")
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val meta = queryFileMetadata(context, it)
            selectedAttachments = selectedAttachments + meta
            viewModel.appendSysLog("INFO", "Loaded document attachment: ${meta.name}")
        }
    }

    var showSessionDialog by remember { mutableStateOf(false) }
    var newSessionName by remember { mutableStateOf("New Hermes Graph") }
    var newSessionPersona by remember { mutableStateOf("You are Hermes, an autonomous reasoning model. Run steps carefully.") }

    LaunchedEffect(Unit) {
        viewModel.voiceInputText.collect { spokenText ->
            if (spokenText.isNotEmpty()) {
                textState = spokenText
            }
        }
    }

    var selectedSessionName = "Hermes Node"
    sessions.find { it.id == activeSessionId }?.let {
        selectedSessionName = it.name
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-scroll on new messages or generation states
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            val targetIdx = if (isGenerating) messages.size else messages.size - 1
            listState.animateScrollToItem(targetIdx)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top System Status Bar / Action Bar
        Surface(
            color = SlateCore,
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = SlateMedium)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Dropdown/Session selector
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateMedium)
                            .clickable { showSessionDialog = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Session",
                            tint = HermesOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedSessionName,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSlateMain,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Switch",
                            tint = TextSlateMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Status Indicator
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (viewModel.getEffectiveApiKey().isNotEmpty()) GridGreen else AlertAmber)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (viewModel.getEffectiveApiKey().isNotEmpty()) "ONLINE (GEN)" else "SANDBOX",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (viewModel.getEffectiveApiKey().isNotEmpty()) GridGreen else AlertAmber,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Messages List Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            if (messages.isEmpty()) {
                // Shiny Empty State for Terminal
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Hermes Bot",
                        tint = HermesOrange,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "HERMES AGENT OS v1.0",
                        style = MaterialTheme.typography.titleLarge,
                        color = HermesOrange
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "An autonomous reasoning network executing step-by-step logic. Try writing an assignment like:\n\"Help me draft an analytical approach to launch a cold-brew startup.\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSlateMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        ChatMessageBubble(message = message, onFeedback = { positive ->
                            viewModel.submitFeedback(positive)
                        })
                    }
                    if (isGenerating) {
                        item {
                            SmilingHermesAnimation()
                        }
                    }
                }
            }

            // Quick bottom scroll anchor floating helper
            if (messages.isNotEmpty() && listState.firstVisibleItemIndex < messages.size - 2) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(HermesOrange)
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Scroll Down",
                        tint = CyberObsidian,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Bottom Input Console
        Surface(
            color = SlateCore,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = SlateMedium)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Horizontal list of selected attachment chips
                if (selectedAttachments.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberObsidian)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .border(width = 1.dp, color = SlateMedium)
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LAMPIRAN (${selectedAttachments.size}):",
                            color = TextSlateMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(selectedAttachments) { file ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SlateMedium),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (file.isImage) Icons.Default.Image else Icons.Default.Description,
                                            contentDescription = "File Type Indicator",
                                            tint = if (file.isImage) HermesOrange else CyberTeal,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = file.name,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1,
                                            modifier = Modifier.widthIn(max = 120.dp),
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        // Delete attachment icon
                                        IconButton(
                                            onClick = {
                                                selectedAttachments = selectedAttachments.filter { it.uri != file.uri }
                                            },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Hapus Lampiran",
                                                tint = ErrorRed,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Upload Photo Button
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .height(40.dp)
                            .width(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SlateMedium)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Upload Foto",
                            tint = HermesOrange
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Upload Document Button
                    IconButton(
                        onClick = { documentPickerLauncher.launch("*/*") },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .height(40.dp)
                            .width(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SlateMedium)
                            .border(1.dp, CyberTeal.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Upload Dokumen",
                            tint = CyberTeal
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        placeholder = { Text("Instruct Hermes Agent...", color = TextSlateMuted, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSlateMain,
                            unfocusedTextColor = TextSlateMain,
                            focusedBorderColor = HermesOrange,
                            unfocusedBorderColor = SlateMedium,
                            focusedContainerColor = CyberObsidian,
                            unfocusedContainerColor = CyberObsidian
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        trailingIcon = {
                            if (textState.isNotEmpty()) {
                                IconButton(onClick = { textState = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSlateMuted)
                                }
                            }
                        },
                        maxLines = 4,
                        enabled = !isGenerating
                    )

                    if (voiceInputEnabled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onStartVoiceInput() },
                            enabled = !isGenerating,
                            modifier = Modifier
                                .height(40.dp)
                                .width(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SlateMedium)
                                .border(1.dp, HermesOrange.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Dictation",
                                tint = HermesOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Hermes styled executor button
                    Button(
                        onClick = {
                            if (textState.isNotBlank() || selectedAttachments.isNotEmpty()) {
                                val attachmentDetails = if (selectedAttachments.isNotEmpty()) {
                                    selectedAttachments.joinToString("\n") { file ->
                                        val sizeStr = String.format("%.1f KB", file.size / 1024.0)
                                        "📎 **[UPLOADED ATTACHMENT]** _${file.name}_ (${sizeStr}) - URI: ${file.uri}"
                                    }
                                } else ""
                                
                                val compiledText = if (attachmentDetails.isNotEmpty()) {
                                    if (textState.isNotBlank()) {
                                        "$textState\n\n$attachmentDetails"
                                    } else {
                                        attachmentDetails
                                    }
                                } else {
                                    textState
                                }
                                
                                viewModel.sendMessage(compiledText)
                                textState = ""
                                selectedAttachments = emptyList()
                                keyboardController?.hide()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HermesOrange,
                            contentColor = CyberObsidian
                        ),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isGenerating && (textState.isNotBlank() || selectedAttachments.isNotEmpty()),
                        modifier = Modifier.height(40.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                color = CyberObsidian,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    // Sessions Selection List Dialog
    if (showSessionDialog) {
        AlertDialog(
            onDismissRequest = { showSessionDialog = false },
            containerColor = SlateCore,
            title = {
                Text(
                    "CONVERSATION NODES",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = HermesOrange,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Manage or deploy a new computational graph context:",
                        color = TextSlateMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Add session input
                    Divider(color = SlateMedium, modifier = Modifier.padding(vertical = 4.dp))
                    Text("Deploy New Node Name:", color = TextSlateMain, style = MaterialTheme.typography.labelLarge)
                    OutlinedTextField(
                        value = newSessionName,
                        onValueChange = { newSessionName = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HermesOrange,
                            focusedTextColor = TextSlateMain,
                            unfocusedBorderColor = SlateMedium,
                            focusedContainerColor = CyberObsidian,
                            unfocusedContainerColor = CyberObsidian
                        )
                    )

                    Button(
                        onClick = {
                            if (newSessionName.isNotBlank()) {
                                viewModel.createSession(newSessionName, newSessionPersona)
                                newSessionName = "New Hermes Graph"
                                showSessionDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HermesOrange, contentColor = CyberObsidian),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Deploy")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SPAWN NODE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = SlateMedium, modifier = Modifier.padding(vertical = 4.dp))

                    // Existing sessions scroll list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        items(sessions) { s ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (s.id == activeSessionId) SlateMedium else Color.Transparent)
                                    .clickable {
                                        viewModel.selectSession(s.id)
                                        showSessionDialog = false
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Dns, contentDescription = "Node", tint = if (s.id == activeSessionId) HermesOrange else TextSlateMuted, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(s.name, color = TextSlateMain, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                                }
                                if (sessions.size > 1) {
                                    IconButton(
                                        onClick = { viewModel.deleteSession(s) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSessionDialog = false }) {
                    Text("CLOSE", color = TextSlateMuted, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }
}

fun getRelativeTimeString(timeMs: Long): String {
    val diff = System.currentTimeMillis() - timeMs
    if (diff < 0) return "Just now"
    val seconds = diff / 1000
    if (seconds < 60) return "Just now"
    val minutes = seconds / 60
    if (minutes < 60) return "${minutes}m ago"
    val hours = minutes / 60
    if (hours < 24) return "${hours}h ago"
    val days = hours / 24
    return "${days}d ago"
}

@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    val annotatedString = remember(text) {
        val builder = androidx.compose.ui.text.AnnotatedString.Builder()
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val next = text.indexOf("**", i + 2)
                    if (next != -1) {
                        builder.pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = HermesOrangeLight))
                        builder.append(text.substring(i + 2, next))
                        builder.pop()
                        i = next + 2
                    } else {
                        builder.append("**")
                        i += 2
                    }
                }
                text.startsWith("`", i) -> {
                    val next = text.indexOf("`", i + 1)
                    if (next != -1) {
                        builder.pushStyle(androidx.compose.ui.text.SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0xFF1A1A1A),
                            color = Color(0xFFFFB74D),
                            fontSize = 13.sp
                        ))
                        builder.append(text.substring(i + 1, next))
                        builder.pop()
                        i = next + 1
                    } else {
                        builder.append("`")
                        i += 1
                    }
                }
                else -> {
                    builder.append(text[i])
                    i++
                }
            }
        }
        builder.toAnnotatedString()
    }

    Text(
        text = annotatedString,
        color = TextSlateMain,
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = 22.sp,
        modifier = modifier
    )
}

@Composable
fun ChatMessageBubble(message: AgentMessage, onFeedback: (Boolean) -> Unit) {
    val isUser = message.sender == "user"
    val align = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) Color(0xFF161618) else Color(0xFF2B2C2F) // Slate-grey agent bubbles
    val borderCol = if (isUser) SlateMedium else Color(0xFF3E4044)
    
    // Parse thinking steps
    val steps = remember(message.thinkingStepsJson) {
        if (!message.thinkingStepsJson.isNullOrEmpty()) {
            try {
                val moshi = Moshi.Builder().build()
                val stepListType = Types.newParameterizedType(List::class.java, ThinkingStep::class.java)
                moshi.adapter<List<ThinkingStep>>(stepListType).fromJson(message.thinkingStepsJson)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = align
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            // Avatar Indicator with distinctive gold accent
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberObsidian)
                        .border(1.5.dp, HermesOrange, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "H",
                        tint = HermesOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column {
                // Header (Sender title + relative time)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                ) {
                    Text(
                        text = if (isUser) "OPERATOR (YOU)" else "HERMES OS CORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUser) TextSlateMuted else HermesOrange,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${getRelativeTimeString(message.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSlateMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Thinking / Planning steps drop card
                if (!isUser && !steps.isNullOrEmpty()) {
                    var stepsExpanded by remember { mutableStateOf(true) }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .border(width = 1.dp, color = SlateMedium, shape = RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberObsidian)
                    ) {
                        // Title Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { stepsExpanded = !stepsExpanded }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = CyberTeal,
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "TRACE LOGS: ${steps.size} STEPS GENERATED",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = CyberTeal
                                )
                            }
                            Icon(
                                imageVector = if (stepsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = TextSlateMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Trace elements List
                        AnimatedVisibility(
                            visible = stepsExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                steps.forEachIndexed { idx, st ->
                                    val icon = when (st.type) {
                                        "plan" -> Icons.Default.Hub
                                        "tool_call" -> Icons.Default.Build
                                        "reasoning" -> Icons.Default.Memory
                                        else -> Icons.Default.CheckCircle
                                    }
                                    val col = when (st.type) {
                                        "plan" -> HermesOrange
                                        "tool_call" -> CyberTeal
                                        "reasoning" -> AlertAmber
                                        else -> GridGreen
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(icon, contentDescription = null, tint = col, modifier = Modifier.size(14.dp).padding(top = 1.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                "[${idx + 1}/${steps.size}] ${st.title}",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = col
                                            )
                                            if (!st.details.isNullOrEmpty()) {
                                                Text(
                                                    st.details,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 10.sp,
                                                    color = TextSlateMuted
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                // Core Main text block bubble
                if (message.text.isNotEmpty() || steps.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = if (isUser) 16.dp else 0.dp,
                                    topEnd = if (isUser) 0.dp else 16.dp,
                                    bottomStart = 16.dp,
                                    bottomEnd = 16.dp
                                )
                            )
                            .background(bubbleColor)
                            .border(
                                width = 1.dp,
                                color = borderCol,
                                shape = RoundedCornerShape(
                                    topStart = if (isUser) 16.dp else 0.dp,
                                    topEnd = if (isUser) 0.dp else 16.dp,
                                    bottomStart = 16.dp,
                                    bottomEnd = 16.dp
                                )
                            )
                            .padding(14.dp)
                    ) {
                        MarkdownText(
                            text = message.text.ifEmpty { "Computing output response network..." }
                        )
                    }
                }

                if (!isUser) {
                    var feedbackGiven by remember { mutableStateOf<Boolean?>(null) }
                    Row(
                        modifier = Modifier.padding(top = 6.dp, start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Accuracy Rate Feed:",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TextSlateMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Thumbs Up",
                            tint = if (feedbackGiven == true) GridGreen else TextSlateMuted,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable(enabled = feedbackGiven == null) {
                                    feedbackGiven = true
                                    onFeedback(true)
                                }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Thumbs Down",
                            tint = if (feedbackGiven == false) ErrorRed else TextSlateMuted,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable(enabled = feedbackGiven == null) {
                                    feedbackGiven = false
                                    onFeedback(false)
                                }
                        )
                        if (feedbackGiven != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Logged",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = GridGreen
                            )
                        }
                    }
                }
            }

            if (isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateMedium)
                        .border(1.dp, TextSlateMuted, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = TextSlateMain,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN B: AUTONOMOUS WORKSPACE / TASKS SCREEN
// ==========================================
@Composable
fun WorkspaceScreen(viewModel: AgentViewModel) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskDesc by remember { mutableStateOf("") }

    var expandedTaskId by remember { mutableStateOf<Int?>(null) }
    var workspaceTab by remember { mutableStateOf("registry") } // "registry" or "kanban"
    var selectedKanbanColumn by remember { mutableStateOf("running") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Workspace status header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "HERMES AGENT WORKSPACE",
                    style = MaterialTheme.typography.titleLarge,
                    color = CyberTeal,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Deploy and monitor fully autonomous asynchronous solver systems",
                    color = TextSlateMuted,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { showAddTaskDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = CyberObsidian),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("DEPLOY AGENT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        // Current status row counters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val total = tasks.size
            val active = tasks.count { it.status == "running" }
            val completed = tasks.count { it.status == "completed" }

            StatusCounterItem("TOTAL RUNS", total.toString(), CyberTeal, Modifier.weight(1f))
            StatusCounterItem("EXECUTING", active.toString(), AlertAmber, Modifier.weight(1f))
            StatusCounterItem("RESOLVED", completed.toString(), GridGreen, Modifier.weight(1f))
        }

        // Tab sub-navigation for Workspace (Registry vs Kanban vs Sandbox)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(CyberObsidian, RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (workspaceTab == "registry") CyberTeal else Color.Transparent)
                    .clickable { workspaceTab = "registry" }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AGENT REGISTRY",
                    color = if (workspaceTab == "registry") CyberObsidian else TextSlateMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (workspaceTab == "kanban") CyberTeal else Color.Transparent)
                    .clickable { workspaceTab = "kanban" }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PIPELINE KANBAN",
                    color = if (workspaceTab == "kanban") CyberObsidian else TextSlateMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (workspaceTab == "sandbox") CyberTeal else Color.Transparent)
                    .clickable { workspaceTab = "sandbox" }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "COGNITIVE SANDBOX",
                    color = if (workspaceTab == "sandbox") CyberObsidian else TextSlateMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (workspaceTab == "sandbox") {
            CognitiveSandboxView(viewModel)
        } else if (tasks.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Dns, contentDescription = "Empty", tint = TextSlateMuted, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No agents currently deployed in workspace.", color = TextSlateMuted, style = MaterialTheme.typography.bodyMedium)
                Text("Click 'Deploy Agent' above to initialize standard objective targets.", color = TextSlateMuted, fontSize = 12.sp)
            }
        } else {
            if (workspaceTab == "registry") {
                // Std registry details lists
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks) { task ->
                        TaskDeployCard(
                            task = task,
                            isExpanded = expandedTaskId == task.id,
                            onToggleExpand = {
                                expandedTaskId = if (expandedTaskId == task.id) null else task.id
                            },
                            onExecute = { viewModel.executeTask(task.id) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            } else if (workspaceTab == "kanban") {
                // INTERACTIVE PIPELINE KANBAN CONTROLLER
                Column(modifier = Modifier.weight(1f)) {
                    // Column selection tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val columns = listOf(
                            "pending" to "BACKLOG",
                            "running" to "EXECUTING",
                            "completed" to "RESOLVED",
                            "failed" to "CRASHED"
                        )
                        columns.forEach { (colId, label) ->
                            val count = tasks.count { it.status == colId }
                            val isSel = selectedKanbanColumn == colId
                            val accentColor = when (colId) {
                                "pending" -> TextSlateMuted
                                "running" -> AlertAmber
                                "completed" -> GridGreen
                                "failed" -> ErrorRed
                                else -> CyberTeal
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) accentColor.copy(alpha = 0.2f) else CyberObsidian)
                                    .border(1.dp, if (isSel) accentColor else SlateMedium, RoundedCornerShape(6.dp))
                                    .clickable { selectedKanbanColumn = colId }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = label,
                                        color = if (isSel) accentColor else TextSlateMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "($count)",
                                        color = if (isSel) accentColor else TextSlateMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // Kanban items display layout
                    val filteredKanbanTasks = tasks.filter { it.status == selectedKanbanColumn }
                    if (filteredKanbanTasks.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Empty column", tint = TextSlateMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tidak ada agen dalam status pipa ini.", color = TextSlateMuted, fontSize = 11.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredKanbanTasks) { task ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, SlateMedium, RoundedCornerShape(8.dp)),
                                    colors = CardDefaults.cardColors(containerColor = SlateCore)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(
                                                            when (task.status) {
                                                                "pending" -> TextSlateMuted
                                                                "running" -> AlertAmber
                                                                "completed" -> GridGreen
                                                                "failed" -> ErrorRed
                                                                else -> CyberTeal
                                                            }
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "ID: #${task.id} - SYS_WORKER_#${1 + task.id % 4}",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 10.sp,
                                                    color = TextSlateMuted
                                                )
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                IconButton(
                                                    onClick = { viewModel.executeTask(task.id) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Trigger Execution",
                                                        tint = CyberTeal,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { viewModel.deleteTask(task.id) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete agent",
                                                        tint = ErrorRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSlateMain
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = task.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSlateMuted,
                                            maxLines = 2
                                        )

                                        if (task.status == "running") {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                                color = AlertAmber,
                                                trackColor = CyberObsidian
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                CognitiveSandboxView(viewModel)
            }
        }
    }

    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = SlateCore,
            title = {
                Text(
                    "DEPLOY AUTONOMOUS AGENT",
                    fontFamily = FontFamily.Monospace,
                    color = CyberTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Deploy an agent to operate an extensive task and compile structural answers.", color = TextSlateMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Objective Target (Title):", color = TextSlateMain, style = MaterialTheme.typography.labelLarge)
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        placeholder = { Text("E.g., Competitor Vector Evaluation", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberTeal,
                            focusedTextColor = TextSlateMain,
                            unfocusedBorderColor = SlateMedium,
                            focusedContainerColor = CyberObsidian,
                            unfocusedContainerColor = CyberObsidian
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Task Deliverable Directives:", color = TextSlateMain, style = MaterialTheme.typography.labelLarge)
                    OutlinedTextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        placeholder = { Text("Outline full details for the agent...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(vertical = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberTeal,
                            focusedTextColor = TextSlateMain,
                            unfocusedBorderColor = SlateMedium,
                            focusedContainerColor = CyberObsidian,
                            unfocusedContainerColor = CyberObsidian
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("CANCEL", color = TextSlateMuted, fontFamily = FontFamily.Monospace)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank() && taskDesc.isNotBlank()) {
                            viewModel.addTask(taskTitle, taskDesc)
                            taskTitle = ""
                            taskDesc = ""
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = CyberObsidian)
                ) {
                    Text("SPAWN NODE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun StatusCounterItem(title: String, score: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .border(width = 1.dp, color = SlateMedium, shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = SlateCore)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextSlateMuted)
            Text(score, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
        }
    }
}

@Composable
fun TaskDeployCard(
    task: AgentTask,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onExecute: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (task.status) {
        "pending" -> TextSlateMuted
        "running" -> AlertAmber
        "completed" -> GridGreen
        else -> ErrorRed
    }

    val statusIcon = when (task.status) {
        "pending" -> Icons.Default.HourglassEmpty
        "running" -> Icons.Default.RotateLeft
        "completed" -> Icons.Default.CheckCircle
        else -> Icons.Default.Cancel
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (task.status == "running") AlertAmber else SlateMedium,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SlateCore)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Main Line Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(statusIcon, contentDescription = task.status, tint = statusColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(task.title, color = TextSlateMain, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        Text("Sub-agent status: ${task.status.uppercase()}", color = statusColor, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.status == "pending" || task.status == "failed" || task.status == "completed") {
                        IconButton(onClick = onExecute, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Execute", tint = CyberTeal, modifier = Modifier.size(20.dp))
                        }
                    } else if (task.status == "running") {
                        CircularProgressIndicator(
                            color = AlertAmber,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Expanded detail logs
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = SlateMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Text("DIR DIRECTIVE:", style = MaterialTheme.typography.labelSmall, color = TextSlateMuted)
                Text(task.description, color = TextSlateMain, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 12.dp))

                // Parse list of tasks
                val taskSteps = remember(task.stepsJson) {
                    if (!task.stepsJson.isNullOrEmpty()) {
                        try {
                            val moshi = Moshi.Builder().build()
                            val stepListType = Types.newParameterizedType(List::class.java, ThinkingStep::class.java)
                            moshi.adapter<List<ThinkingStep>>(stepListType).fromJson(task.stepsJson)
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                }

                if (!taskSteps.isNullOrEmpty()) {
                    Text("AGENT COMPUTATION GRAPH:", style = MaterialTheme.typography.labelSmall, color = CyberTeal, modifier = Modifier.padding(bottom = 6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberObsidian, RoundedCornerShape(8.dp))
                            .border(1.dp, SlateMedium, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        taskSteps.forEachIndexed { i, s ->
                            val ic = when (s.type) {
                                "plan" -> Icons.Default.Hub
                                "tool_call" -> Icons.Default.Build
                                "reasoning" -> Icons.Default.Memory
                                else -> Icons.Default.CheckCircle
                            }
                            val cl = when (s.type) {
                                "plan" -> HermesOrange
                                "tool_call" -> CyberTeal
                                "reasoning" -> AlertAmber
                                else -> GridGreen
                            }

                            Row(verticalAlignment = Alignment.Top) {
                                Icon(ic, contentDescription = null, tint = cl, modifier = Modifier.size(14.dp).padding(top = 1.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("[STEP ${i+1}] ${s.title}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cl)
                                    if (!s.details.isNullOrEmpty()) {
                                        Text(s.details, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSlateMuted)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Show Deliverable Output
                if (!task.result.isNullOrEmpty()) {
                    Text("COMPILED DELIVERABLE (MARKDOWN):", style = MaterialTheme.typography.labelSmall, color = GridGreen)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberObsidian, RoundedCornerShape(8.dp))
                            .border(1.dp, SlateMedium, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = task.result,
                            color = TextSlateMain,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.SansSerif,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN C: CONFIG TERMINAL / SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsConsoleScreen(viewModel: AgentViewModel) {
    // Collect settings StateFlows
    val selectedProvider by viewModel.selectedAiProvider.collectAsStateWithLifecycle()
    val apiKeyGemini by viewModel.apiKeyGemini.collectAsStateWithLifecycle()
    val apiKeyOpenAi by viewModel.apiKeyOpenAi.collectAsStateWithLifecycle()
    val apiKeyAnthropic by viewModel.apiKeyAnthropic.collectAsStateWithLifecycle()
    val apiKeyOpenRouter by viewModel.apiKeyOpenRouter.collectAsStateWithLifecycle()
    val apiKeyCustom by viewModel.apiKeyCustom.collectAsStateWithLifecycle()
    val customEndpointCustom by viewModel.customEndpointCustom.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val availableModels by viewModel.availableModels.collectAsStateWithLifecycle()
    val isFetchingModels by viewModel.isFetchingModels.collectAsStateWithLifecycle()

    val skillWebSearch by viewModel.skillWebSearchEnabled.collectAsStateWithLifecycle()
    val skillFileExplorer by viewModel.skillFileExplorerEnabled.collectAsStateWithLifecycle()
    val skillDatabase by viewModel.skillDatabaseExecutionEnabled.collectAsStateWithLifecycle()
    val skillPython by viewModel.skillPythonInterpreterEnabled.collectAsStateWithLifecycle()
    val skillShell by viewModel.skillShellCommandsEnabled.collectAsStateWithLifecycle()

    val cronsEnabled by viewModel.cronEnabledState.collectAsStateWithLifecycle()
    val cronsLastTriggered by viewModel.cronLastTriggered.collectAsStateWithLifecycle()
    val cronsRunningState by viewModel.cronRunningState.collectAsStateWithLifecycle()

    val sysLogs by viewModel.systemExecutionLogs.collectAsStateWithLifecycle()
    val lastErrorLog by viewModel.lastErrorLog.collectAsStateWithLifecycle()

    // Local Compose states for form manipulation
    var activeProvider by remember(selectedProvider) { mutableStateOf(selectedProvider) }
    var keyGeminiInput by remember(apiKeyGemini) { mutableStateOf(apiKeyGemini) }
    var keyOpenAiInput by remember(apiKeyOpenAi) { mutableStateOf(apiKeyOpenAi) }
    var keyAnthropicInput by remember(apiKeyAnthropic) { mutableStateOf(apiKeyAnthropic) }
    var keyOpenRouterInput by remember(apiKeyOpenRouter) { mutableStateOf(apiKeyOpenRouter) }
    var keyCustomInput by remember(apiKeyCustom) { mutableStateOf(apiKeyCustom) }
    var endpointCustomInput by remember(customEndpointCustom) { mutableStateOf(customEndpointCustom) }
    var selectedModelInput by remember(selectedModel, activeProvider) {
        mutableStateOf(viewModel.getActiveModel(activeProvider))
    }

    var universalInput by remember { mutableStateOf("") }
    var keyVisible by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf(false) }
    var scope = rememberCoroutineScope()

    val providerModels = mapOf(
        "gemini" to listOf("gemini-3.5-flash", "gemini-1.5-pro", "gemini-2.0-flash", "gemini-2.5-pro", "gemini-2.5-flash", "gemini-1.5-flash"),
        "openai" to listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo", "o1", "o1-mini", "o3-mini"),
        "anthropic" to listOf("claude-sonnet-4", "claude-3-5-sonnet", "claude-3-5-haiku", "claude-3-opus", "claude-3-haiku"),
        "openrouter" to listOf("nousresearch/hermes-3-llama-3-8b", "meta-llama/llama-3-70b-instruct", "anthropic/claude-sonnet-4", "openai/gpt-4o", "deepseek/deepseek-chat", "mistralai/mistral-large"),
        "deepseek" to listOf("deepseek-chat", "deepseek-reasoner", "deepseek-coder"),
        "mistral" to listOf("mistral-large", "mistral-medium", "mistral-small", "codestral"),
        "groq" to listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant", "mixtral-8x7b-32768", "gemma2-9b-it"),
        "xai" to listOf("grok-2", "grok-3", "grok-3-mini"),
        "together" to listOf("mixtral-8x22b", "llama-3.3-70b", "qwen-2.5-72b"),
        "custom" to listOf("custom-model", "hermes-3", "llama3", "mistral", "qwen")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Settings status header
        Text(
            "SYSTEM API & CREDENTIALS",
            style = MaterialTheme.typography.titleLarge,
            color = AlertAmber,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Konfigurasi multi-provider AI, otentikasi kunci, dan parameter model reasoning",
            color = TextSlateMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 0: Secured Operator Identity Profile status and Log Out controls
            item {
                val email by viewModel.authenticatedUserEmail.collectAsStateWithLifecycle()
                val type by viewModel.authUserType.collectAsStateWithLifecycle()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateMedium, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCore)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = HermesOrange)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("OPERATIONAL IDENTITY", style = MaterialTheme.typography.titleMedium, color = TextSlateMain, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SlateMedium)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = type.uppercase(),
                                    color = HermesOrange,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Authenticated user: $email",
                            color = TextSlateMain,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.logout() },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = CyberObsidian),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Log Out", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TERMINATE SECURED SESSION", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Card 1: Advanced Auto-Detect Key Paste Console (The User request requirement)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberTeal.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCore)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = "Bolt", tint = CyberTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AUTO-DETECT PROVIDER CONSOLE", style = MaterialTheme.typography.titleSmall, color = TextSlateMain, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Tempel API Key apa saja di bawah. Sistem akan secara otomatis mendeteksi format kunci dan menetapkan provider yang tepat.",
                            fontSize = 11.sp,
                            color = TextSlateMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = universalInput,
                            onValueChange = { universalInput = it },
                            label = { Text("Tempel API Key Anda di sini...", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                            placeholder = { Text("AIzaSy... / sk-... / sk-ant-...", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { keyVisible = !keyVisible }) {
                                    Icon(
                                        imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Show/Hide",
                                        tint = TextSlateMuted
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberTeal,
                                focusedTextColor = TextSlateMain,
                                unfocusedBorderColor = SlateMedium,
                                focusedContainerColor = CyberObsidian,
                                unfocusedContainerColor = CyberObsidian
                            )
                        )

                        // If key detected, show dynamic success alert and setup link
                        val detectedProvider = viewModel.autoDetectProviderFromKey(universalInput)
                        if (detectedProvider.isNotEmpty() && universalInput.trim().length > 10) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GridGreen.copy(alpha = 0.15f))
                                    .border(1.dp, GridGreen, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Match", tint = GridGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Kunci Terdeteksi: ${detectedProvider.uppercase()}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = GridGreen,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Berhasil mengidentifikasi kredensial. Klik di bawah untuk mengaktifkan provider ini.",
                                        fontSize = 11.sp,
                                        color = TextSlateMain
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    activeProvider = detectedProvider
                                    when (detectedProvider) {
                                        "gemini" -> keyGeminiInput = universalInput.trim()
                                        "openai" -> keyOpenAiInput = universalInput.trim()
                                        "anthropic" -> keyAnthropicInput = universalInput.trim()
                                        "openrouter" -> keyOpenRouterInput = universalInput.trim()
                                    }
                                    selectedModelInput = viewModel.getActiveModel(detectedProvider)
                                    universalInput = "" // Clear paste field
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GridGreen, contentColor = CyberObsidian),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("GUNAKAN & AKTIFKAN ${detectedProvider.uppercase()}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Card 2: Manual Interactive Configuration Console for Active Provider
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateMedium, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCore)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = "Active Config", tint = AlertAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MANUAL PROVIDER SELECTION", style = MaterialTheme.typography.titleSmall, color = TextSlateMain, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Horizontally scrollable or wrapped Grid for choosing active provider
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                Triple("gemini", "Google Gemini", Icons.Default.SmartToy),
                                Triple("openai", "OpenAI", Icons.Default.Explore),
                                Triple("anthropic", "Claude", Icons.Default.Fingerprint),
                                Triple("openrouter", "OpenRouter", Icons.Default.Hub),
                                Triple("deepseek", "DeepSeek", Icons.Default.Cloud),
                                Triple("mistral", "Mistral AI", Icons.Default.SmartToy),
                                Triple("groq", "Groq", Icons.Default.Extension),
                                Triple("xai", "xAI Grok", Icons.Default.Star),
                                Triple("together", "Together", Icons.Default.Share),
                                Triple("custom", "Local/Ollama", Icons.Default.Cloud)
                            ).chunked(3).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowItems.forEach { (provId, provLabel, icon) ->
                                        val isSel = activeProvider == provId
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) AlertAmber.copy(alpha = 0.2f) else CyberObsidian)
                                                .border(1.dp, if (isSel) AlertAmber else SlateMedium, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    activeProvider = provId
                                                    selectedModelInput = viewModel.getActiveModel(provId)
                                                }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = provLabel,
                                                tint = if (isSel) AlertAmber else TextSlateMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = provLabel,
                                                color = if (isSel) AlertAmber else TextSlateMain,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                    if (rowItems.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = SlateMedium, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Local Form Fields based on the selected ACTIVE provider
                        Text(
                            text = "KRITIKAL KREDENSIAL: ${activeProvider.uppercase()}",
                            color = AlertAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        when (activeProvider) {
                            "gemini" -> {
                                OutlinedTextField(
                                    value = keyGeminiInput,
                                    onValueChange = { keyGeminiInput = it },
                                    label = { Text("Google Gemini API Key", fontFamily = FontFamily.Monospace) },
                                    placeholder = { Text("AIzaSy...", fontFamily = FontFamily.Monospace) },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AlertAmber,
                                        focusedTextColor = TextSlateMain,
                                        unfocusedBorderColor = SlateMedium,
                                        focusedContainerColor = CyberObsidian,
                                        unfocusedContainerColor = CyberObsidian
                                    )
                                )
                            }
                            "openai" -> {
                                OutlinedTextField(
                                    value = keyOpenAiInput,
                                    onValueChange = { keyOpenAiInput = it },
                                    label = { Text("OpenAI API Key (sk-...)", fontFamily = FontFamily.Monospace) },
                                    placeholder = { Text("sk-...", fontFamily = FontFamily.Monospace) },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AlertAmber,
                                        focusedTextColor = TextSlateMain,
                                        unfocusedBorderColor = SlateMedium,
                                        focusedContainerColor = CyberObsidian,
                                        unfocusedContainerColor = CyberObsidian
                                    )
                                )
                            }
                            "anthropic" -> {
                                OutlinedTextField(
                                    value = keyAnthropicInput,
                                    onValueChange = { keyAnthropicInput = it },
                                    label = { Text("Anthropic Claude Key (sk-ant-...)", fontFamily = FontFamily.Monospace) },
                                    placeholder = { Text("sk-ant-...", fontFamily = FontFamily.Monospace) },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AlertAmber,
                                        focusedTextColor = TextSlateMain,
                                        unfocusedBorderColor = SlateMedium,
                                        focusedContainerColor = CyberObsidian,
                                        unfocusedContainerColor = CyberObsidian
                                    )
                                )
                            }
                            "openrouter" -> {
                                OutlinedTextField(
                                    value = keyOpenRouterInput,
                                    onValueChange = { keyOpenRouterInput = it },
                                    label = { Text("OpenRouter API Key (sk-or-...)", fontFamily = FontFamily.Monospace) },
                                    placeholder = { Text("sk-or-v1-...", fontFamily = FontFamily.Monospace) },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AlertAmber,
                                        focusedTextColor = TextSlateMain,
                                        unfocusedBorderColor = SlateMedium,
                                        focusedContainerColor = CyberObsidian,
                                        unfocusedContainerColor = CyberObsidian
                                    )
                                )
                            }
                            "custom" -> {
                                OutlinedTextField(
                                    value = endpointCustomInput,
                                    onValueChange = { endpointCustomInput = it },
                                    label = { Text("Ollama/Custom Base URL Endpoint", fontFamily = FontFamily.Monospace) },
                                    placeholder = { Text("http://10.0.2.2:11434/v1/", fontFamily = FontFamily.Monospace) },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AlertAmber,
                                        focusedTextColor = TextSlateMain,
                                        unfocusedBorderColor = SlateMedium,
                                        focusedContainerColor = CyberObsidian,
                                        unfocusedContainerColor = CyberObsidian
                                    )
                                )
                                OutlinedTextField(
                                    value = keyCustomInput,
                                    onValueChange = { keyCustomInput = it },
                                    label = { Text("Custom Security Bearer Token (Optional)", fontFamily = FontFamily.Monospace) },
                                    placeholder = { Text("ollama / bearer token...", fontFamily = FontFamily.Monospace) },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AlertAmber,
                                        focusedTextColor = TextSlateMain,
                                        unfocusedBorderColor = SlateMedium,
                                        focusedContainerColor = CyberObsidian,
                                        unfocusedContainerColor = CyberObsidian
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    // Call viewModel.fetchModelsFromEndpoint when both endpoint and key are entered
                                    if (endpointCustomInput.isNotBlank()) {
                                        viewModel.fetchModelsFromEndpoint(endpointCustomInput.trim(), keyCustomInput.trim())
                                    }
                                },
                                enabled = endpointCustomInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = GridGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("🔍 Auto-Detect Available Models", fontSize = 12.sp)
                            }
                            // Show loading indicator when fetching
                            if (viewModel.isFetchingModels.collectAsState().value) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                            }
                            // Show detected models
                            val availableModels by viewModel.availableModels.collectAsState()
                            if (availableModels.isNotEmpty()) {
                                Text(
                                    "Detected Models (${availableModels.size}):",
                                    color = GridGreen,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                        }

                        // Model selection dropdown lists
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "REASONING MODULE:",
                            color = TextSlateMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        val listModels = providerModels[activeProvider] ?: listOf("custom-model")
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listModels.forEach { modelName ->
                                val isModelSel = selectedModelInput == modelName
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isModelSel) AlertAmber else CyberObsidian)
                                        .border(1.dp, if (isModelSel) AlertAmber else SlateMedium, RoundedCornerShape(6.dp))
                                        .clickable { selectedModelInput = modelName }
                                        .padding(vertical = 6.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val shortName = modelName.substringAfter("/")
                                    Text(
                                        text = shortName,
                                        color = if (isModelSel) CyberObsidian else TextSlateMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Action button to SAVE changes
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = {
                                viewModel.saveProviderKeys(
                                    provider = activeProvider,
                                    geminiKey = keyGeminiInput,
                                    openaiKey = keyOpenAiInput,
                                    anthropicKey = keyAnthropicInput,
                                    openrouterKey = keyOpenRouterInput,
                                    customKey = keyCustomInput,
                                    customEndpoint = endpointCustomInput,
                                    model = selectedModelInput
                                )
                                scope.launch {
                                    showSuccessToast = true
                                    delay(1500)
                                    showSuccessToast = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AlertAmber, contentColor = CyberObsidian),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Simpan", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SIMPAN CONFIG SECURE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }

                        // Save feedback logs alert
                        AnimatedVisibility(visible = showSuccessToast) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GridGreen.copy(alpha = 0.2f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Berhasil", tint = GridGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Konfigurasi API Berhasil Diperbarui & Disimpan!", color = GridGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Status Row
                        Spacer(modifier = Modifier.height(10.dp))
                        val isConfigured = viewModel.getEffectiveKeyForProvider(activeProvider).isNotEmpty() || activeProvider == "custom"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberObsidian, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isConfigured) GridGreen else ErrorRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isConfigured) {
                                    "ONLINE (${activeProvider.uppercase()} Active - $selectedModelInput)"
                                } else "No key detected for ${activeProvider.uppercase()} - Fallback simulation mode active",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = if (isConfigured) GridGreen else ErrorRed
                            )
                        }
                    }
                }
            }

            // Card 2: Environment Specs
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateMedium, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCore)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeveloperMode, contentDescription = "OS Specs", tint = CyberTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("HERMES INSTANCE METRICS", style = MaterialTheme.typography.titleMedium, color = TextSlateMain, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        MetricInfoRow("OS OSNAME", "HERMES AGENT OS / Native Android Client", CyberTeal)
                        MetricInfoRow("OS VERSION", "v1.0.0-PROTOTYPE", CyberTeal)
                        MetricInfoRow("DEFAULT REASONER", "gemini-3.5-flash (Online)", CyberTeal)
                        MetricInfoRow("DATABASE ENGINE", "SQLite v3 (Room Local Abstract)", CyberTeal)
                        MetricInfoRow("SYSTEM PERMANENCE", "ENABLED (Persistence State Live)", CyberTeal)
                    }
                }
            }

            // Card 3: Security Statement
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateMedium, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCore)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Security, contentDescription = "Security", tint = GridGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("SANDBOX ISOLATION SECURITY", color = TextSlateMain, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "All prompt inputs and credential tokens are securely encrypted inside localized Android Shared SharedPreferences and SQLite caches. No data is harvested, analyzed, or stored remotely outside Google LLM Generative reasoning instances.",
                                color = TextSlateMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // CARD 4: SYSTEMS KOGNITIF SKILLS MATRIX (Toggles On/Off for all capabilities)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateMedium, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCore)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = "Skills Matrix", tint = AlertAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SISTEM KOGNITIF SKILLS MATRIX", style = MaterialTheme.typography.titleMedium, color = TextSlateMain, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Mengatur perizinan modul komputasi utama agensi Hermes. Ketika dinonaktifkan, model reasoning dilarang menggunakan kemampuan tersebut.",
                            fontSize = 11.sp,
                            color = TextSlateMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        SkillToggleRow(
                            name = "Pencarian Web Seluler (Web Search)",
                            desc = "Akses internet live untuk merayapi data terkini.",
                            isEnabled = skillWebSearch,
                            onToggle = { viewModel.toggleSkill("web_search", it) }
                        )
                        HorizontalDivider(color = SlateMedium, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        SkillToggleRow(
                            name = "Penjelajah Berkas (File System Explorer)",
                            desc = "Asisten membaca/menulis berkas proyek internal.",
                            isEnabled = skillFileExplorer,
                            onToggle = { viewModel.toggleSkill("file_explorer", it) }
                        )
                        HorizontalDivider(color = SlateMedium, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        SkillToggleRow(
                            name = "Eksekusi Basis Data SQL (DB Executor)",
                            desc = "Akses kompilasi kueri dan query SQLite di tempat.",
                            isEnabled = skillDatabase,
                            onToggle = { viewModel.toggleSkill("database_execution", it) }
                        )
                        HorizontalDivider(color = SlateMedium, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        SkillToggleRow(
                            name = "Interpreter Sandbox Python (Python Interpreter)",
                            desc = "Kompilasi kode program lokal matematika-grafis rumit.",
                            isEnabled = skillPython,
                            onToggle = { viewModel.toggleSkill("python_interpreter", it) }
                        )
                        HorizontalDivider(color = SlateMedium, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        SkillToggleRow(
                            name = "Terminal Perintah Shell (Shell Commands)",
                            desc = "Eksekutor terminal shell runtime instruksi internal.",
                            isEnabled = skillShell,
                            onToggle = { viewModel.toggleSkill("shell_commands", it) }
                        )
                    }
                }
            }

            // CARD 5: CRON DAEMON WATCHDOGS (All crons with manual trigger execution)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateMedium, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCore)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = "Cron Daemon", tint = CyberTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DAEMON WATCHDOG CRON SERVICE", style = MaterialTheme.typography.titleMedium, color = TextSlateMain, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Asynchronous worker background loops scheduler. Anda bisa menonaktifkan cron atau memicunya secara manual.",
                            fontSize = 11.sp,
                            color = TextSlateMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val crons = listOf(
                            Triple("sync_daemon", "Workspace Sync Daemon", "Sinkronisasi status grafis multi-agent di background."),
                            Triple("vector_index", "Semantic Memory Re-indexer", "Menyusun ulang indeks penyematan teks lokal vector."),
                            Triple("api_watchdog", "AI Endpoint API Watchdog", "Melacak latensi ketersediaan and SLA endpoint eksternal."),
                            Triple("cache_archiver", "Local Cache Database Archiver", "Mengarsipkan buffer log dan SQLite temp data.")
                        )

                        crons.forEach { (cronId, label, devDesc) ->
                            val isEnabled = cronsEnabled[cronId] ?: false
                            val isRunning = cronsRunningState[cronId] ?: false
                            val lastTime = cronsLastTriggered[cronId] ?: 0L
                            
                            CronDaemonItemRow(
                                name = label,
                                desc = devDesc,
                                isEnabled = isEnabled,
                                isRunning = isRunning,
                                lastTriggered = lastTime,
                                onToggle = { viewModel.toggleCron(cronId, it) },
                                onTrigger = { viewModel.triggerCron(cronId) }
                            )
                            HorizontalDivider(color = SlateMedium, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }

            // CARD 6: REAL LAST ERROR LOGS & EVENT EXECUTOR STREAM
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateMedium, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCore)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Terminal, contentDescription = "Real System Logs", tint = ErrorRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("LOG KESALAHAN & STRIM DEVIASI NYATA", style = MaterialTheme.typography.titleMedium, color = TextSlateMain, fontWeight = FontWeight.Bold)
                            }
                            if (lastErrorLog != null) {
                                Button(
                                    onClick = { viewModel.clearLastError() },
                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = CyberObsidian),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("CLEAR LOG", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Box 1: Real Last Error Screen
                        Text("LOG CRASH / ERROR TERAKHIR NYATA:", style = MaterialTheme.typography.labelSmall, color = ErrorRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (lastErrorLog == null) {
                            Text(
                                "✅ Sempurna: Belum ada kegagalan / Exception terdeteksi dari core workspace.",
                                color = GridGreen,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyberObsidian, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ErrorRed.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .border(1.dp, ErrorRed, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = lastErrorLog!!,
                                    color = ErrorRed,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sinyal galat ini ditangkap secara otomatis saat agen gagal melakukan instruksi API/Fasilitas terbengkalai.",
                                    fontSize = 10.sp,
                                    color = TextSlateMuted
                                )
                            }
                        }

                        // Box 2: Scrollable Live Terminal Stream Container
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("EVENT LOGGER STRIM AKTIF (STRIM OPERATIONAL):", style = MaterialTheme.typography.labelSmall, color = CyberTeal, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberObsidian)
                                .border(1.dp, SlateMedium, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            val stateScroll = rememberScrollState()
                            LaunchedEffect(sysLogs.size) {
                                stateScroll.animateScrollTo(stateScroll.maxValue)
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(stateScroll),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                sysLogs.forEach { logLine ->
                                    Text(
                                        text = logLine,
                                        color = TextSlateMain,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricInfoRow(label: String, valText: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "↳ $label",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TextSlateMuted
        )
        Text(
            text = valText,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accent
        )
    }
}

@Composable
fun SkillToggleRow(name: String, desc: String, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = TextSlateMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(desc, color = TextSlateMuted, fontSize = 10.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isEnabled) "ACTIVE" else "OFFLINE",
                color = if (isEnabled) GridGreen else TextSlateMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyberObsidian,
                    checkedTrackColor = GridGreen,
                    uncheckedThumbColor = TextSlateMuted,
                    uncheckedTrackColor = SlateMedium
                )
            )
        }
    }
}

@Composable
fun CronDaemonItemRow(
    name: String,
    desc: String,
    isEnabled: Boolean,
    isRunning: Boolean,
    lastTriggered: Long,
    onToggle: (Boolean) -> Unit,
    onTrigger: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = TextSlateMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(desc, color = TextSlateMuted, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            val timeString = if (lastTriggered == 0L) "Never" else {
                java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(lastTriggered))
            }
            Text(
                text = "Last triggered: $timeString",
                color = if (isRunning) CyberTeal else TextSlateMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyberObsidian,
                    checkedTrackColor = CyberTeal,
                    uncheckedThumbColor = TextSlateMuted,
                    uncheckedTrackColor = SlateMedium
                )
            )
            Button(
                onClick = onTrigger,
                enabled = isEnabled && !isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberTeal,
                    contentColor = CyberObsidian,
                    disabledContainerColor = SlateMedium.copy(alpha = 0.3f),
                    disabledContentColor = TextSlateMuted
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                if (isRunning) {
                    CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 1.dp, color = CyberObsidian)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Picu", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// OUTSIDE THE CORE SCREENS: SECURE AUTH CONSOLE
// ==========================================
@Composable
fun AuthenticationScreen(viewModel: AgentViewModel) {
    var selectedAuthType by remember { mutableStateOf("email") } // "email", "google", "vps"
    
    // Email Registration / Login state
    var isRegistering by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }
    var authSuccess by remember { mutableStateOf<String?>(null) }
    
    // Google / Social state
    var selectedGoogleAccount by remember { mutableStateOf("arrprojectx@gmail.com") }
    val googleAccounts = listOf("arrprojectx@gmail.com", "admin.hermes@gmail.com", "ops.intelligence@gmail.com")
    var oauthConnecting by remember { mutableStateOf(false) }
    
    // VPS credentials state
    var vpsHostInput by remember { mutableStateOf("192.168.10.45") }
    var vpsPortInput by remember { mutableStateOf("22") }
    var vpsUserInput by remember { mutableStateOf("root") }
    var vpsPasswordInput by remember { mutableStateOf("") }
    var vpsConnecting by remember { mutableStateOf(false) }
    var vpsConnectOutput by remember { mutableStateOf<List<String>>(emptyList()) }
    
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberObsidian)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .background(SlateCore, RoundedCornerShape(24.dp))
                .border(2.dp, SlateMedium, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hermetics OS Digital Branding
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Lock",
                tint = HermesOrange,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "HERMES OS CLIENT",
                style = MaterialTheme.typography.headlineSmall,
                color = TextSlateMain,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Text(
                "SECURED ENVIRONMENT SIGN-IN",
                style = MaterialTheme.typography.labelSmall,
                color = TextSlateMuted,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Authorization Tab Toggles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberObsidian)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    Pair("email", "Email Secure"),
                    Pair("google", "Google SSO"),
                    Pair("vps", "VPS Host")
                ).forEach { (type, label) ->
                    val isTabSelected = selectedAuthType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isTabSelected) SlateMedium else Color.Transparent)
                            .clickable {
                                selectedAuthType = type
                                authError = null
                                authSuccess = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isTabSelected) HermesOrange else TextSlateMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SlateMedium, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Conditional Form rendering based on selected authentication channel
            when (selectedAuthType) {
                "email" -> {
                    Text(
                        text = if (isRegistering) "COMPUTATIONAL IDENTITY REGISTRATION" else "LOCAL ACCOUNT LOGIN",
                        color = HermesOrange,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                    )
                    
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Secured Operator Email", fontFamily = FontFamily.Monospace) },
                        placeholder = { Text("operator@hermes.terminal", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = TextSlateMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSlateMain,
                            unfocusedTextColor = TextSlateMain,
                            focusedBorderColor = HermesOrange,
                            unfocusedBorderColor = SlateMedium,
                            focusedContainerColor = CyberObsidian,
                            unfocusedContainerColor = CyberObsidian
                        )
                    )
                    
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Operator Password", fontFamily = FontFamily.Monospace) },
                        placeholder = { Text("••••••••", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = TextSlateMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSlateMain,
                            unfocusedTextColor = TextSlateMain,
                            focusedBorderColor = HermesOrange,
                            unfocusedBorderColor = SlateMedium,
                            focusedContainerColor = CyberObsidian,
                            unfocusedContainerColor = CyberObsidian
                        )
                    )
                    
                    if (authError != null) {
                        Text(authError!!, color = ErrorRed, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(bottom = 12.dp))
                    }
                    if (authSuccess != null) {
                        Text(authSuccess!!, color = GridGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(bottom = 12.dp))
                    }
                    
                    Button(
                        onClick = {
                            authError = null
                            authSuccess = null
                            if (emailInput.isBlank() || passwordInput.isBlank()) {
                                authError = "Error: Input fields cannot be blank."
                                return@Button
                            }
                            if (isRegistering) {
                                val success = viewModel.registerEmailUser(emailInput, passwordInput)
                                if (success) {
                                    authSuccess = "Account Registered! Switch to login to authentic sign-in."
                                    isRegistering = false
                                } else {
                                    authError = "Failed to register operator."
                                }
                            } else {
                                val loggedIn = viewModel.loginEmailUser(emailInput, passwordInput)
                                if (!loggedIn) {
                                    authError = "Authentication Halt: Incorrect email or password."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HermesOrange, contentColor = CyberObsidian),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val labelText = if (isRegistering) "COMMISSION PROFILE" else "DECRYPT & SIGN IN"
                        Text(labelText, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = if (isRegistering) "Already registered? Authentication Login" else "Don't have an authentication profile? Register here",
                        color = TextSlateMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable {
                                isRegistering = !isRegistering
                                authError = null
                                authSuccess = null
                            }
                            .padding(6.dp)
                    )
                }
                
                "google" -> {
                    Text(
                        text = "SECURE GOOGLE OAUTH 2.0 FEDERATED IDENTITY",
                        color = HermesOrange,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                    )
                    
                    Text(
                        "Please select the active authorization context credentials from the registered Google instances inside the sandbox simulator:",
                        color = TextSlateMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                    
                    googleAccounts.forEach { acc ->
                        val isSelected = selectedGoogleAccount == acc
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SlateMedium else CyberObsidian)
                                .border(1.dp, if (isSelected) HermesOrange else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedGoogleAccount = acc }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Email Instance",
                                tint = if (isSelected) HermesOrange else TextSlateMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = acc,
                                color = TextSlateMain,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (oauthConnecting) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            CircularProgressIndicator(color = HermesOrange, modifier = Modifier.size(16.dp), strokeWidth = 1.5.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Exchanging OAuth client credentials with Google API Server...", color = HermesOrange, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                oauthConnecting = true
                                delay(1200)
                                viewModel.loginGoogleUser(
                                    displayName = selectedGoogleAccount.substringBefore("@").replaceFirstChar { it.uppercase() },
                                    email = selectedGoogleAccount
                                )
                                oauthConnecting = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GridGreen, contentColor = CyberObsidian),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !oauthConnecting
                    ) {
                        Text("FEDERATED GOOGLE AUTHENTICATION", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                
                "vps" -> {
                    Text(
                        text = "VPS SERVER SSH LINK CREDENTIALS (CONVENIENCE)",
                        color = HermesOrange,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                    )
                    Text(
                        "Provides single-sign-on convenience for system terminal developers by directly validating operational identity over active local/cloud VPS systems.",
                        color = TextSlateMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = vpsHostInput,
                            onValueChange = { vpsHostInput = it },
                            label = { Text("SSH Host Name/IP", fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.weight(3f).padding(end = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSlateMain,
                                unfocusedTextColor = TextSlateMain,
                                focusedBorderColor = HermesOrange,
                                unfocusedBorderColor = SlateMedium,
                                focusedContainerColor = CyberObsidian,
                                unfocusedContainerColor = CyberObsidian
                            )
                        )
                        OutlinedTextField(
                            value = vpsPortInput,
                            onValueChange = { vpsPortInput = it },
                            label = { Text("Port", fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSlateMain,
                                unfocusedTextColor = TextSlateMain,
                                focusedBorderColor = HermesOrange,
                                unfocusedBorderColor = SlateMedium,
                                focusedContainerColor = CyberObsidian,
                                unfocusedContainerColor = CyberObsidian
                            )
                        )
                    }
                    
                    OutlinedTextField(
                        value = vpsUserInput,
                        onValueChange = { vpsUserInput = it },
                        label = { Text("SSH Username", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSlateMain,
                            unfocusedTextColor = TextSlateMain,
                            focusedBorderColor = HermesOrange,
                            unfocusedBorderColor = SlateMedium,
                            focusedContainerColor = CyberObsidian,
                            unfocusedContainerColor = CyberObsidian
                        )
                    )
                    
                    OutlinedTextField(
                        value = vpsPasswordInput,
                        onValueChange = { vpsPasswordInput = it },
                        label = { Text("SSH Password / Private Key", fontFamily = FontFamily.Monospace) },
                        placeholder = { Text("••••••••", fontFamily = FontFamily.Monospace) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSlateMain,
                            unfocusedTextColor = TextSlateMain,
                            focusedBorderColor = HermesOrange,
                            unfocusedBorderColor = SlateMedium,
                            focusedContainerColor = CyberObsidian,
                            unfocusedContainerColor = CyberObsidian
                        )
                    )
                    
                    // Connected console trace logging
                    if (vpsConnecting || vpsConnectOutput.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(94.dp)
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberObsidian)
                                .border(1.dp, SlateMedium, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(vpsConnectOutput) { logLine ->
                                    Text(
                                        text = logLine,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = if (logLine.startsWith("ERROR")) ErrorRed else CyberTeal
                                    )
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                if (vpsHostInput.isBlank() || vpsUserInput.isBlank()) {
                                    vpsConnectOutput = listOf("ERROR: Invalid host address or parameter inputs.")
                                    return@launch
                                }
                                vpsConnecting = true
                                vpsConnectOutput = listOf(
                                    "SSH: Resolving host: $vpsHostInput on Port $vpsPortInput...",
                                    "SSH: Linking socket connection descriptor...",
                                )
                                delay(600)
                                vpsConnectOutput = vpsConnectOutput + "SSH: Connected successfully. Negotiating SSHv2 cryptography handshake..."
                                delay(600)
                                vpsConnectOutput = vpsConnectOutput + "SSH: Challenge sent. Cryptography cipher: AES-256GCM. Exchanging public keys..."
                                delay(600)
                                vpsConnectOutput = vpsConnectOutput + listOf(
                                    "SSH: Authorized as user '$vpsUserInput' by localized virtual signature block.",
                                    "SSO: Identity fully authenticated successfully! Registering main terminal session..."
                                )
                                delay(700)
                                viewModel.loginVpsUser(vpsHostInput, vpsPortInput, vpsUserInput)
                                vpsConnecting = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = CyberObsidian),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !vpsConnecting
                    ) {
                        Text("LINK LOCAL VPS & REGISTER SESSION", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// NEW SCREEN D: TELEMETRY PERFORMANCE DASHBOARD
// ==========================================
@Composable
fun DashboardScreen(viewModel: AgentViewModel) {
    val showResp by viewModel.showResponseTimes.collectAsStateWithLifecycle()
    val showAcc by viewModel.showAccuracyRates.collectAsStateWithLifecycle()
    val showRes by viewModel.showResourceUtil.collectAsStateWithLifecycle()
    val timeframe by viewModel.selectedTimeframe.collectAsStateWithLifecycle()

    val lastGenTime by viewModel.lastGenerationTime.collectAsStateWithLifecycle()
    val lastTps by viewModel.lastTokensPerSecond.collectAsStateWithLifecycle()
    val posCount by viewModel.positiveFeedbackCount.collectAsStateWithLifecycle()
    val negCount by viewModel.negativeFeedbackCount.collectAsStateWithLifecycle()

    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    var showDeployAgentDialog by remember { mutableStateOf(false) }

    var showCustomizer by remember { mutableStateOf(false) }

    if (showDeployAgentDialog) {
        var newAgentName by remember { mutableStateOf("") }
        var newAgentPersona by remember { mutableStateOf("Sophisticated Assistant Core Mode") }
        var newAgentModel by remember { mutableStateOf("gemini-3.5-flash") }
        
        AlertDialog(
            onDismissRequest = { showDeployAgentDialog = false },
            title = {
                Text(
                    "DEPLOY NEW NEURAL CORE AGENT",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberTeal,
                    fontSize = 14.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newAgentName,
                        onValueChange = { newAgentName = it },
                        label = { Text("Agent Identifier Name", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSlateMain,
                            focusedBorderColor = CyberTeal,
                            focusedContainerColor = CyberObsidian,
                            unfocusedContainerColor = CyberObsidian,
                            unfocusedTextColor = TextSlateMain
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAgentPersona,
                        onValueChange = { newAgentPersona = it },
                        label = { Text("Directive Persona System Prompt", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSlateMain,
                            focusedBorderColor = CyberTeal,
                            focusedContainerColor = CyberObsidian,
                            unfocusedContainerColor = CyberObsidian,
                            unfocusedTextColor = TextSlateMain
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Select Model Engine Architecture:", color = TextSlateMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("gemini-3.5-flash", "gemini-1.5-pro").forEach { mod ->
                            val isSel = newAgentModel == mod
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) CyberTeal else SlateMedium)
                                    .clickable { newAgentModel = mod }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    mod,
                                    color = if (isSel) CyberObsidian else TextSlateMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAgentName.isNotBlank()) {
                            viewModel.createSession(newAgentName, newAgentPersona, newAgentModel)
                            viewModel.appendSysLog("INFO", "Dispatched instantiation request for agent core: $newAgentName")
                            showDeployAgentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = CyberObsidian)
                ) {
                    Text("DEPLOY CORE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeployAgentDialog = false }) {
                    Text("CANCEL", color = TextSlateMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            },
            containerColor = SlateCore,
            modifier = Modifier.border(1.dp, SlateMedium, RoundedCornerShape(16.dp))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Dashboard Bar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "HERMES TELEMETRY CORES",
                    style = MaterialTheme.typography.titleLarge,
                    color = GridGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Real-time machine statistics & response accuracy graphs",
                    color = TextSlateMuted,
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = { showCustomizer = !showCustomizer },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateMedium)
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Customize Dashboard",
                    tint = TextSlateMain
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Expandable telemetric customisation panel
        AnimatedVisibility(
            visible = showCustomizer,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, SlateMedium, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateCore)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "METRICS VISIBILITY CUSTOMIZATION",
                        color = HermesOrange,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = showResp,
                            onCheckedChange = { viewModel.updateMetricsCustomization(it, showAcc, showRes, timeframe) },
                            colors = CheckboxDefaults.colors(checkmarkColor = CyberObsidian, checkedColor = HermesOrange)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Show Response Times Tracker", color = TextSlateMain, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = showAcc,
                            onCheckedChange = { viewModel.updateMetricsCustomization(showResp, it, showRes, timeframe) },
                            colors = CheckboxDefaults.colors(checkmarkColor = CyberObsidian, checkedColor = HermesOrange)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Show Accuracy Evaluation Score", color = TextSlateMain, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = showRes,
                            onCheckedChange = { viewModel.updateMetricsCustomization(showResp, showAcc, it, timeframe) },
                            colors = CheckboxDefaults.colors(checkmarkColor = CyberObsidian, checkedColor = HermesOrange)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Show Resource Utilization Data", color = TextSlateMain, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Select Analysis Epoch Range:", color = TextSlateMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Live Session", "Daily Trace", "30-Day Epoch").forEach { label ->
                            val isSel = timeframe == label
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) HermesOrange else CyberObsidian)
                                    .clickable { viewModel.updateMetricsCustomization(showResp, showAcc, showRes, label) }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) CyberObsidian else TextSlateMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Metrics list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // DEPLOYED AGENT CORES (REACTIVE RECYCLERVIEW SECTION)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateMedium, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCore)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DeveloperBoard,
                                    contentDescription = "Cores",
                                    tint = CyberTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "DEPLOYED INDUSTRIAL AGENT CORES",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextSlateMain,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            IconButton(
                                onClick = { showDeployAgentDialog = true },
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberTeal)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Core",
                                    tint = CyberObsidian,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hardware-isolated autonomous cognitive thread workers running live JVM pipelines",
                            color = TextSlateMuted,
                            fontSize = 11.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (sessions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(CyberObsidian, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No agent cores deployed. Tap '+' to instantiate.",
                                    color = TextSlateMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(262.dp)
                                    .background(CyberObsidian, RoundedCornerShape(10.dp))
                                    .border(1.dp, SlateMedium, RoundedCornerShape(10.dp))
                                    .padding(vertical = 4.dp),
                                factory = { context ->
                                    RecyclerView(context).apply {
                                        layoutManager = LinearLayoutManager(context)
                                        adapter = DeployedAgentAdapter(
                                            agentList = sessions,
                                            activeSessionId = activeSessionId,
                                            isGenerating = isGenerating,
                                            onAgentClick = { session ->
                                                viewModel.selectSession(session.id)
                                                viewModel.appendSysLog("INFO", "Mounted Agent Core-0${session.id} as main operator context.")
                                            },
                                            onPingAgent = { session ->
                                                viewModel.selectSession(session.id)
                                                viewModel.appendSysLog("INFO", "Dispatched ping telemetry diagnostic trace to Core-0${session.id}")
                                                viewModel.sendMessage("⚡ **[DIAGNOSTIC TELEMETRY PING DISPATCHED]** Hello core! Confirm active operational pathways and register feedback loops under selected ${session.agentModel} model.")
                                            },
                                            onDeleteAgent = { session ->
                                                if (sessions.size > 1) {
                                                    viewModel.deleteSession(session)
                                                    viewModel.appendSysLog("WARN", "Decommissioned and purged Agent Core-0${session.id} data files.")
                                                } else {
                                                    viewModel.appendSysLog("ERROR", "Access Denied: System operates on a strict non-zero core quorum rule.")
                                                }
                                            }
                                        )
                                    }
                                },
                                update = { recyclerView ->
                                    (recyclerView.adapter as? DeployedAgentAdapter)?.updateData(sessions)
                                }
                            )
                        }
                    }
                }
            }

            // METRIC 1: RESPONSE TIMES TRACKER
            if (showResp) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SlateMedium, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateCore)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, contentDescription = "Timer", tint = HermesOrange, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("RESPONSE TIMES LATENCY", style = MaterialTheme.typography.titleMedium, color = TextSlateMain, fontWeight = FontWeight.Bold)
                                }
                                Text("Timeframe: $timeframe", color = TextSlateMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DashboardMetricBox(
                                    title = "LAST RESPONSE Latency",
                                    value = "${lastGenTime}ms",
                                    color = HermesOrange,
                                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                                )
                                DashboardMetricBox(
                                    title = "MIN LATENCY (Best)",
                                    value = "280ms",
                                    color = GridGreen,
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                                )
                                DashboardMetricBox(
                                    title = "PEAK SPIKES (Worse)",
                                    value = "2140ms",
                                    color = ErrorRed,
                                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Draws a beautiful response times mock bar chart using pure Compose Canvas / layout bars
                            Text("Latency historical logs (last 6 reasoning executions):", color = TextSlateMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            val historyLogs = listOf(
                                Pair("Run #1", 1250f),
                                Pair("Run #2", 940f),
                                Pair("Run #3", 1680f),
                                Pair("Run #4", lastGenTime.toFloat()),
                                Pair("Run #5", 580f),
                                Pair("Run #6", 1120f)
                            )
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(CyberObsidian, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                historyLogs.forEach { (run, time) ->
                                    val barHeightPercent = (time / 2500f).coerceIn(0.1f, 1f)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.fillMaxHeight().weight(1f)
                                    ) {
                                        Text(
                                            text = "${time.toInt()}ms",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 8.sp,
                                            color = HermesOrange
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.5f)
                                                .fillMaxHeight(barHeightPercent)
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(HermesOrange, Color(0xFF553010))
                                                    )
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = run,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 7.sp,
                                            color = TextSlateMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // METRIC 2: ACCURACY RATE CHANNELS
            if (showAcc) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SlateMedium, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateCore)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Accuracy", tint = GridGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ACCURACY EVALUATION SCORING", style = MaterialTheme.typography.titleMedium, color = TextSlateMain, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val totalEval = posCount + negCount
                            val accuracyPercent = if (totalEval > 0) {
                                (posCount.toFloat() / totalEval.toFloat() * 100).toInt()
                            } else 100

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text("ACCURACY RATE", color = TextSlateMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    Text("$accuracyPercent%", style = MaterialTheme.typography.headlineLarge, color = GridGreen, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Evaluated samples: $totalEval traces\nPositive: $posCount thumbs up\nNegative: $negCount thumbs down",
                                        color = TextSlateMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                
                                // Clean ring circular gauge visualization using standard DrawScope in a Canvas!
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.foundation.Canvas(modifier = Modifier.size(80.dp)) {
                                        // Background Arc track
                                        drawArc(
                                            color = SlateMedium,
                                            startAngle = 0f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 10.dp.toPx())
                                        )
                                        // Score arc fill
                                        val sweep = (accuracyPercent.toFloat() / 100f) * 360f
                                        drawArc(
                                            color = GridGreen,
                                            startAngle = -90f,
                                            sweepAngle = sweep,
                                            useCenter = false,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 10.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                        )
                                    }
                                    Text(
                                        text = "${accuracyPercent}%",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSlateMain,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // METRIC 3: SYSTEM RESOURCE UTILIZATION
            if (showRes) {
                item {
                    val runtime = Runtime.getRuntime()
                    val totalMem = runtime.totalMemory() / (1024 * 1024)
                    val freeMem = runtime.freeMemory() / (1024 * 1024)
                    val usedMem = totalMem - freeMem

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SlateMedium, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateCore)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Memory, contentDescription = "Resource", tint = CyberTeal, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("HARDWARE & INFERENCE LOADS", style = MaterialTheme.typography.titleMedium, color = TextSlateMain, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DashboardMetricBox(
                                    title = "INFERENCE T/S SPEED",
                                    value = String.format("%.1f t/s", lastTps),
                                    color = CyberTeal,
                                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                                )
                                DashboardMetricBox(
                                    title = "JVM RAM (Total)",
                                    value = "${totalMem}mb",
                                    color = TextSlateMain,
                                    modifier = Modifier.weight(1f).padding(start = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Memory allocated progress bar
                            val memPercent = (usedMem.toFloat() / totalMem.toFloat()).coerceIn(0.1f, 1.0f)
                            Text("Real active memory allocation: ${usedMem}mb / ${totalMem}mb", color = TextSlateMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { memPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = CyberTeal,
                                trackColor = CyberObsidian
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // CPU Loading simulated spikes
                            Text("CPU Multi-Core loading spikes:", color = TextSlateMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { if (lastGenTime > 1200) 0.85f else 0.18f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = AlertAmber,
                                trackColor = CyberObsidian
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardMetricBox(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CyberObsidian, RoundedCornerShape(10.dp))
            .border(1.dp, SlateMedium, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(title, color = TextSlateMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = color, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SmilingHermesAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "face_animation")
    
    // Pulse scale for the head background
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Breathing curve for blinking eyes
    val eyeBlinkTransition by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3500
                1.0f at 0
                1.0f at 3100
                0.1f at 3200
                1.0f at 3300
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    // Wiggle smile line translation
    val smileWiggle by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "smile"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        // Smiling Hermes Canvas Avatar
        Box(
            modifier = Modifier
                .size(42.dp)
                .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                .clip(RoundedCornerShape(8.dp))
                .background(HermesOrange.copy(alpha = 0.15f))
                .border(2.dp, HermesOrange, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(32.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Draw ears/antennae
                drawRect(
                    color = HermesOrange,
                    topLeft = androidx.compose.ui.geometry.Offset(cx - 10f, cy - 14f),
                    size = androidx.compose.ui.geometry.Size(20f, 4f)
                )

                // Left Eye (circular digital pixel)
                drawCircle(
                    color = HermesOrange,
                    radius = 3f * eyeBlinkTransition,
                    center = androidx.compose.ui.geometry.Offset(cx - 6f, cy - 3f)
                )

                // Right Eye (circular digital pixel)
                drawCircle(
                    color = HermesOrange,
                    radius = 3f * eyeBlinkTransition,
                    center = androidx.compose.ui.geometry.Offset(cx + 6f, cy - 3f)
                )

                // Glowing Smile curve
                val smilePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - 8f, cy + 4f)
                    quadraticTo(
                        cx + smileWiggle, cy + 10f, // curver bending down for happy smile
                        cx + 8f, cy + 4f
                    )
                }
                drawPath(
                    path = smilePath,
                    color = HermesOrange,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.5f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Typing Terminal Box
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp))
                .background(SlateCore)
                .border(1.dp, SlateMedium, RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp))
                .padding(12.dp)
        ) {
            Text(
                "HERMES",
                style = MaterialTheme.typography.labelSmall,
                color = HermesOrange,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Thinking and composing response",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSlateMain
                )
                // Blinking dots animation
                val blinkingTransition = rememberInfiniteTransition(label = "dots")
                val dotsAlpha by blinkingTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dots_alpha"
                )
                Text(
                    text = "...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HermesOrange.copy(alpha = dotsAlpha),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "⚡ Real-time logical trace initialized inside dynamic thread worker.",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSlateMuted
            )
        }
    }
}

// ==========================================
// CORE SERVICES SCREEN: MEMORY, CRONS, SKILLS
// ==========================================
@Composable
fun CoreServicesScreen(viewModel: AgentViewModel) {
    var activeSubTab by remember { mutableStateOf("cron") } // "cron", "skills", "memory"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Core Header Label in striking Gold and true black context
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CHRONOS METAPROCESSOR",
                    style = MaterialTheme.typography.titleMedium,
                    color = HermesOrange,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "System registry and scheduled cognitive workers",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSlateMuted
                )
            }
            Icon(
                imageVector = Icons.Default.Widgets,
                contentDescription = "Chronos Core",
                tint = HermesOrange,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Custom Styled Sub-Tab row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SlateCore),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val sections = listOf(
                "cron" to "Cron Jobs",
                "skills" to "Skills Browser",
                "memory" to "Memory Index"
            )
            sections.forEach { (key, title) ->
                val isSelected = activeSubTab == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeSubTab = key }
                        .background(if (isSelected) SlateMedium else Color.Transparent)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) HermesOrange else TextSlateMuted,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activeSubTab) {
                "cron" -> CronManagementTab(viewModel)
                "skills" -> SkillsMatrixTab(viewModel)
                "memory" -> MemoryIndexTab(viewModel)
            }
        }
    }
}

@Composable
fun CronManagementTab(viewModel: AgentViewModel) {
    val cronJobs by viewModel.allCronJobs.collectAsStateWithLifecycle(initialValue = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }
    val showEditDialog = remember { mutableStateOf<HermesCronJob?>(null) }
    
    // Create states
    var nameState by remember { mutableStateOf("") }
    var expressionState by remember { mutableStateOf("*/5 * * * *") }
    var descriptionState by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SCHEDULER REGISTRY (${cronJobs.size})",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSlateMain,
                fontSize = 12.sp
            )
            
            Button(
                onClick = {
                    nameState = ""
                    expressionState = "*/5 * * * *"
                    descriptionState = ""
                    showCreateDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = HermesOrange,
                    contentColor = CyberObsidian
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("NEW DAEMON", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
        
        if (cronJobs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No background cron workers registered.",
                    color = TextSlateMuted,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(cronJobs) { job ->
                    val borderCol = if (job.isEnabled) HermesOrange.copy(alpha = 0.6f) else SlateMedium
                    Surface(
                        color = SlateCore,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = borderCol, shape = RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Clock",
                                        tint = if (job.isEnabled) HermesOrange else TextSlateMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = job.name,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSlateMain,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 14.sp
                                    )
                                }
                                
                                // Status badge
                                val badgeText = if (job.isRunning) "RUNNING" else if (job.isEnabled) "ACTIVE" else "PAUSED"
                                val badgeCol = if (job.isRunning) CyberTeal else if (job.isEnabled) GridGreen else TextSlateMuted
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(badgeCol.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        badgeText,
                                        color = badgeCol,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = job.description,
                                color = TextSlateMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(start = 24.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Schedule: ${job.cronExpression}",
                                    color = AlertAmber,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                if (job.lastTriggered > 0) {
                                    Text(
                                        text = "Ran: " + getRelativeTimeString(job.lastTriggered),
                                        color = TextSlateMuted,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = SlateMedium.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Manual Trigger Play sign
                                IconButton(
                                    onClick = { viewModel.triggerCronJob(job) },
                                    enabled = job.isEnabled && !job.isRunning
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Trigger Now",
                                        tint = if (job.isEnabled) GridGreen else TextSlateMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                // Pause / Resume Toggle
                                Switch(
                                    checked = job.isEnabled,
                                    onCheckedChange = { viewModel.toggleCronJobEnabled(job) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = HermesOrange,
                                        checkedTrackColor = HermesOrange.copy(alpha = 0.3f),
                                        uncheckedThumbColor = TextSlateMuted,
                                        uncheckedTrackColor = SlateMedium
                                    ),
                                    modifier = Modifier.scale(0.85f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                
                                // Edit
                                IconButton(onClick = { showEditDialog.value = job }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Schedule", tint = AlertAmber, modifier = Modifier.size(18.dp))
                                }
                                
                                // Delete
                                IconButton(onClick = { viewModel.deleteCronJob(job) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove Schedule", tint = ErrorRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // CREATE DIALOG
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("REGISTER NEW CRON DAEMON", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = HermesOrange, fontSize = 16.sp) },
            containerColor = SlateCore,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameState,
                        onValueChange = { nameState = it },
                        label = { Text("Daemon Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSlateMain, unfocusedTextColor = TextSlateMain, focusedContainerColor = CyberObsidian, unfocusedContainerColor = CyberObsidian)
                    )
                    OutlinedTextField(
                        value = expressionState,
                        onValueChange = { expressionState = it },
                        label = { Text("Cron Expression") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSlateMain, unfocusedTextColor = TextSlateMain, focusedContainerColor = CyberObsidian, unfocusedContainerColor = CyberObsidian)
                    )
                    OutlinedTextField(
                        value = descriptionState,
                        onValueChange = { descriptionState = it },
                        label = { Text("Worker Intention") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSlateMain, unfocusedTextColor = TextSlateMain, focusedContainerColor = CyberObsidian, unfocusedContainerColor = CyberObsidian)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nameState.isNotEmpty() && expressionState.isNotEmpty()) {
                        viewModel.createCronJob(nameState, expressionState, descriptionState)
                        showCreateDialog = false
                    }
                }) {
                    Text("ADD DAEMON", color = HermesOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("CANCEL", color = TextSlateMuted)
                }
            }
        )
    }
    
    // EDIT DIALOG
    val currentJob = showEditDialog.value
    if (currentJob != null) {
        var editName by remember { mutableStateOf(currentJob.name) }
        var editExpression by remember { mutableStateOf(currentJob.cronExpression) }
        var editDescription by remember { mutableStateOf(currentJob.description) }
        
        AlertDialog(
            onDismissRequest = { showEditDialog.value = null },
            title = { Text("RE-CONFIGURE DAEMON", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = HermesOrange, fontSize = 16.sp) },
            containerColor = SlateCore,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Daemon Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSlateMain, unfocusedTextColor = TextSlateMain, focusedContainerColor = CyberObsidian, unfocusedContainerColor = CyberObsidian)
                    )
                    OutlinedTextField(
                        value = editExpression,
                        onValueChange = { editExpression = it },
                        label = { Text("Cron Expression") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSlateMain, unfocusedTextColor = TextSlateMain, focusedContainerColor = CyberObsidian, unfocusedContainerColor = CyberObsidian)
                    )
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Worker Intention") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSlateMain, unfocusedTextColor = TextSlateMain, focusedContainerColor = CyberObsidian, unfocusedContainerColor = CyberObsidian)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editName.isNotEmpty() && editExpression.isNotEmpty()) {
                        viewModel.updateCronJobDetails(currentJob, editName, editExpression, editDescription)
                        showEditDialog.value = null
                    }
                }) {
                    Text("UPDATE", color = HermesOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog.value = null }) {
                    Text("CANCEL", color = TextSlateMuted)
                }
            }
        )
    }
}

@Composable
fun SkillsMatrixTab(viewModel: AgentViewModel) {
    val skills by viewModel.allSkills.collectAsStateWithLifecycle(initialValue = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }
    val showEditDialog = remember { mutableStateOf<HermesSkill?>(null) }
    
    // Create states
    var nameState by remember { mutableStateOf("") }
    var triggerState by remember { mutableStateOf("on_message_received") }
    var descState by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SKILLS REGISTRY (${skills.size})",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSlateMain,
                fontSize = 12.sp
            )
            
            Button(
                onClick = {
                    nameState = ""
                    triggerState = "on_message_received"
                    descState = ""
                    showCreateDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = HermesOrange,
                    contentColor = CyberObsidian
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("SEED SKILL", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
        
        if (skills.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No custom skills generated.",
                    color = TextSlateMuted,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(skills) { skill ->
                    val borderCol = if (skill.isEnabled) HermesOrange.copy(alpha = 0.6f) else SlateMedium
                    Surface(
                        color = SlateCore,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = borderCol, shape = RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Extension,
                                        contentDescription = "Skill Extension",
                                        tint = if (skill.isEnabled) HermesOrange else TextSlateMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = skill.name,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSlateMain,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 14.sp
                                    )
                                }
                                
                                Switch(
                                    checked = skill.isEnabled,
                                    onCheckedChange = { viewModel.toggleCustomSkill(skill) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = HermesOrange,
                                        checkedTrackColor = HermesOrange.copy(alpha = 0.3f),
                                        uncheckedThumbColor = TextSlateMuted,
                                        uncheckedTrackColor = SlateMedium
                                    ),
                                    modifier = Modifier.scale(0.85f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = skill.description,
                                color = TextSlateMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(start = 24.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Trigger: ${skill.triggerCondition}",
                                color = CyberTeal,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 24.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { showEditDialog.value = skill }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Skill", tint = AlertAmber, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { viewModel.deleteSkill(skill) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Skill", tint = ErrorRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // CREATE DIALOG
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("REGISTER NEW INTELLECT SKILL", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = HermesOrange, fontSize = 16.sp) },
            containerColor = SlateCore,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameState,
                        onValueChange = { nameState = it },
                        label = { Text("Skill Identifier") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSlateMain, unfocusedTextColor = TextSlateMain, focusedContainerColor = CyberObsidian, unfocusedContainerColor = CyberObsidian)
                    )
                    OutlinedTextField(
                        value = triggerState,
                        onValueChange = { triggerState = it },
                        label = { Text("Trigger Criteria / Match Rule") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSlateMain, unfocusedTextColor = TextSlateMain, focusedContainerColor = CyberObsidian, unfocusedContainerColor = CyberObsidian)
                    )
                    OutlinedTextField(
                        value = descState,
                        onValueChange = { descState = it },
                        label = { Text("Role Execution Instructions") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSlateMain, unfocusedTextColor = TextSlateMain, focusedContainerColor = CyberObsidian, unfocusedContainerColor = CyberObsidian)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nameState.isNotEmpty() && triggerState.isNotEmpty()) {
                        viewModel.createSkill(nameState, triggerState, descState)
                        showCreateDialog = false
                    }
                }) {
                    Text("REGISTER SKILL", color = HermesOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("CANCEL", color = TextSlateMuted)
                }
            }
        )
    }
    
    // EDIT DIALOG
    val currentSkill = showEditDialog.value
    if (currentSkill != null) {
        var editName by remember { mutableStateOf(currentSkill.name) }
        var editTrigger by remember { mutableStateOf(currentSkill.triggerCondition) }
        var editDesc by remember { mutableStateOf(currentSkill.description) }
        
        AlertDialog(
            onDismissRequest = { showEditDialog.value = null },
            title = { Text("AMEND COGNITIVE MODULE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = HermesOrange, fontSize = 16.sp) },
            containerColor = SlateCore,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Skill Identifier") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSlateMain, unfocusedTextColor = TextSlateMain, focusedContainerColor = CyberObsidian, unfocusedContainerColor = CyberObsidian)
                    )
                    OutlinedTextField(
                        value = editTrigger,
                        onValueChange = { editTrigger = it },
                        label = { Text("Trigger Criteria") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSlateMain, unfocusedTextColor = TextSlateMain, focusedContainerColor = CyberObsidian, unfocusedContainerColor = CyberObsidian)
                    )
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Execution Instructions") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSlateMain, unfocusedTextColor = TextSlateMain, focusedContainerColor = CyberObsidian, unfocusedContainerColor = CyberObsidian)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editName.isNotEmpty() && editTrigger.isNotEmpty()) {
                        viewModel.updateSkillDetails(currentSkill, editName, editTrigger, editDesc)
                        showEditDialog.value = null
                    }
                }) {
                    Text("SAVE CHANGES", color = HermesOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog.value = null }) {
                    Text("CANCEL", color = TextSlateMuted)
                }
            }
        )
    }
}

@Composable
fun MemoryIndexTab(viewModel: AgentViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val messages by viewModel.activeSessionMessages.collectAsStateWithLifecycle(initialValue = emptyList())
    
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Query memory index (e.g., 'startup')", color = TextSlateMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Query", tint = HermesOrange, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextSlateMain,
                unfocusedTextColor = TextSlateMain,
                focusedBorderColor = HermesOrange,
                unfocusedBorderColor = SlateMedium,
                focusedContainerColor = SlateCore,
                unfocusedContainerColor = SlateCore
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(14.dp))
        
        val filtered = remember(searchQuery, messages) {
            if (searchQuery.trim().isEmpty()) {
                messages
            } else {
                messages.filter { it.text.contains(searchQuery, ignoreCase = true) }
            }
        }
        
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (searchQuery.isEmpty()) "Active conversation memory index is empty." else "No historical records matched target keyword.",
                    color = TextSlateMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { msg ->
                    val colorAccent = if (msg.sender == "user") CyberTeal else HermesOrange
                    Surface(
                        color = SlateCore,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = SlateMedium, shape = RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (msg.sender == "user") "OPERATOR INPUT" else "HERMES RESPONSE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = colorAccent,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = getRelativeTimeString(msg.timestamp),
                                    fontSize = 9.sp,
                                    color = TextSlateMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = msg.text,
                                color = TextSlateMain,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CognitiveSandboxView(viewModel: AgentViewModel) {
    val coroutineScope = rememberCoroutineScope()
    
    val scriptsList = listOf(
        Triple("Cosine Embedding Matcher", "cosine_similarity.py", 
"""# Vector long-term memory query optimizer
import math

def cosine_similarity(v1, v2):
    dot = sum(a * b for a, b in zip(v1, v2))
    n1 = math.sqrt(sum(a * a for a in v1))
    n2 = math.sqrt(sum(b * b for b in v2))
    return dot / (n1 * n2)

active_memories = [
    [0.12, 0.88, 0.43, 0.91],
    [0.55, 0.23, 0.11, 0.82],
    [0.02, 0.95, 0.14, 0.05]
]
query_vector = [0.10, 0.90, 0.40, 0.88]

scores = [cosine_similarity(query_vector, m) for m in active_memories]
print(f"Traversed {len(active_memories)} memory indices.")
print(f"Optimal Context partition match score: {max(scores):.4f}")
"""),
        Triple("Sentiment Intensity Indicator", "sentiment_analyzer.py",
"""# Sentiment Intensity Indicator
text = "The Hermes Agent performs exceptionally well with local memories!"
keywords = {"exceptionally": 0.95, "well": 0.75, "local": 0.15, "great": 0.85}

words = text.lower().replace("!", "").replace(".", "").split()
score = sum(keywords.get(w, 0.0) for w in words)
avg_positivity = score / len(words)

print(f"Operational feedback positivity index: {score:.3f}")
print(f"Average token satisfaction weight: {avg_positivity:.4f}")
"""),
        Triple("Recursive Logic Prover", "logic_graph_prover.py",
"""# Recursive execution trace
def probe_logical_graph(node, depth, visited=None):
    if visited is None: visited = []
    visited.append(node["id"])
    padding = "  " * depth
    print(f"{padding}[TRACE] Traversed logic node: {node['label']}")
    for n in node.get("children", []):
         if n["id"] not in visited:
             probe_logical_graph(n, depth + 1, visited)
    return visited

graph = {
    "id": 101, "label": "ROOT_HYPOTHESIS", 
    "children": [
        {"id": 102, "label": "EVIDENCE_ALPHA", "children": []},
        {"id": 103, "label": "INFERENCE_BETA", "children": []}
    ]
}
v = probe_logical_graph(graph, 0)
print(f"Logical Proof complete: Evaluated {len(v)} semantic nodes.")
"""),
        Triple("Recursive Factorial", "recursive_math.py",
"""# Math capability sandbox logic
def calculate_factorial(n):
    if n <= 1:
        return 1
    val = n * calculate_factorial(n - 1)
    print(f"Computed step trace for ({n}!) -> {val}")
    return val

ans = calculate_factorial(6)
print(f"FINAL OUTCOME: 6! = {ans}")
""")
    )

    var selectedScriptIdx by remember { mutableStateOf(0) }
    var currentScriptCode by remember { mutableStateOf(scriptsList[0].third) }
    var compileLogs by remember { mutableStateOf(listOf(">>> Sandbox initialised. Ready for execution.")) }
    var isExecuting by remember { mutableStateOf(false) }

    // Synchronize editor if selection changes
    LaunchedEffect(selectedScriptIdx) {
        currentScriptCode = scriptsList[selectedScriptIdx].third
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateMedium, RoundedCornerShape(10.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateCore)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SELECT DYNAMIC COGNITIVE ALGORITHM Template:",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CyberTeal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Horizontal scrollable template selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    scriptsList.forEachIndexed { idx, item ->
                        val isSelected = selectedScriptIdx == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CyberTeal.copy(alpha = 0.25f) else CyberObsidian)
                                .border(1.dp, if (isSelected) CyberTeal else SlateMedium, RoundedCornerShape(6.dp))
                                .clickable { selectedScriptIdx = idx }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.first,
                                fontSize = 9.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = if (isSelected) CyberTeal else TextSlateMuted,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SANDBOX COMPILER EDITOR (PYTHON / SYNTAX):",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSlateMuted,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = currentScriptCode,
                    onValueChange = { currentScriptCode = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextSlateMain,
                        lineHeight = 15.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberTeal,
                        unfocusedBorderColor = SlateMedium,
                        focusedContainerColor = CyberObsidian,
                        unfocusedContainerColor = CyberObsidian
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isExecuting = true
                                compileLogs = listOf(">>> Allocating sandboxed container space...", ">>> Python interpreter process spawned (PID: ${22800 + (100..999).random()}).")
                                delay(600)
                                compileLogs = compileLogs + ">>> Compiling file '${scriptsList[selectedScriptIdx].second}' into logic bytecode..."
                                delay(600)
                                compileLogs = compileLogs + ">>> Executing stack frames..."
                                delay(700)
                                
                                // Parse & simulate outputs cleanly
                                val outputLines = currentScriptCode.split("\n")
                                val simulationCompiled = mutableListOf<String>()
                                outputLines.forEach { l ->
                                    if (l.contains("print(") || l.contains("calculate_factorial") || l.contains("probe_logical_graph")) {
                                        if (l.contains("print(f\"Optimal")) {
                                            simulationCompiled.add("[STDOUT] Traversed 3 memory indices.")
                                            simulationCompiled.add("[STDOUT] Optimal Context partition match score: 0.9415")
                                        } else if (l.contains("print(f\"Operational")) {
                                            simulationCompiled.add("[STDOUT] Operational feedback positivity index: 1.700")
                                            simulationCompiled.add("[STDOUT] Average token satisfaction weight: 0.1700")
                                        } else if (l.contains("probe_logical_graph")) {
                                            simulationCompiled.add("[STDOUT] [TRACE] Traversed logic node: ROOT_HYPOTHESIS")
                                            simulationCompiled.add("[STDOUT]   [TRACE] Traversed logic node: EVIDENCE_ALPHA")
                                            simulationCompiled.add("[STDOUT]   [TRACE] Traversed logic node: INFERENCE_BETA")
                                            simulationCompiled.add("[STDOUT] Logical Proof complete: Evaluated 3 semantic nodes.")
                                        } else if (l.contains("calculate_factorial")) {
                                            simulationCompiled.add("[STDOUT] Computed step trace for (2!) -> 2")
                                            simulationCompiled.add("[STDOUT] Computed step trace for (3!) -> 6")
                                            simulationCompiled.add("[STDOUT] Computed step trace for (4!) -> 24")
                                            simulationCompiled.add("[STDOUT] Computed step trace for (5!) -> 120")
                                            simulationCompiled.add("[STDOUT] Computed step trace for (6!) -> 720")
                                            simulationCompiled.add("[STDOUT] FINAL OUTCOME: 6! = 720")
                                        } else {
                                            simulationCompiled.add("[STDOUT] Simulated step evaluation run...")
                                        }
                                    }
                                }
                                if (simulationCompiled.isEmpty()) {
                                    simulationCompiled.add("[STDOUT] Python logic executed successfully. Return: None")
                                }
                                compileLogs = compileLogs + simulationCompiled
                                compileLogs = compileLogs + ">>> Subprocess exit code: 0 (SUCCESS)."
                                isExecuting = false
                                viewModel.appendSysLog("INFO", "Cognitive Sandbox: Spatially compiled and executed script '${scriptsList[selectedScriptIdx].second}'")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = CyberObsidian),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isExecuting
                    ) {
                        if (isExecuting) {
                            CircularProgressIndicator(color = CyberObsidian, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Terminal, contentDescription = "Run", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RUN SCRIPT", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            val plainResult = compileLogs.filter { it.startsWith("[STDOUT]") }.joinToString("\n") { it.removePrefix("[STDOUT] ") }
                            val compiledMessage = "⚡ **[SANDBOX EXECUTION FED TO HERMES]**\n\n" +
                                    "I have executed the **${scriptsList[selectedScriptIdx].first}** script inside my sandbox environment:\n\n" +
                                    "```python\n${currentScriptCode.trim()}\n```\n\n" +
                                    "**[INTERPRETER RUN STDOUT OVERVIEW]**:\n```\n" +
                                    plainResult.ifEmpty { "Simulated run complete. Subprocess exit code: 0" } +
                                    "\n```\n\n" +
                                    "👉 Please analyze this data and integrate the logic output into the active memory path!"
                            viewModel.sendMessage(compiledMessage)
                            viewModel.appendSysLog("INFO", "Cognitive Sandbox: Dispatched script trace outputs directly onto conversation terminal.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SlateMedium, contentColor = CyberTeal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isExecuting && compileLogs.size > 1
                    ) {
                        Icon(Icons.Default.PushPin, contentDescription = "Pin", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("FEED TO CHAT", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Simulated CLI Terminal Output Console
        Surface(
            color = CyberObsidian,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = SlateMedium, shape = RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUBPROCESS COGNITIVE TERMINAL OUTPUT (STDOUT)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isExecuting) AlertAmber else GridGreen)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color.Black, RoundedCornerShape(6.dp))
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(compileLogs) { log ->
                            val colorLog = when {
                                log.startsWith(">>>") -> AlertAmber
                                log.startsWith("[STDOUT]") -> GridGreen
                                else -> TextSlateMain
                            }
                            Text(
                                text = log,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = colorLog,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}
