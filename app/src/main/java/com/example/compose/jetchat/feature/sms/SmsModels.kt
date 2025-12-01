package com.example.compose.jetchat.feature.sms

data class SmsGroup(
    val mobile: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val messageCount: Int
)

// For detail screen (conversation style)
data class SmsMessage(
    val address: String,
    val body: String,
    val timestamp: Long
    // you can add "type" later if you want (inbox/sent)
)
