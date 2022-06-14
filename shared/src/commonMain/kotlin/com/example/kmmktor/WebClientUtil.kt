package com.example.kmmktor

import kotlin.reflect.KClass

class WebClientUtil {
    companion object {
        // wss://tools.dxfeed.com/webservice/cometd
        const val HOST: String = "tools.dxfeed.com"
        val PORT: Int? = null
        const val PATH: String = "/webservice/cometd"

        const val CHANNEL_KEY = "channel"
        const val CLIENT_KEY = "clientId"
        const val SUCCESSFUL_KEY = "successful"

        const val HANDSHAKE_CHANNEL = "/meta/handshake"
        const val CONNECT_CHANNEL = "/meta/connect"
        const val SERVICE_SUB_CHANNEL = "/service/sub"
        const val SERVICE_DATA_CHANNEL = "/service/data"

        val valueTypeForHashMapArray: KClass<out Array<HashMap<String, Any>>>
                = (Array(0) { HashMap<String, Any>() })::class

        fun createHandshakeMessage(clientId: String?): String {
            val message: HashMap<String, Any?> = HashMap()
            message[CHANNEL_KEY] = HANDSHAKE_CHANNEL
            message[CLIENT_KEY] = clientId
            return JsonUtil.toJson(listOf(message))
        }

        fun createConnectMessage(clientId: String?): String {
            val message: HashMap<String, Any?> = HashMap()
            message[CHANNEL_KEY] = CONNECT_CHANNEL
            message[CLIENT_KEY] = clientId
            message["connectionType"] = "websocket"
            return JsonUtil.toJson(listOf(message))
        }

        fun createSubscriptionMessage(clientId: String?, eventTypes: List<String>, symbols: List<String>): String {
            val message: HashMap<String, Any?> = HashMap()
            message[CHANNEL_KEY] = SERVICE_SUB_CHANNEL
            message[CLIENT_KEY] = clientId!!
            val data: MutableMap<String, Any> = HashMap()
            val subMap: MutableMap<String, Collection<String>> = HashMap()
            eventTypes.forEach { type: String ->
                subMap[type] = symbols
            }
            data["add"] = subMap
            message["data"] = data
            return JsonUtil.toJson(listOf(message))
        }
    }
}