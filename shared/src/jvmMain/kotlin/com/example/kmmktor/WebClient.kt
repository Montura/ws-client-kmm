package com.example.kmmktor

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

actual fun logWithThreadName(msg: String?) {
    println("[${Thread.currentThread().name}]: $msg")
}

fun main() {
    logWithThreadName("Run WebClientKt ...")

    val api = DxFeedApi(WebClientUtil.HOST, WebClientUtil.PORT, WebClientUtil.PATH)

    val eventTypes = listOf("Quote")
    val sub = api.createSubscription(eventTypes) {
            data -> logWithThreadName("USER_HANDLER: got raw data:\n\t" + data.json)
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