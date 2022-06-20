package com.example.kmmktor

import com.example.kmmktor.json.EventDeserializer
import com.example.kmmktor.request.Request
import com.example.kmmktor.request.RequestSerializer
import com.example.kmmktor.response.Event
import io.ktor.client.*
import io.ktor.utils.io.core.*
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.ktor.client.rSocket
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

expect fun logWithThreadName(msg: String?)

expect fun httpClient(): HttpClient

@OptIn(DelicateCoroutinesApi::class)
class WebClient(host: String, port: Int?, path: String?) {
    private val eventDeserializer: EventDeserializer = EventDeserializer()
    private val requestSerializer: RequestSerializer = RequestSerializer()
    private var rsocket: Deferred<RSocket>
    private var clientKt: HttpClient = httpClient()

    init {
        rsocket = GlobalScope.async {
            clientKt.rSocket(
                host = host,
                port = port,
                path = path
            ) {

            }
        }
    }

    suspend fun subscribe(requests: Flow<Request>): Flow<Array<Event>> {
        return rsocket.await()
            .requestChannel(
                requests.take(1).map { r -> Json.encodeToString(requestSerializer, r) }
                    .map { s -> buildPayload { data(s) } }
                    .first(),
                requests.drop(1)
                    .map { r -> Json.encodeToString(requestSerializer, r) }
                    .map { s -> buildPayload { data(s) } }
            )
            .map { p -> p.data }
            .map { d -> d.readBytes() }
            .map { d -> d.decodeToString() }
            .map { d -> run { Json.decodeFromString(eventDeserializer, d)} }
    }
}