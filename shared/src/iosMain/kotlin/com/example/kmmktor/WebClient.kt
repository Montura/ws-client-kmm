package com.example.kmmktor

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*


actual class CallBackHandler actual constructor(webClient: WebClient) {

    actual suspend fun onMessage(msg: String) {
    }

    actual suspend fun onHandshake(map: HashMap<String, Any>) {
    }

    actual suspend fun sendMessage(messageSupplier: () -> String) {
    }

    actual fun onConnect(json: HashMap<String, Any>) {
    }

    actual val webClient: WebClient
        get() = TODO("Not yet implemented")

}
actual class WebClient {
    var session: WebSocketSession? = null
    var clientId: String? = null

    val clientKt: HttpClient = HttpClient(CIO) {
        install(WebSockets)
        engine {
            requestTimeout = 0
        }
    }

    actual fun run(
        host: String,
        port: Int?,
        path: String?,
        onInit: suspend DefaultClientWebSocketSession.(String?, suspend () -> Unit) -> Unit
    ) {
    }


}