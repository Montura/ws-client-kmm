package com.example.kmmktor

class Subscription(private val client: WebClient,
                   private val activeEventTypes: List<String>,
                   subProvider: (eventTypes: List<String>) -> SubscriptionImpl
) {
    private var subImpl: SubscriptionImpl? = null
    private val activeSymbols: MutableSet<String> = mutableSetOf()

    init {
        subImpl = subProvider.invoke(activeEventTypes)
    }

    fun remove() {
        removeSymbols(activeSymbols.toList())
        subImpl = null
    }

    fun onRawData(data: String) {
        subImpl?.onRawData(RawData(data))
    }

    fun addSymbols(symbols: List<String>) {
        val symbolsToSubscribe = symbols.minus(activeSymbols)
        logWithThreadName("[WSClient]: added symbols: $symbolsToSubscribe")
        client.subscribe(activeEventTypes, symbolsToSubscribe)
        activeSymbols.addAll(symbols)
    }

    fun removeSymbols(symbols: List<String>) {
        val symbolsToSubscribe = symbols.intersect(activeSymbols).toList()
        logWithThreadName("[WSClient]: remove symbols: $symbolsToSubscribe")
        client.unsubscribe(activeEventTypes, symbolsToSubscribe)
        activeSymbols.removeAll(symbols.toSet())
    }
}