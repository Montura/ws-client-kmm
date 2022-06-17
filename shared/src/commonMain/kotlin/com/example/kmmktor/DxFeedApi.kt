package com.example.kmmktor

import io.ktor.client.*
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
class DxFeedApi(httpClient: HttpClient, host: String, port: Int?, path: String) {
    private val client: WebClient = WebClient(httpClient)

    init {
        GlobalScope.launch(Dispatchers.Default) {
            client.run(host, port, path)
        }
    }

    fun createSubscription(
        eventTypes: List<String>,
        onEventCallback: (RawData) -> Unit
    ): Subscription {
        val sub = Subscription(client, eventTypes, onEventCallback)
        client.setSubscription(sub)
        return sub
    }

//    fun createTimeSeriesSubscription(eventTypes: List<String>): Subscription {}
}