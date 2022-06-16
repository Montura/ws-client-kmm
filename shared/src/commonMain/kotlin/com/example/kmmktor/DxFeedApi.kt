package com.example.kmmktor

import io.ktor.client.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.native.concurrent.ThreadLocal

class DxFeedApi {
    @ThreadLocal // todo: check
    companion object {
        private lateinit var client: WebClient

        @OptIn(DelicateCoroutinesApi::class)
        fun init(httpClient: HttpClient) {
            client = WebClient(httpClient)
            GlobalScope.launch(Dispatchers.Default) {
                client.run(WebClientUtil.HOST, WebClientUtil.PORT, WebClientUtil.PATH)
            }
        }

        fun createSubscription(
            eventTypes: List<String>,
            subProvider: (eventTypes: List<String>) -> Subscription
        ): Subscription {
            return client.createSubscription(eventTypes, subProvider)
        }

        fun subscribe(sub: Subscription) {
            client.subscribe(sub)
        }

        fun removeSubscription(sub: Subscription) {
            client.removeSubscription(sub)
        }
    }
//    fun createTimeSeriesSubscription(eventTypes: List<String>): Subscription {}
}