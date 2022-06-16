package com.example.kmmktor

abstract class Subscription(val eventTypes: List<String>) {
    private var timeSeries: Boolean = false
    private var symbols: MutableSet<String> = mutableSetOf()

    fun getSymbols(): Collection<String> {
        return symbols
    }

    fun addSymbols(newSymbols: List<String>) {
        symbols.addAll(newSymbols)
    }

    abstract fun onRawData(data: RawData)
}

abstract class TimeSeriesSubscription(eventTypes: List<String>) : Subscription(eventTypes) {
    private var fromTime: Long = 0

    fun setFromTime(time: Long) {
        fromTime = time
    }
}