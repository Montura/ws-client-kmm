package com.example.kmmktor

import com.example.kmmktor.WebClientUtil.Companion.CONNECT_CHANNEL
import com.example.kmmktor.WebClientUtil.Companion.HANDSHAKE_CHANNEL
import com.example.kmmktor.WebClientUtil.Companion.SERVICE_SUB_CHANNEL
import com.example.kmmktor.WebClientUtil.Companion.SUCCESSFUL_KEY
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*

// todo: Address list
//  - wss://tools.dxfeed.com/webservice/cometd
//  - ws://localhost:8080/dxfeed-webservice/cometd - Quote AAPL

actual class CallbackHandler {
    fun onSubscribe() {
        println("USER_HANDLER: onSubscribe")
    }

    fun onConnect() {
        println("USER_HANDLER: onConnect")
    }

    fun onHandshake() {
        println("USER_HANDLER: onHandshake")
    }
}

actual class WebClient {
    private var session: WebSocketSession? = null
    private var clientId: String? = null

    private val clientKt: HttpClient = HttpClient(CIO) {
        install(WebSockets)
        engine {
            requestTimeout = 0
        }
    }

    private var clientCallbackHandler: CallbackHandler? = null
    private val activeEventTypes:  MutableMap<String, Set<String>> = HashMap()
    private val activeSymbols:  MutableMap<String, Set<String>> = HashMap()

    actual fun run(
        host: String, port: Int?, path: String?,
        clientCallbackHandler: CallbackHandler
    ) {
        runBlocking {
            clientKt.webSocket(
                method = HttpMethod.Get,
                host = host,
                port = port,
                path = path
            )
            {
                session = this
                this@WebClient.clientCallbackHandler = clientCallbackHandler
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

    suspend fun subscribe(eventTypes: List<String>, symbols: List<String>) {
        clientId?.let {
            val currEventTypes = activeEventTypes[it]
            val currSymbols = activeSymbols[it]
//            todo:  add eventTypes and symbols if not null
            if (currEventTypes == null && currSymbols == null) {
                sendMessage { WebClientUtil.createConnectMessage(it) }
                sendMessage { WebClientUtil.createSubscriptionMessage(it, eventTypes, symbols) }
                activeEventTypes[it] = eventTypes.toHashSet()
                activeSymbols[it] = eventTypes.toHashSet()
            }
        }
    }

    private suspend fun sendHeartbeat(session: WebSocketSession?): String {
        val connectMessage = WebClientUtil.createConnectMessage(clientId)
        session?.send(connectMessage)
        return connectMessage
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

    private fun onMessage(msg: String) {
        val json: HashMap<String, Any> = JsonUtil.fromJson(msg, WebClientUtil.valueTypeForHashMapArray)[0]
        if (clientId == null ) {
            println("RECV: $msg")
            when (json.channel()) {
                HANDSHAKE_CHANNEL -> {
                    if (onHandshake(json)) {
                        clientCallbackHandler?.onHandshake()
                    }
                }
                null -> println("Unknown channel")
            }
        } else {
//            json.remove(CLIENT_KEY)
            println("RECV: $msg")
            when (json.channel()) {
                CONNECT_CHANNEL -> {
                    onConnect(json)
                    clientCallbackHandler?.onConnect()
                }
                SERVICE_SUB_CHANNEL -> {
                    clientCallbackHandler?.onSubscribe()
                }
                null -> println("Unknown channel")
            }
        }
    }

    private fun onConnect(json: HashMap<String, Any>) {
        if (json.value(SUCCESSFUL_KEY) == true) {
            println("Connection is established!")
        } else {
            println("Connection isn't established!")
        }
    }

    private fun onHandshake(map: HashMap<String, Any>): Boolean {
        val success = map.value(SUCCESSFUL_KEY) as Boolean
        if (success) {
            println("\tHandshake completed")
            clientId = map["clientId"] as String?
        } else {
            println("\tCan't establish a handshake")
            try {
                clientKt.close()
            } catch (ignored: Exception) { }
            //todo: processExit?
        }
        return success
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun runConnectionUpdater() {
        GlobalScope.launch (Dispatchers.Default) {
            delay(8000)
            while (true) {
                val heartbeatMessage: String = sendHeartbeat(session)
                println("HEARTBEAT: $heartbeatMessage")
                delay(20000)
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    println("Run WebClientKt ...")

    val webClient = WebClient()
    val clientCallbackHandler = CallbackHandler()

    GlobalScope.launch(Dispatchers.Default) {
        webClient.run(WebClientUtil.HOST, WebClientUtil.PORT, WebClientUtil.PATH, clientCallbackHandler)
    }

    runBlocking {
        while (true) {
            webClient.subscribe(listOf("Quote"), listOf("AAPL"))
        }
    }

}