package com.example.kmmktor

class DxFeedApi(host: String, port: Int?, path: String) {
    private var client: WebClient

    init {
        client = WebClient(host, port, path)
    }

    fun createSubscription(
        eventTypes: List<String>,
        onEventCallback: (RawData) -> Unit
    ): Subscription {
        return Subscription(client, eventTypes, onEventCallback)
    }

//    fun createTimeSeriesSubscription(eventTypes: List<String>): Subscription {}
}