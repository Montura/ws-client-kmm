package com.example.kmmktor

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.rsocket.kotlin.core.WellKnownMimeType
import io.rsocket.kotlin.keepalive.KeepAlive
import io.rsocket.kotlin.ktor.client.RSocketSupport
import io.rsocket.kotlin.metadata.compositeMetadata
import io.rsocket.kotlin.payload.PayloadMimeType
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.minutes

// todo: Address list
//  - wss://tools.dxfeed.com/webservice/cometd
//  - ws://localhost:8080/dxfeed-webservice/cometd - Quote AAPL

actual fun httpClient(): HttpClient {
    return HttpClient(CIO) {
        install(WebSockets)
        engine {
            requestTimeout = 0
        }
        install(RSocketSupport) {
            connector {
                maxFragmentSize = 1024

                connectionConfig {
                    keepAlive = KeepAlive(
                        interval = 10.minutes,
                        maxLifetime = 20.minutes
                    )

                    //mime types
                    payloadMimeType = PayloadMimeType(
                        data = WellKnownMimeType.ApplicationJson,
                        metadata = WellKnownMimeType.MessageRSocketCompositeMetadata
                    )

                    setupPayload {
                        buildPayload {
                            data("")
                            compositeMetadata {
                            }
                        }
                    }

                }
            }
        }
    }
}

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