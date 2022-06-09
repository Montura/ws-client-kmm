package com.example.kmmktor

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlin.collections.*

// ws://localhost:8080/dxfeed-webservice/cometd - Quote AAPL

class WebClientKt {
    private var session: WebSocketSession? = null
    private var clientId: String? = null
    private var id: Int = 0
    private var handshakeCompleted = false
    private var connectionEstablished = false

    private val clientKt = HttpClient(CIO) {
        install(WebSockets)
        engine {
            requestTimeout = 0
        }
    }

    fun run() {
        // wss://tools.dxfeed.com/webservice/cometd
        // ws://localhost:8080/dxfeed-webservice/cometd - Quote AAPL
        runBlocking {
            clientKt.webSocket(
                method = HttpMethod.Get,
                host = "localhost",
                port = 8080,
                path = "/dxfeed-webservice/cometd",
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
        val connectMessage = createConnectMessage()
        session?.send(connectMessage)
        return connectMessage
    }

    private suspend fun onMessage(msg: String) {
        println("RECV: $msg")
        val map = fromJson(msg, valueTypeForHashMapArray)[0]
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
            sendMessage { createSubscriptionMessage(listOf("Quote"), listOf("AAPL")) }
            sendMessage { createConnectMessage() }
            // todo: }
        }
        if (!connectionEstablished &&  map["channel"] == "/meta/connect") {
            println("Connection is established!")
            connectionEstablished = true
        }
    }


    private suspend fun onConnect(session: WebSocketSession) {
        this.session = session
        sendMessage { createHandshakeMessage() }

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

    private fun createHandshakeMessage(): String {
        val message: MutableMap<String, Any?> = HashMap()
        message["channel"] = "/meta/handshake"
        message["clientId"] = clientId
        message["id"] = ++id
        return toJson(listOf<Map<String, Any?>>(message))
    }

    private fun createConnectMessage(): String {
        val message: MutableMap<String, Any?> = HashMap()
        message["channel"] = "/meta/connect"
        message["clientId"] = clientId
        message["id"] = ++id
        message["connectionType"] = "websocket"
        return toJson(listOf<Map<String, Any?>>(message))
    }

    private fun createSubscriptionMessage(eventTypes: List<String>, symbols: List<String>): String {
        val message: MutableMap<String, Any> = HashMap()
        message["channel"] = "/service/sub"
        message["clientId"] = clientId!!
        message["id"] = ++id
        val data: MutableMap<String, Any> = HashMap()
        val subMap: MutableMap<String, Collection<String>> = HashMap()
        eventTypes.forEach { type: String ->
            subMap[type] = symbols
        }
        data["add"] = subMap
        message["data"] = data
        return toJson(listOf<Map<String, Any>>(message))
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

    companion object {
        val valueTypeForHashMapArray = (Array(0) { HashMap<String, Any>() })::class.java
//        val HANDSHAKE_MESSAGE: Frame.Text =
//            Frame.Text(WebClientUtil.toJson(listOf(hashMapOf<String, Any>("channel" to "/meta/handshake"))))

        private val OBJECT_MAPPER = ObjectMapper()
        fun toJson(value: Any?): String {
            return try {
                OBJECT_MAPPER.writeValueAsString(value)
            } catch (e: java.lang.Exception) {
                println(e)
                throw RuntimeException(e)
            }
        }

        fun <T> fromJson(value: String?, valueType: Class<T>?): T {
            return try {
                OBJECT_MAPPER.readValue(value, valueType)
            } catch (e: java.lang.Exception) {
                println(e)
                throw RuntimeException(e)
            }
        }
    }
}

fun main() {
    println("Run WebClientKt ...")
    WebClientKt().run()
}