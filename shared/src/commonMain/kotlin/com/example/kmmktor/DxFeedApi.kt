package com.example.kmmktor

// onRawData(RawData data)

// Subscribtion sub = webClient.createSubscrtiption(
//          () -> RawData { }
// );

// sub.addSymbols("AAPL");
//
// Subscribtion tsS = createTimeSeriesSubscription()

//
// remove(subscription)

class DxFeedApi(private val webClient: WebClient) {
    fun createSubscription(eventTypes: List<String>, subProvider: (eventTypes: List<String>) -> Subscription): Subscription {
        return webClient.createSubscription(eventTypes, subProvider)
    }

    fun subscribe(sub: Subscription) {
        webClient.subscribe(sub)
    }

    fun removeSubscription(sub: Subscription) {
        webClient.removeSubscription(sub)
    }

//    fun createTimeSeriesSubscription(eventTypes: List<String>): Subscription {}
}