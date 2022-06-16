package com.example.kmmktor

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// todo: Address list
//  - wss://tools.dxfeed.com/webservice/cometd
//  - ws://localhost:8080/dxfeed-webservice/cometd - Quote AAPL

actual fun httpClient(): HttpClient {
    return HttpClient(CIO) {
        install(WebSockets)
        engine {
            requestTimeout = 0
        }
    }
}

actual fun logWithThreadName(msg: String?) {
    println("[${Thread.currentThread().name}]: $msg")
}

class MySub(eventTypes: List<String>) : Subscription(eventTypes) {
    override fun onRawData(data: RawData) {
        logWithThreadName("USER_HANDLER: got raw data:\n\t" + data.json)
    }
}

fun main() {
    logWithThreadName("Run WebClientKt ...")

    DxFeedApi.init(httpClient())

    val eventTypes = listOf("Quote")
    val sub = DxFeedApi.createSubscription(eventTypes) { MySub(eventTypes) }
    sub.addSymbols(listOf("AAPL"))
    DxFeedApi.subscribe(sub)
    DxFeedApi.subscribe(sub)

    while (true) {}
}