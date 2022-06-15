package com.example.kmmktor

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*

expect class CallbackHandler

expect fun logWithThreadName(msg: String?)

expect fun httpClient(): HttpClient

interface WsEventHandler {
    suspend fun processIncomingMessage(client: WebClient, msg: String, json: HashMap<String, Any?>)
}

class JvmWsEventHandler : WsEventHandler {
    private var client: WebClient? = null
    private suspend fun onWebSocketOpen() {
        client?.sendMessage { clientId -> WebClientUtil.createConnectMessage(clientId) }
        client?.sendMessage { clientId -> WebClientUtil.createSubscriptionMessage(clientId, listOf("Quote"), listOf("AAPL")) }
    }

    private fun onWebSocketConnect(success: Boolean) {
        logWithThreadName("USER_HANDLER:")
        if (success) {
            logWithThreadName("\tConnection is established!")
        } else {
            logWithThreadName("\tCan't establish a connection!")
        }
    }

    private fun onSubscribe(success: Boolean) {
        logWithThreadName("USER_HANDLER:")
        if (success) {
            logWithThreadName("\tConnection is established!")
        } else {
            logWithThreadName("\tCan't establish a connection!")
        }
    }

    private fun onData(json: HashMap<String, Any?>) {
        logWithThreadName("USER_HANDLER: todo -> process data")
    }

    override suspend fun processIncomingMessage(client: WebClient, msg: String, json: HashMap<String, Any?>) {
        this.client = client
        logWithThreadName("RECV: $msg")
        when (val channel = json.channel()) {
            WebClientUtil.HANDSHAKE_CHANNEL -> {
                onWebSocketOpen()
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
}

class WebClient(private val clientKt: HttpClient) {
    private var session: WebSocketSession? = null
    private var clientId: String? = null
    private var userListener: WsEventHandler? = null

    fun run(host: String, port: Int?, path: String?, userListener: WsEventHandler) {
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
                    this@WebClient.userListener = userListener
                    onWebSocketOpen()

                    while (true) {
                        try {
                            val incomingMsg = incoming.receive() as? Frame.Text ?: continue
                            val msg = incomingMsg.readText()
                            val json = processIncomingMessage(msg)
                            userListener.processIncomingMessage(this@WebClient, msg, json)
                        } catch (e: Exception) {
                            println("[WSClient]: Error while receiving: " + e.message)
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                println("[WSClient]: Error while opening web-socket: " + e.message)
            }
        }
        logWithThreadName("[WSClient]: Client closed. Goodbye!")
        clientKt.close()
    }

    private fun processIncomingMessage(msg: String): HashMap<String, Any?> {
        val json: HashMap<String, Any?> = JsonUtil.fromJson(msg)
        when (val channel = json.channel()) {
            WebClientUtil.HANDSHAKE_CHANNEL -> onHandshake(json)
            else -> {
                if (channel == WebClientUtil.EMPTY_CHANNEL_KEY) {
                    logWithThreadName("[WSClient]: Unknown channel!")
                }
            }
        }
        return json
    }

    private fun onHandshake(map: HashMap<String, Any?>): Boolean {
        val success = map.booleanValue(WebClientUtil.SUCCESSFUL_KEY)
        if (success) {
            logWithThreadName("[WSClient]: Handshake is established!")
            if (clientId != null) {
                throw IllegalStateException("Reassigning clientId!")
            }
            clientId = map["clientId"] as String?
        } else {
            try {
                clientKt.close()
            } catch (e: Exception) {
                logWithThreadName("[WSClient]: Can't establish a handshake:")
                logWithThreadName(e.message)
            }
        }
        return success
    }

    suspend fun sendMessage(messageSupplier: (clientId: String?) -> String) {
        try {
            val message = messageSupplier.invoke(clientId)
            session!!.send(message)
        } catch (t: Throwable) {
            logWithThreadName("[WSClient]: Error while sending: " + t.message)
        }
    }

    private suspend fun sendHeartbeat(): String? {
        return try {
            val connectMessage = WebClientUtil.createConnectMessage(clientId!!)
            session?.send(connectMessage)
            connectMessage
        } catch (t: Throwable) {
            logWithThreadName("[WSClient]: Error while sending: " + t.message)
            null
        }
    }

    private suspend fun onWebSocketOpen() {
        sendMessage { WebClientUtil.createHandshakeMessage() }
        runConnectionUpdater()                 // Start heartbeat
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun runConnectionUpdater() {
        GlobalScope.launch (Dispatchers.Default) {
            delay(8000)
            while (true) {
                val heartbeatMessage: String? = sendHeartbeat()
                heartbeatMessage?.let {
                    logWithThreadName("[WSClient]: HEARTBEAT: $heartbeatMessage")
                    delay(20000)
                } ?: break
            }
        }
    }
}