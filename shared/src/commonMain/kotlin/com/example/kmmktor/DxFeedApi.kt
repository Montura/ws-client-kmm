package com.example.kmmktor

import io.ktor.client.*
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
class DxFeedApi(httpClient: HttpClient) {
    private val client: WebClient = WebClient(httpClient)

    init {
        GlobalScope.launch(Dispatchers.Default) {
            client.run(WebClientUtil.HOST, WebClientUtil.PORT, WebClientUtil.PATH)
        }
    }

    fun createSubscription(
        eventTypes: List<String>,
        subProvider: (eventTypes: List<String>) -> SubscriptionImpl
    ): Subscription {
        val sub = Subscription(client, eventTypes, subProvider)
        client.setSubscription(sub)
        return sub
    }

//    fun createTimeSeriesSubscription(eventTypes: List<String>): Subscription {}
}