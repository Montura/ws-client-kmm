package com.example.kmmktor

class WebClientUtil {
    companion object {
        // todo: Address list
        //  - wss://tools.dxfeed.com/webservice/cometd
        //  - ws://localhost:8080/dxfeed-webservice/cometd - Quote AAPL

        const val HOST: String = "localhost"
        val PORT: Int? = 8080
        const val PATH: String = "/dxfeed-webservice-3.312-SNAPSHOT/cometd"

        const val EMPTY_CHANNEL_KEY = "/null"
        const val CHANNEL_KEY = "channel"
        const val CLIENT_KEY = "clientId"
        const val SUCCESSFUL_KEY = "successful"

        const val HANDSHAKE_CHANNEL = "/meta/handshake"
        const val CONNECT_CHANNEL = "/meta/connect"
        const val SERVICE_SUB_CHANNEL = "/service/sub"
        const val SERVICE_DATA_CHANNEL = "/service/data"
        const val SERVICE_TIME_SERIES_DATA_CHANNEL = "/service/timeSeriesData"

        enum class SubscribeAction(val action: String) {
            SUBSCRIBE("add"),
            UNSUBSCRIBE("remove")
        }

        // KEYWORDS
        fun createHandshakeMessage(): String {
            val message: HashMap<String, Any?> = HashMap()
            message[CHANNEL_KEY] = HANDSHAKE_CHANNEL
            return JsonUtil.toJson(listOf(message))
        }

        fun createConnectMessage(clientId: String?): String {
            val message: HashMap<String, Any?> = HashMap()
            message[CHANNEL_KEY] = CONNECT_CHANNEL
            message[CLIENT_KEY] = clientId
            return JsonUtil.toJson(listOf(message))
        }

        fun createSubscribingMsg(clientId: String?, eventTypes: List<String>, symbols: List<String>): String {
            return subscribingMsgTemplate(SubscribeAction.SUBSCRIBE, clientId, eventTypes, symbols)
        }

        fun createUnsubscribingMsg(clientId: String?, eventTypes: List<String>, symbols: List<String>): String {
            return subscribingMsgTemplate(SubscribeAction.UNSUBSCRIBE, clientId, eventTypes, symbols)
        }

        private fun subscribingMsgTemplate(
            action: SubscribeAction,
            clientId: String?,
            eventTypes: List<String>,
            symbols: List<String>
        ): String {
            val message: HashMap<String, Any?> = HashMap()
            message[CHANNEL_KEY] = SERVICE_SUB_CHANNEL
            message[CLIENT_KEY] = clientId!!
            val data: MutableMap<String, Any> = HashMap()
            val subMap: MutableMap<String, Collection<String>> = HashMap()
            eventTypes.forEach { type: String ->
                subMap[type] = symbols
            }
            data[action.action] = subMap
            message["data"] = data
            return JsonUtil.toJson(listOf(message))
        }
    }
}