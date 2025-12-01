package com.example.compose.jetchat.feature.sms

import android.content.Context
import android.net.Uri

object SmsRepository {

    fun getGroupedSms(context: Context): List<SmsGroup> {
        val smsMap = mutableMapOf<String, MutableList<Pair<String, Long>>>()

        val cursor = context.contentResolver.query(
            Uri.parse("content://sms/inbox"),
            arrayOf("address", "body", "date"),
            null,
            null,
            "date DESC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val address = it.getString(it.getColumnIndexOrThrow("address"))
                val body = it.getString(it.getColumnIndexOrThrow("body"))
                val date = it.getLong(it.getColumnIndexOrThrow("date"))

                val list = smsMap.getOrPut(address) { mutableListOf() }
                list.add(body to date)
            }
        }

        return smsMap.map { (mobile, messages) ->
            val last = messages.firstOrNull()
            SmsGroup(
                mobile = mobile,
                lastMessage = last?.first ?: "",
                lastTimestamp = last?.second ?: 0L,
                messageCount = messages.size
            )
        }.sortedByDescending { it.lastTimestamp }
    }

    // ⭐ NEW: all messages for a specific address (conversation)
    fun getMessagesForAddress(context: Context, address: String): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()

        // Using content://sms to include inbox (and optionally others)
        val cursor = context.contentResolver.query(
            Uri.parse("content://sms"),
            arrayOf("address", "body", "date"),
            "address = ?",
            arrayOf(address),
            "date ASC" // oldest first -> nice chat flow
        )

        cursor?.use {
            while (it.moveToNext()) {
                val addr = it.getString(it.getColumnIndexOrThrow("address"))
                val body = it.getString(it.getColumnIndexOrThrow("body"))
                val date = it.getLong(it.getColumnIndexOrThrow("date"))

                messages.add(
                    SmsMessage(
                        address = addr,
                        body = body,
                        timestamp = date
                    )
                )
            }
        }

        return messages
    }
}
