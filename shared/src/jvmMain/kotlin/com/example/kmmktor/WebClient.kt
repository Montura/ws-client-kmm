package com.example.kmmktor

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// todo: Address list
//  - wss://tools.dxfeed.com/webservice/cometd
//  - ws://localhost:8080/dxfeed-webservice/cometd - Quote AAPL

actual class CallbackHandler {
    fun onSubscribe() {
        println("USER_HANDLER: onSubscribe")
    }
}

// OnWebSocketClose   -> log("Client closed: " + statusCode + " - " + reason);  this.session = null;
// OnWebSocketConnect -> First connection + send handshake
// OnWebSocketMessage -> Handshake
// OnWebSocketError   -> log

actual fun logWithThreadName(msg: String?) {
    println("[${Thread.currentThread().name}]: $msg")
}

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    logWithThreadName("Run WebClientKt ...")

    val webClient = WebClient()

    GlobalScope.launch(Dispatchers.Default) {
        webClient.run(WebClientUtil.HOST, WebClientUtil.PORT, WebClientUtil.PATH)
    }

    while (true) {}
}