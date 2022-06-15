package com.example.kmmktor

class JvmWsEventHandler : WsEventHandler {
    private var client: WebClient? = null

    private suspend fun onWebSocketOpen() {
        client?.sendMessage { clientId -> WebClientUtil.createConnectMessage(clientId) }
        client?.sendMessage { clientId ->
            WebClientUtil.createSubscriptionMessage(
                clientId,
                listOf("Quote"),
                listOf("AAPL")
            )
        }
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