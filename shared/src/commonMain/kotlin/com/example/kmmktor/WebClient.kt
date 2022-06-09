package com.example.kmmktor

import io.ktor.client.*
import io.ktor.websocket.*

expect class WebClient {
    var session: WebSocketSession?
    var clientId: String?
    val clientKt: HttpClient
}