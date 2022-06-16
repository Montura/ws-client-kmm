package com.example.kmmktor

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.*

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

class MySubscriptionImpl : SubscriptionImpl() {
    override fun onRawData(data: RawData) {
        logWithThreadName("USER_HANDLER: got raw data:\n\t" + data.json)
    }
}

fun main() {
    logWithThreadName("Run WebClientKt ...")

    val api = DxFeedApi(httpClient())

    val eventTypes = listOf("Quote")
    val sub = api.createSubscription(eventTypes) {
        object : SubscriptionImpl() {
            override fun onRawData(data: RawData) {
                logWithThreadName("USER_HANDLER: got raw data:\n\t" + data.json)
            }
        }
    }
    sub.addSymbols(listOf("AAPL"))
    sub.addSymbols(listOf("MSFT"))
    runBlocking {
        delay(10000)
    }

    sub.removeSymbols(listOf("AAPL"))

    sub.remove()
    while (true) {}
}