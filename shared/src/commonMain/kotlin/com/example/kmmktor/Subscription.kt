package com.example.kmmktor

import com.example.kmmktor.request.AddSymbolRequest
import com.example.kmmktor.request.RemoveSymbolRequest
import com.example.kmmktor.response.Event
import com.example.kmmktor.response.data.Quote
import io.rsocket.kotlin.PrefetchStrategy
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class Subscription(
    private val client: WebClient,
    private val activeEventTypes: List<String>,
    onEventCallback: (RawData) -> Unit
) {
    private var onEvent: (RawData) -> Unit = { }
    private val activeSymbols: MutableSet<String> = mutableSetOf()

    init {
        onEvent = onEventCallback
    }

    fun remove() {
        removeSymbols(activeSymbols.toList())
    }

    fun addSymbols(symbols: List<String>) {
        val symbolsToSubscribe = symbols.minus(activeSymbols)
        val addSymbolRequests = activeEventTypes.map {
                eventType -> AddSymbolRequest(eventType, symbolsToSubscribe.toTypedArray(), null, arrayOf())
        }.toList()
        activeSymbols.addAll(symbols)

        doAsync {
            logWithThreadName("[WSClient]: add symbols: $symbolsToSubscribe")
            client.subscribe(addSymbolRequests.asFlow())
                .flowOn(PrefetchStrategy(requestSize = 10, requestOn = 5))
                .collect { events: Array<Event> ->
                    events.map {
                        onEvent.invoke(RawData((it as Quote).toString()))
                    }
                }
        }
    }

    fun removeSymbols(symbols: List<String>) {
        val symbolsToUnsubscribe = symbols.intersect(activeSymbols).toList()
        val removeSymbolRequests = activeEventTypes.map {
            eventType -> RemoveSymbolRequest(eventType, symbolsToUnsubscribe.toTypedArray(), null, arrayOf())
        }
        activeSymbols.removeAll(symbols.toSet())

        doAsync {
            logWithThreadName("[WSClient]: remove symbols: $symbolsToUnsubscribe")
            client.subscribe(removeSymbolRequests.asFlow())
                .flowOn(PrefetchStrategy(requestSize = 10, requestOn = 5))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun doAsync(runnable: suspend () -> Unit) {
        GlobalScope.launch {
           runnable.invoke()
        }
    }
}