package com.example.compose.jetchat.feature.voicetotext

import android.util.Log
import okhttp3.*

/**
 * Very small WebSocket client used by VoiceToTextScreenV6.
 * Call connect() once when screen enters, close() when leaving,
 * and sendFinal / sendPartial for messages.
 */
object VoiceWsClient {

    private const val TAG = "VoiceWsClient"

    // TODO: change this to your WebSocket URL
    private const val WS_URL = "wss://keshavsoft.com/"

    private val client by lazy { OkHttpClient() }

    private var webSocket: WebSocket? = null

    private val request: Request by lazy {
        Request.Builder()
            .url(WS_URL)
            .build()
    }

    private val listener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket opened")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "WebSocket message from server: $text")
            // If you want to update UI with this, expose a Flow/State and collect in Compose.
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closing: $code $reason")
            webSocket.close(code, reason)
        }

        override fun onFailure(
            webSocket: WebSocket,
            t: Throwable,
            response: Response?
        ) {
            Log.e(TAG, "WebSocket failure", t)
        }
    }

    fun connect() {
        if (webSocket != null) return // already open or opening
        Log.d(TAG, "Connecting to $WS_URL")
        webSocket = client.newWebSocket(request, listener)
    }

    fun close() {
        webSocket?.close(1000, "Closing from client")
        webSocket = null
    }

    fun sendFinal(text: String) {
        if (text.isBlank()) return
        val msg = "FINAL:$text"
        val ok = webSocket?.send(msg) ?: false
        Log.d(TAG, "sendFinal ok=$ok msg=$msg")
    }

    fun sendPartial(text: String) {
        if (text.isBlank()) return
        val msg = "PARTIAL:$text"
        val ok = webSocket?.send(msg) ?: false
        Log.d(TAG, "sendPartial ok=$ok msg=$msg")
    }
}
