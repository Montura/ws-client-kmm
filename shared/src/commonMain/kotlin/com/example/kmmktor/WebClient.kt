package com.example.kmmktor

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*

expect fun logWithThreadName(msg: String?)

expect fun httpClient(): HttpClient

class WebClient(private val clientKt: HttpClient) {
    private var session: WebSocketSession? = null
    private var clientId: String? = null
    private var sub: Subscription? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private val counterContext = newSingleThreadContext("CounterContext")
    private val eventQueue = ArrayDeque<(String?) -> String>()


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
                            val msg = incomingMsg.readText()
                            val json = processIncomingMessage(msg)
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
    fun setSubscription(sub: Subscription) {
        this.sub = sub
    }

    private fun processIncomingMessage(msg: String): HashMap<String, Any?> {
        val json: HashMap<String, Any?> = JsonUtil.fromJson(msg)
        logWithThreadName("RECV: $msg")
        when (val channel = json.channel()) {
            WebClientUtil.HANDSHAKE_CHANNEL -> {
                onHandshake(json)
            }
            WebClientUtil.CONNECT_CHANNEL -> {
//                onWebSocketConnect(json.booleanValue(WebClientUtil.SUCCESSFUL_KEY))
            }
            WebClientUtil.SERVICE_SUB_CHANNEL -> {
//                onSubscribe(json.booleanValue(WebClientUtil.SUCCESSFUL_KEY))
            }
            WebClientUtil.SERVICE_DATA_CHANNEL -> {
                sub?.onRawData(json.toString())
            }
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

    private fun onWebSocketConnect(success: Boolean) {
        logWithThreadName("[WSClient]:")
        if (success) {
            logWithThreadName("\tConnection is established!")
        } else {
            logWithThreadName("\tCan't establish a connection!")
        }
    }

    private fun onSubscribe(success: Boolean) {
        logWithThreadName("[WSClient]:")
        if (success) {
            logWithThreadName("\tonSubscribe is successful!")
        } else {
            logWithThreadName("\tonSubscribe isn't successful!")
        }
    }

    private suspend fun sendMessage(messageSupplier: (clientId: String?) -> String) {
        try {
            val message = messageSupplier.invoke(clientId)
            logWithThreadName("SEND: $message")
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

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    fun subscribe(eventTypes: List<String>, symbolsToSubscribe: List<String>) {
        if (symbolsToSubscribe.isEmpty()) return
        runBlocking {
            withContext(counterContext) {
                logWithThreadName("Add subscriptions")
                eventQueue.addFirst { WebClientUtil.createConnectMessage(clientId) }
                eventQueue.addFirst { WebClientUtil.createSubscribingMsg(clientId, eventTypes, symbolsToSubscribe) }
            }
        }
        GlobalScope.launch(Dispatchers.Default) {
            withContext(counterContext) {
                while (clientId == null) {
                    logWithThreadName("[WSClient]: is waiting for client")
                    delay(3000)
                }
                sendMessage(eventQueue.removeLast())
                sendMessage(eventQueue.removeLast())
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    fun unsubscribe(eventTypes: List<String>, symbolsToSubscribe: List<String>) {
        if (symbolsToSubscribe.isEmpty()) return
        runBlocking {
            withContext(counterContext) {
                logWithThreadName("Remove subscriptions")
                eventQueue.addFirst { WebClientUtil.createConnectMessage(clientId) }
                eventQueue.addFirst { WebClientUtil.createUnsubscribingMsg(clientId, eventTypes, symbolsToSubscribe) }
            }
        }
        GlobalScope.launch(counterContext) {
            while (clientId == null) {
                logWithThreadName("[WSClient]: is waiting for client")
                delay(3000)
            }
            sendMessage(eventQueue.removeLast())
            sendMessage(eventQueue.removeLast())
        }
    }
}