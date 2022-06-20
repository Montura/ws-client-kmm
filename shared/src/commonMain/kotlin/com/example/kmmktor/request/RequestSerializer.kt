package com.example.kmmktor.request

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

class RequestSerializer : SerializationStrategy<Request> {
    override fun serialize(encoder: Encoder, request: Request) {
        if (encoder !is JsonEncoder) error("Json Encoder is expected")

        val jsonElement: JsonElement = buildJsonObject {
            putJsonObject(
                when (request) {
                    is AddSymbolRequest -> "add"
                    is RemoveSymbolRequest -> "remove"
                    else -> error("Unknown request type")
                }
            ) {
                putJsonArray(request.getEventType()) {
                    for (symbol in request.getSymbols()) {
                        addJsonObject {
                            put("symbol", symbol)
                            if (request.getFromTime() != null) put("fromTime", request.getFromTime())
                            if (request.getFields() != null) putJsonArray("fields") {
                                request.getFields()!!.forEach { s -> add(s) }
                            }
                        }
                    }
                }
            }
        }
        encoder.encodeJsonElement(jsonElement)
    }

    override val descriptor: SerialDescriptor
        get() = TODO("Not yet implemented")
}