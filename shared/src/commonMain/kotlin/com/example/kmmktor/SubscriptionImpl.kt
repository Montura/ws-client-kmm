package com.example.kmmktor

abstract class SubscriptionImpl {
    abstract fun onRawData(data: RawData)
}

abstract class TimeSeriesSubscriptionImpl : SubscriptionImpl() {
    private var fromTime: Long = 0

    fun setFromTime(time: Long) {
        fromTime = time
    }
}