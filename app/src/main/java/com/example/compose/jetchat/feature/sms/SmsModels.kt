package com.example.compose.jetchat.feature.sms

data class SmsGroup(
    val mobile: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val messageCount: Int
)
