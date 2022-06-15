package com.example.kmmktor

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*

expect class CallbackHandler

expect fun logWithThreadName(msg: String?)

class WebClient {
    private var session: WebSocketSession? = null
    private var clientId: String? = null

    private val clientKt: HttpClient = HttpClient(CIO) {
        install(WebSockets)
        engine {
            requestTimeout = 0
        }
    }
    fun run(host: String, port: Int?, path: String?) {
        runBlocking {
            try {
                clientKt.webSocket(
                    method = HttpMethod.Get,
                    host = host,
                    port = port,
                    path = path
                )
                {
                    session = this
                    onWebSocketOpen()

                    while (true) {
                        try {
                            val incomingMsg = incoming.receive() as? Frame.Text ?: continue
                            processIncomingMessage(incomingMsg.readText())
                        } catch (e: Exception) {
                            println("Error while receiving: " + e.message)
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error while opening web-socket: " + e.message)
            }
        }
        logWithThreadName("Client closed. Goodbye!")
        clientKt.close()
    }

    suspend fun onWebSocketOpen(success: Boolean) {
        if (success) {
            logWithThreadName("\tHandshake is established!")
            sendMessage { WebClientUtil.createConnectMessage(clientId) }
            sendMessage { WebClientUtil.createSubscriptionMessage(clientId, listOf("Quote"), listOf("AAPL")) }
        } else {
            logWithThreadName("\tCan't establish a handshake")
        }
    }

    fun onWebSocketConnect(success: Boolean) {
        logWithThreadName("USER_HANDLER:")
        if (success) {
            logWithThreadName("\tConnection is established!")
        } else {
            logWithThreadName("\tCan't establish a connection!")
        }
    }

    fun onSubscribe(success: Boolean) {
        logWithThreadName("USER_HANDLER:")
        if (success) {
            logWithThreadName("\tConnection is established!")
        } else {
            logWithThreadName("\tCan't establish a connection!")
        }
    }

    fun onData(json: HashMap<String, Any?>) {
        logWithThreadName("USER_HANDLER: todo -> process data")
    }

    private suspend fun onWebSocketOpen() {
        sendMessage { WebClientUtil.createHandshakeMessage(clientId) }
        runConnectionUpdater()                 // Start heartbeat
    }

    private suspend fun processIncomingMessage(msg: String) {
        val json: HashMap<String, Any?> = JsonUtil.fromJson(msg)
        logWithThreadName("RECV: $msg")
        when (val channel = json.channel()) {
            WebClientUtil.HANDSHAKE_CHANNEL -> {
                val success = onHandshake(json)
                onWebSocketOpen(success)
            }
            WebClientUtil.CONNECT_CHANNEL -> {
                onWebSocketConnect(json.booleanValue(WebClientUtil.SUCCESSFUL_KEY))
            }
            WebClientUtil.SERVICE_SUB_CHANNEL -> {
                onSubscribe(json.booleanValue(WebClientUtil.SUCCESSFUL_KEY))
            }
            WebClientUtil.SERVICE_DATA_CHANNEL -> {
                onData(json)
            }
            else -> {
                logWithThreadName("Unknown channel: $channel")
            }
        }
    }

    private fun onHandshake(map: HashMap<String, Any?>): Boolean {
        val success = map.booleanValue(WebClientUtil.SUCCESSFUL_KEY)
        if (success) {
            if (clientId != null) {
                throw IllegalStateException("Reassigning clientId!")
            }
            clientId = map["clientId"] as String?
        } else {
            try {
                clientKt.close()
            } catch (e: Exception) {
                logWithThreadName(e.message)
            }
        }
        return success
    }

    private suspend fun sendMessage(messageSupplier: () -> String) {
        try {
            val message = messageSupplier.invoke()
            logWithThreadName("SEND: $message")
            session!!.send(message)
        } catch (t: Throwable) {
            logWithThreadName("Error while sending: " + t.message)
        }
    }

    private suspend fun sendHeartbeat(): String? {
        return try {
            val connectMessage = WebClientUtil.createConnectMessage(clientId)
            session?.send(connectMessage)
            connectMessage
        } catch (t: Throwable) {
            logWithThreadName("Error while sending: " + t.message)
            null
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun runConnectionUpdater() {
        GlobalScope.launch (Dispatchers.Default) {
            delay(8000)
            while (true) {
                val heartbeatMessage: String? = sendHeartbeat()
                heartbeatMessage?.let {
                    logWithThreadName("HEARTBEAT: $heartbeatMessage")
                    delay(20000)
                } ?: break
            }
        }
    }
}