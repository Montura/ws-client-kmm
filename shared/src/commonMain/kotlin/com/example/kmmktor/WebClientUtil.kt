package com.example.kmmktor

class WebClientUtil {
    companion object {
        // todo: Address list
        //   - wss://tools.dxfeed.com/webservice/cometd
        //   - ws://localhost:8080/dxfeed-webservice/cometd - Quote AAPL
        //   - 208.93.103.3:7521/wapi/rsocket
        const val HOST: String = "208.93.103.3"
        val PORT: Int? = 7521
        const val PATH: String = "/wapi/rsocket"

        const val EMPTY_CHANNEL_KEY = "/null"
        const val CHANNEL_KEY = "channel"
        const val CLIENT_KEY = "clientId"
        const val SUCCESSFUL_KEY = "successful"

        const val HANDSHAKE_CHANNEL = "/meta/handshake"
        const val CONNECT_CHANNEL = "/meta/connect"
        const val SERVICE_SUB_CHANNEL = "/service/sub"
        const val SERVICE_DATA_CHANNEL = "/service/data"
        const val SERVICE_TIME_SERIES_DATA_CHANNEL = "/service/timeSeriesData"
    }
}