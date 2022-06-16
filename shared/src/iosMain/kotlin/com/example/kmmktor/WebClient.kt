package com.example.kmmktor

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.websocket.*
import platform.Foundation.NSThread

actual fun httpClient(): HttpClient {
    return HttpClient(Darwin) {
        install(WebSockets)
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
    }
}

// OnWebSocketClose   -> log("Client closed: " + statusCode + " - " + reason);  this.session = null;
// OnWebSocketConnect -> First connection + send handshake
// OnWebSocketMessage -> Handshake
// OnWebSocketError   -> log

actual fun logWithThreadName(msg: String?) {
    println("[${NSThread.currentThread()}]: $msg")
}

fun main() {
    logWithThreadName("Run iOS WebClientKt ...")
}