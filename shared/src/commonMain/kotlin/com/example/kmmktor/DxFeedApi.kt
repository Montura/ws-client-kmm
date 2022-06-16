package com.example.kmmktor

import io.ktor.client.*
import kotlinx.coroutines.*

class Subscription(private val client: WebClient) {
    private var subImpl: SubscriptionImpl? = null
    private var activeEventTypes: List<String>? = null
    private val activeSymbols: MutableSet<String> = mutableSetOf()

    fun init(
        eventTypes: List<String>,
        subProvider: (eventTypes: List<String>) -> SubscriptionImpl
    ): Subscription {
        activeEventTypes = eventTypes
        subImpl = subProvider.invoke(eventTypes)
        return this
    }

    fun removeSubscription() {
        subImpl = null
    }

    fun onRawData(data: String) {
        subImpl?.onRawData(RawData(data))
    }

    fun addSymbols(symbols: List<String>) {
        val sub1 = subImpl!!
        val symbolsToSubscribe = symbols.minus(activeSymbols)
        logWithThreadName("[WSClient]: added symbols: $symbolsToSubscribe")
        client.subscribe(activeEventTypes!!, symbolsToSubscribe)
        activeSymbols.addAll(symbols)
    }

    fun removeSymbols(symbols: List<String>) {
        val sub1 = subImpl!!
        val symbolsToSubscribe = symbols.intersect(activeSymbols).toList()
        logWithThreadName("[WSClient]: remove symbols: $symbolsToSubscribe")
        client.unsubscribe(activeEventTypes!!, symbolsToSubscribe)
        activeSymbols.addAll(symbols)
    }
}

@OptIn(DelicateCoroutinesApi::class)
class DxFeedApi(httpClient: HttpClient) {
    private val client: WebClient = WebClient(httpClient)
    private val sub: Subscription = Subscription(client)

    init {
        GlobalScope.launch(Dispatchers.Default) {
            client.run(WebClientUtil.HOST, WebClientUtil.PORT, WebClientUtil.PATH, sub)
        }
    }

    fun createSubscription(
        eventTypes: List<String>,
        subProvider: (eventTypes: List<String>) -> SubscriptionImpl
    ): Subscription {
        return sub.init(eventTypes, subProvider)
    }

//    fun createTimeSeriesSubscription(eventTypes: List<String>): Subscription {}
}