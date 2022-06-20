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
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import kotlin.jvm.Volatile

class Subscription<EventType : Event>(
    private val client: WebClient,
    private val activeEventTypes: Set<String>
) {
    private val activeSymbols: MutableSet<String> = mutableSetOf()

    @Volatile
    private var closed: Boolean = false

    @Volatile
    @Transient  // fires without synchronization
    private var eventListeners: DXFeedEventListener<EventType>? = null


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
                    // todo:
                    val events1: List<EventType> = events.toList() as List<EventType>
                    eventListeners?.eventsReceived(events1)
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

    interface DXFeedEventListener<EventType> {
        fun eventsReceived(events: List<EventType>) {}
    }

    @Synchronized
    fun addEventListener(listener: DXFeedEventListener<EventType>?) {
        if (listener == null)
            throw NullPointerException()
        if (closed)
            return

        eventListeners = addListener(eventListeners, listener, false, ::EventListeners)
    }
    private fun <L : Any> addListener(oneOrList: L?, listener: L, idempotent: Boolean, listWrapper: (Array<Any>) -> L): L {
        if (oneOrList == null)
            return listener
        if (idempotent && listener == oneOrList)
            return oneOrList
        if (oneOrList !is ListenerList<*>)
            return listWrapper.invoke(arrayOf(oneOrList, listener))

        val list: ListenerList<L> = oneOrList as ListenerList<L>
        if (idempotent && findListener(list, listener) >= 0)
            return oneOrList
        val a: Array<Any> = arrayOf(list.a, listener)
        return listWrapper.invoke(a)
    }

    private abstract class ListenerList<L> protected constructor(val a: Array<Any>)

    private fun <L> findListener(oldList: ListenerList<L>, newListener: L): Int {
        for (i in oldList.a.indices) if (newListener == oldList.a.get(i)) return i
        return -1
    }

    companion object {
        private class EventListeners<E>(a: Array<Any>) : ListenerList<DXFeedEventListener<E>>(a), DXFeedEventListener<E> {
            override fun eventsReceived(events: List<E>) {
                var error: Throwable? = null
                for (listener in a) {
                    try {
                        (listener as DXFeedEventListener<E>).eventsReceived(events)
                    } catch (e: Throwable) {
                        error = e
                    }
                    rethrow(error)
                }
            }

            private fun rethrow(error: Throwable?) {
                if (error is RuntimeException) throw (error as RuntimeException?)!!
                if (error is Error) throw error
            }
        }
    }

}