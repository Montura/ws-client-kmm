package com.example.kmmktor

import com.example.kmmktor.WebClientUtil.Companion.CONNECT_CHANNEL
import com.example.kmmktor.WebClientUtil.Companion.HANDSHAKE_CHANNEL
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

actual class CallbackHandler(webClient: WebClient) {
    private var client: WebClient = webClient

    actual suspend fun onMessage() {
        println("USER_HANDLER: onMessage")
        // todo: unique API for user {
//        sendMessage { WebClientUtil.createSubscriptionMessage(clientId, listOf("Quote"), listOf("AAPL")) }
//        sendMessage { WebClientUtil.createConnectMessage(clientId) }
        // todo: }
    }

    suspend fun onSubscribe() {
        println("USER_HANDLER: onHandshake")
        client.connect(this)
    }

    suspend fun createSubscriptionMessage(eventTypes: List<String>, symbols: List<String>) {
        client.subscribe(this, eventTypes, symbols)
//        println("USER_HANDLER: createSubscriptionMessage")
    }

    fun onConnect() {
        println("USER_HANDLER: Hello from onConnect")
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

    private val activeClients: MutableMap<CallbackHandler, String?> = HashMap()
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
                sendMessage { WebClientUtil.createHandshakeMessage(clientId) }
                runConnectionUpdater()                 // Start heartbeat
//
                val messageOutputRoutine = launch { outputMessages(clientCallbackHandler) }
                val userInputRoutine = launch { inputMessages() }

                userInputRoutine.join() // Wait for completion; either "exit" or error
                messageOutputRoutine.cancelAndJoin()
            }
        }
        println("Client closed. Goodbye!")
        clientKt.close()
    }

    suspend fun subscribe(handler: CallbackHandler, eventTypes: List<String>, symbols: List<String>) {
        val clientId = activeClients[handler]
        clientId?.let {
            val currEventTypes = activeEventTypes[it]
            val currSymbols = activeSymbols[it]
            // todo: add eventTypes and symbols if not null
            if (currEventTypes == null && currSymbols == null) {
                sendMessage { WebClientUtil.createConnectMessage(it) }
                sendMessage { WebClientUtil.createSubscriptionMessage(it, eventTypes, symbols) }
                activeEventTypes[it] = eventTypes.toHashSet()
                activeSymbols[it] = eventTypes.toHashSet()
            }
        }
    }

    suspend fun connect(handler: CallbackHandler) {
        val clientId = activeClients[handler]
        clientId?.let {
            sendMessage { WebClientUtil.createConnectMessage(it) }
        }
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

    private suspend fun sendHeartbeat(session: WebSocketSession?): String {
        val connectMessage = WebClientUtil.createConnectMessage(clientId)
        session?.send(connectMessage)
        return connectMessage
    }

    private suspend fun DefaultClientWebSocketSession.outputMessages(clientCallbackHandler: CallbackHandler) {
        try {
            for (message in incoming) {
                message as? Frame.Text ?: continue
                onMessage(message.readText(), clientCallbackHandler)
            }
        } catch (e: Exception) {
            println("Error while receiving: " + e.localizedMessage)
        }
    }

    private suspend fun DefaultClientWebSocketSession.inputMessages() {
        while (true) {
            val message = readLine() ?: ""
            if (message.equals("exit", true)) return
            try {
                send(message)
            } catch (e: Exception) {
                println("Error while sending: " + e.localizedMessage)
                return
            }
        }
    }

    private suspend fun onMessage(msg: String, clientCallbackHandler: CallbackHandler) {
        val json: HashMap<String, Any> = JsonUtil.fromJson(msg, WebClientUtil.valueTypeForHashMapArray)[0]
        if (clientId == null ) {
            println("RECV: $msg")
            when (json.channel()) {
                HANDSHAKE_CHANNEL -> {
                    if (onHandshake(json)) {
                        activeClients[clientCallbackHandler] = clientId
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
                    clientCallbackHandler.onConnect()
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

    private suspend fun sendMessage(messageSupplier: () -> String) {
        try {
            val message = messageSupplier.invoke()
            println("SEND: $message")
            session!!.send(message)
        } catch (t: Throwable) {
            println(t)
        }
    }

    fun isReady(): Boolean {
        return clientId != null
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    println("Run WebClientKt ...")

    val webClient = WebClient()
    val clientCallbackHandler = CallbackHandler(webClient)

    GlobalScope.launch(Dispatchers.Default) {
        webClient.run(WebClientUtil.HOST, WebClientUtil.PORT, WebClientUtil.PATH, clientCallbackHandler)
    }

    runBlocking {
        while (true) {
            clientCallbackHandler.createSubscriptionMessage(listOf("Quote"), listOf("AAPL"))
        }
    }

}