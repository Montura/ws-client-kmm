package com.example.kmmktor

import com.example.kmmktor.response.Event

class DxFeedApi(host: String, port: Int?, path: String) {
    private var client: WebClient

    init {
        client = WebClient(host, port, path)
    }

    fun <EventType: Event>createSubscription(
        eventTypes: List<String>,
        onEventCallback: Subscription.DXFeedEventListener<EventType>
    ): Subscription<EventType> {
        val subscription = Subscription<EventType>(client, eventTypes.toSet())
        subscription.addEventListener(onEventCallback)
        return subscription
    }

//    fun createTimeSeriesSubscription(eventTypes: List<String>): Subscription {}
}