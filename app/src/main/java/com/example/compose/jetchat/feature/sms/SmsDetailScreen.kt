package com.example.compose.jetchat.feature.sms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsDetailScreen(
    mobile: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var messages by remember { mutableStateOf<List<SmsMessage>>(emptyList()) }

    LaunchedEffect(mobile) {
        messages = SmsRepository.getMessagesForAddress(context, mobile)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = mobile) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "No messages with this contact",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.timestamp }) { msg ->
                    SmsBubble(msg)
                }
            }
        }
    }
}

@Composable
private fun SmsBubble(message: SmsMessage) {
    val isSent = message.isSent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
    ) {
        // Different bubble color for sent vs received
        val bubbleColor = if (isSent) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }

        val textColor = if (isSent) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }

        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp,
            color = bubbleColor,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(
                    start = if (isSent) 40.dp else 0.dp,
                    end = if (isSent) 0.dp else 40.dp
                )
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = message.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDetailTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun formatDetailTime(ts: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(ts))
}
