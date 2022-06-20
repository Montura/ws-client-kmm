package com.example.kmmktor

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.websocket.*
import io.rsocket.kotlin.core.WellKnownMimeType
import io.rsocket.kotlin.keepalive.KeepAlive
import io.rsocket.kotlin.ktor.client.RSocketSupport
import io.rsocket.kotlin.metadata.compositeMetadata
import io.rsocket.kotlin.payload.PayloadMimeType
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import platform.Foundation.NSThread
import kotlin.time.Duration.Companion.minutes

actual fun httpClient(): HttpClient {
    return HttpClient(Darwin) {
        install(WebSockets)
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
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