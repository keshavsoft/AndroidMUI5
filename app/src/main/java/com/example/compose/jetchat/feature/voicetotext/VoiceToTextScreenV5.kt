package com.example.compose.jetchat.feature.voicetotext

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceToTextScreenV5(onBack: () -> Unit = {}) {

    val context = LocalContext.current
    val activity = context as? Activity

    var isListening by remember { mutableStateOf(false) }
    var partialText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Tap the mic and start speaking") }

    var bubbles by remember { mutableStateOf(listOf<VoiceBubble>()) }

    // State for menu + edit dialog
    var menuForBubble by remember { mutableStateOf<VoiceBubble?>(null) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    var editingBubble by remember { mutableStateOf<VoiceBubble?>(null) }
    var editText by remember { mutableStateOf("") }

    // Speech recognizer
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    DisposableEffect(Unit) {
        onDispose { speechRecognizer?.destroy() }
    }

    fun addBubbleFromText(text: String) {
        if (text.isBlank()) return
        bubbles = bubbles + VoiceBubble(text = text)
    }

    fun startListening() {
        if (speechRecognizer == null) {
            status = "Speech recognition not available on this device"
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                status = "Listening…"
                partialText = ""
            }

            override fun onBeginningOfSpeech() {
                status = "Speak now"
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                status = "Finishing…"
            }

            override fun onError(error: Int) {
                isListening = false
                partialText = ""
                status = "Tap the mic and start speaking"
            }

            override fun onResults(resultsBundle: Bundle?) {
                isListening = false
                val matches =
                    resultsBundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.joinToString(" ") ?: ""
                addBubbleFromText(text)
                partialText = ""
                status = "Tap the mic and start speaking"
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial =
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                partialText = partial?.firstOrNull() ?: ""
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
        isListening = true
        status = "Preparing mic…"
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        partialText = ""
        status = "Tap the mic and start speaking"
    }

    fun ensureAudioPermissionAndStart() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            if (activity == null) {
                status = "Cannot request mic permission"
                return
            }

            status = "Requesting mic permission…"

            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1001
            )
            return
        }

        startListening()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Voice to Text – V4") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (bubbles.isNotEmpty() || partialText.isNotBlank()) {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE)
                                        as ClipboardManager
                            val fullText = buildString {
                                bubbles.forEach { appendLine(it.text) }
                                if (partialText.isNotBlank()) {
                                    appendLine(partialText)
                                }
                            }
                            clipboard.setPrimaryClip(
                                ClipData.newPlainText("VoiceTextV4", fullText)
                            )
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy all"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isListening) stopListening() else ensureAudioPermissionAndStart()
                }
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Mic"
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
        ) {

            // Status line
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            // Bubbles list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(bubbles, key = { _, item -> item.id }) { _, bubble ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // Bubble itself
                        ChatBubble(text = bubble.text)

                        // Three-dot menu
                        IconButton(
                            onClick = {
                                menuForBubble = bubble
                                editText = bubble.text
                                isMenuExpanded = true
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Options"
                            )
                        }
                    }
                }

                // Live partial text as fading bubble at bottom
                if (partialText.isNotBlank()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            ChatBubble(text = partialText, isLive = true)
                        }
                    }
                }
            }

            // Bottom actions row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    bubbles = emptyList()
                    partialText = ""
                }) {
                    Text("Clear")
                }

                Text(
                    text = if (isListening) "Listening…" else "Idle",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isListening)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Dropdown menu for a specific bubble
        DropdownMenu(
            expanded = isMenuExpanded && menuForBubble != null,
            onDismissRequest = { isMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    isMenuExpanded = false
                    editingBubble = menuForBubble
                    editText = menuForBubble?.text.orEmpty()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    val target = menuForBubble
                    if (target != null) {
                        bubbles = bubbles.filterNot { it.id == target.id }
                    }
                    isMenuExpanded = false
                }
            )
        }

        // Edit dialog
        if (editingBubble != null) {
            AlertDialog(
                onDismissRequest = { editingBubble = null },
                title = { Text("Edit text") },
                text = {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val target = editingBubble
                        if (target != null) {
                            bubbles = bubbles.map {
                                if (it.id == target.id) it.copy(text = editText)
                                else it
                            }
                        }
                        editingBubble = null
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingBubble = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ChatBubble(
    text: String,
    isLive: Boolean = false
) {
    val background: Color
    val contentColor: Color

    if (isLive) {
        background = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        contentColor = MaterialTheme.colorScheme.onSurface
    } else {
        background = MaterialTheme.colorScheme.primary
        contentColor = MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = Modifier
            .padding(start = 48.dp, end = 4.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 0.dp
                )
            )
            .background(background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VoiceToTextScreenV5Preview() {
    MaterialTheme {
        VoiceToTextScreenV5()
    }
}
