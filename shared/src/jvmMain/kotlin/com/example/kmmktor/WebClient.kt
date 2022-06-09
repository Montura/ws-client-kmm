package com.example.kmmktor

import com.example.kmmktor.WebClientUtil.Companion.CONNECT_CHANNEL
import com.example.kmmktor.WebClientUtil.Companion.HANDSHAKE_CHANNEL
import com.example.kmmktor.WebClientUtil.Companion.SUCCESSFUL
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import java.util.HashMap

// todo: Address list
//  - wss://tools.dxfeed.com/webservice/cometd
//  - ws://localhost:8080/dxfeed-webservice/cometd - Quote AAPL

actual class WebClient {
    actual var session: WebSocketSession? = null
    actual var clientId: String? = null

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
                session = this
                sendMessage { WebClientUtil.createHandshakeMessage(clientId) }
                runConnectionUpdater()                 // Start heartbeat

                while (true) {
                    val incomingMsg = incoming.receive() as? Frame.Text ?: continue
                    onMessage(incomingMsg.readText())
                }
            }
        }
        println("Client closed. Goodbye!")
        clientKt.close()
    }

    private suspend fun sendHeartbeat(): String {
        val connectMessage = WebClientUtil.createConnectMessage(clientId)
        session?.send(connectMessage)
        return connectMessage
    }

    private suspend fun onMessage(msg: String) {
        println("RECV: $msg")
        val json: HashMap<String, Any> = JsonUtil.fromJson(msg, WebClientUtil.valueTypeForHashMapArray)[0]
        when (json.channel()) {
            HANDSHAKE_CHANNEL -> onHandshake(json)
            CONNECT_CHANNEL -> onConnect(json)
            null -> println("Unknown channel")
        }
    }

    private fun onConnect(json: HashMap<String, Any>) {
        if (json.value(SUCCESSFUL) == true) {
            println("Connection is established!")
        } else {
            println("Connection isn't established!")
        }
    }

    private suspend fun onHandshake(map: HashMap<String, Any>) {
        val success = map.value(SUCCESSFUL) as Boolean
        if (success) {
            println("\tHandshake completed")

            clientId = map["clientId"] as String?
            // todo: unique API for user {
            sendMessage { WebClientUtil.createSubscriptionMessage(clientId, listOf("Quote"), listOf("AAPL")) }
            sendMessage { WebClientUtil.createConnectMessage(clientId) }
            // todo: }
        } else {
            println("\tCan't establish a handshake")
            try {
                clientKt.close()
            } catch (ignored: Exception) { }
            //todo: processExit?
        }
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
                val heartbeatMessage: String = sendHeartbeat()
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