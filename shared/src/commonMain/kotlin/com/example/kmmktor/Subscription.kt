package com.example.kmmktor

class Subscription(private val client: WebClient,
                   private val activeEventTypes: List<String>,
                   onEventCallback: (RawData) -> Unit
) {
    private var onEvent: (RawData) -> Unit = {  }
    private val activeSymbols: MutableSet<String> = mutableSetOf()

    init {
        onEvent = onEventCallback
    }

    fun remove() {
        removeSymbols(activeSymbols.toList())
    }

    fun onRawData(data: String) {
        onEvent.invoke(RawData(data))
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