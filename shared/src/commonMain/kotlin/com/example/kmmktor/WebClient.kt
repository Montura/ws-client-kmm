package com.example.kmmktor

import io.ktor.client.*
import io.ktor.websocket.*

expect class WebClient {
    var session: WebSocketSession?
    var clientId: String?
    var handshakeCompleted: Boolean
    var connectionEstablished: Boolean
    val clientKt: HttpClient
}