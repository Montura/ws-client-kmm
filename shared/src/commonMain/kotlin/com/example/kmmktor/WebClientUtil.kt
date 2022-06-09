package com.example.kmmktor

import kotlin.reflect.KClass

class WebClientUtil {
    companion object {
        fun createHandshakeMessage(clientId: String?): String {
            val message: MutableMap<String, Any?> = HashMap()
            message["channel"] = "/meta/handshake"
            message["clientId"] = clientId
            return JsonUtil.toJson(listOf<Map<String, Any?>>(message))
        }

        fun createConnectMessage(clientId: String?): String {
            val message: MutableMap<String, Any?> = HashMap()
            message["channel"] = "/meta/connect"
            message["clientId"] = clientId
            message["connectionType"] = "websocket"
            return JsonUtil.toJson(listOf<Map<String, Any?>>(message))
        }

        fun createSubscriptionMessage(clientId: String?, eventTypes: List<String>, symbols: List<String>): String {
            val message: MutableMap<String, Any> = HashMap()
            message["channel"] = "/service/sub"
            message["clientId"] = clientId!!
            val data: MutableMap<String, Any> = HashMap()
            val subMap: MutableMap<String, Collection<String>> = HashMap()
            eventTypes.forEach { type: String ->
                subMap[type] = symbols
            }
            data["add"] = subMap
            message["data"] = data
            return JsonUtil.toJson(listOf<Map<String, Any>>(message))
        }

        const val HOST: String = "localhost"
        const val PORT: Int = 8080
        const val PATH: String = "/dxfeed-webservice/cometd"

        val valueTypeForHashMapArray: KClass<out Array<HashMap<String, Any>>>
            = (Array(0) { HashMap<String, Any>() })::class
    }
}