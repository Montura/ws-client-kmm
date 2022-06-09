package com.example.kmmktor

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*

// todo: Address list
//  - wss://tools.dxfeed.com/webservice/cometd
//  - ws://localhost:8080/dxfeed-webservice/cometd - Quote AAPL

actual class WebClient {
    actual var session: WebSocketSession? = null
    actual var clientId: String? = null
    actual var handshakeCompleted: Boolean = false
    actual var connectionEstablished: Boolean = false

    actual val clientKt: HttpClient = HttpClient(CIO) {
        install(WebSockets)
        engine {
            requestTimeout = 0
        }
    }

    fun run() {
        runBlocking {
            clientKt.webSocket(
                method = HttpMethod.Get,
                host = WebClientUtil.HOST,
                port = WebClientUtil.PORT,
                path = WebClientUtil.PATH
            )
             {
                onConnect(this)
                while (true) {
                    val incomingMsg = incoming.receive() as? Frame.Text ?: continue
                    onMessage(incomingMsg.readText())
                }
            }
        }
        println("Client closed. Goodbye!")
        clientKt.close()
    }

    private suspend fun sendHeartBeat(): String {
        val connectMessage = WebClientUtil.createConnectMessage(clientId)
        session?.send(connectMessage)
        return connectMessage
    }

    private suspend fun onMessage(msg: String) {
        println("RECV: $msg")
        val map = JsonUtil.fromJson(msg, WebClientUtil.valueTypeForHashMapArray)[0]
        if (!handshakeCompleted && map["channel"] == "/meta/handshake") {
            println("Handshake completed")
            handshakeCompleted = true
            val successful = map["successful"] as Boolean
            println("\thandshake successful?: $successful")
            if (!successful) {
                try {
                    clientKt.close()
                } catch (ignored: Exception) {}
                return
            }
            clientId = map["clientId"] as String?
            // todo: unique API for user {
            sendMessage { WebClientUtil.createSubscriptionMessage(clientId, listOf("Quote"), listOf("AAPL")) }
            sendMessage { WebClientUtil.createConnectMessage(clientId) }
            // todo: }
        }
        if (!connectionEstablished &&  map["channel"] == "/meta/connect") {
            println("Connection is established!")
            connectionEstablished = true
        }
    }


    private suspend fun onConnect(session: WebSocketSession) {
        this.session = session
        sendMessage { WebClientUtil.createHandshakeMessage(clientId) }

        // Start heartbeat
        runConnectionUpdater()
    }

    private suspend fun sendMessage(messageSupplier: () -> String) {
        try {
            val message = messageSupplier.invoke()
            println("SEND: $message")
            session!!.send(message)
        } catch (t: Throwable) {
            println(t)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun runConnectionUpdater() {
        GlobalScope.launch (Dispatchers.Default) {
            delay(8000)
            while (true) {
                val heartbeatMessage: String = sendHeartBeat()
                println("HEARTBEAT: $heartbeatMessage")
                delay(20000)
            }
        }
    }
}

fun main() {
    println("Run WebClientKt ...")
    WebClient().run()
}