package com.example.kmmktor

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import platform.Foundation.NSThread

// todo: Address list
//  - wss://tools.dxfeed.com/webservice/cometd
//  - ws://localhost:8080/dxfeed-webservice/cometd - Quote AAPL

actual class CallbackHandler {
    fun onSubscribe() {
        println("USER_HANDLER: onSubscribe")
    }
}

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

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    logWithThreadName("Run WebClientKt ...")

    val webClient = WebClient(httpClient())

    GlobalScope.launch(Dispatchers.Default) {
        webClient.run(WebClientUtil.HOST, WebClientUtil.PORT, WebClientUtil.PATH)
    }

    while (true) {}
}