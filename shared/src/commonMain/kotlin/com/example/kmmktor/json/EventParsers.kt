/**
* Generated code. Please do not change it
*/
package com.example.kmmktor.json

import com.example.kmmktor.response.Event

import kotlinx.serialization.json.*
import com.example.kmmktor.response.data.Quote

class EventParsers {

    private val parsers: MutableMap<String, EventParser> = HashMap()

    init {
        parsers["Quote"] = QuoteEventParser();
    }

    fun getParser(eventType: String): EventParser {
        return parsers[eventType] ?: error("Failed to find parser for event type: $eventType")
    }

    class QuoteEventParser : EventParser {
        private val fieldSetters: MutableMap<String, (JsonElement, Quote) -> Unit> = HashMap()

    init {
        fieldSetters["eventSymbol"] = { node: JsonElement, result: Quote ->  result.eventSymbol = node.asText() }
        fieldSetters["eventTime"] = { node: JsonElement, result: Quote ->  result.eventTime = node.asLong() }
        fieldSetters["sequence"] = { node: JsonElement, result: Quote ->  result.sequence = node.asInt() }
        fieldSetters["timeNanoPart"] = { node: JsonElement, result: Quote ->  result.timeNanoPart = node.asInt() }
        fieldSetters["bidTime"] = { node: JsonElement, result: Quote ->  result.bidTime = node.asLong() }
        fieldSetters["bidExchangeCode"] = { node: JsonElement, result: Quote ->  result.bidExchangeCode = node.asText()[0] }
        fieldSetters["bidPrice"] = { node: JsonElement, result: Quote ->  result.bidPrice = node.asDouble() }
        fieldSetters["bidSize"] = { node: JsonElement, result: Quote ->  result.bidSize = node.asDouble() }
        fieldSetters["askTime"] = { node: JsonElement, result: Quote ->  result.askTime = node.asLong() }
        fieldSetters["askExchangeCode"] = { node: JsonElement, result: Quote ->  result.askExchangeCode = node.asText()[0] }
        fieldSetters["askPrice"] = { node: JsonElement, result: Quote ->  result.askPrice = node.asDouble() }
        fieldSetters["askSize"] = { node: JsonElement, result: Quote ->  result.askSize = node.asDouble() }
    }

    override fun parse(fields: Array<String>, values: Array<JsonElement?>): Event {
        val result = Quote()

        for (i in fields.indices) {
            val fieldName = fields[i]
            (fieldSetters[fieldName] ?: error("Failed to find setter for field $fieldName"))
                .invoke(values[i]!!, result)
        }
        return result
    }

    }

    companion object {
        private inline fun JsonElement.asText(): String {
            return this.jsonPrimitive.content
        }

        private inline fun JsonElement.asLong(): Long {
            return this.jsonPrimitive.long
        }

        private inline fun JsonElement.asInt(): Int {
            return this.jsonPrimitive.int
        }

        private inline fun JsonElement.asDouble(): Double {
            return this.jsonPrimitive.double
        }
    }
}