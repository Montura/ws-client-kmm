package com.example.kmmktor

import io.ktor.client.*
import io.ktor.websocket.*

actual class WebClient {
    actual var session: WebSocketSession? = null
    actual var clientId: String? = null
    actual var handshakeCompleted: Boolean = false
    actual var connectionEstablished: Boolean = false
    actual val clientKt: HttpClient = HttpClient {  }
}