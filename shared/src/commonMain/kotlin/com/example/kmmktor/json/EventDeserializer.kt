package com.example.kmmktor.json

import com.example.kmmktor.response.Event
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.*


class EventDeserializer : DeserializationStrategy<Array<Event>> {

    private val parsers: EventParsers = EventParsers()

    private val knownHeaders: MutableMap<String, Header> = HashMap()

    override fun deserialize(decoder: Decoder): Array<Event> {
        val jsonDecoder = (decoder as JsonDecoder)

        val elt = jsonDecoder.decodeJsonElement()

        if (elt !is JsonArray) {
            throw IllegalArgumentException("Failed to deserialize: input argument must be an array.")
        }

        if (elt.jsonArray.isEmpty()) {
            return emptyArray()
        }

        val result = ArrayList<Event>()

        for (item in elt.jsonArray) {
            if (item !is JsonArray) {
                throw IllegalArgumentException("Failed to deserialize: item must be an array.")
            }

            val itemArr = item.jsonArray
            val header: Header = parseMessageHeader(itemArr[0])
            val data: JsonElement = itemArr[1]
            if (data !is JsonArray) {
                throw IllegalArgumentException("Failed to deserialize: item must be an array.")
            }
            result.addAll(parseData(header, data.jsonArray))

        }

        return result.toTypedArray()
    }

    private fun parseMessageHeader(node: JsonElement): Header {
        if (node.isTextual()) {
            return parseFieldlessMessageHeader(node.jsonPrimitive.content)
        }
        if (node is JsonArray) {
            return parseFieldfullMessageHeader(node.jsonArray)
        }
        throw IllegalArgumentException("Failed to deserialize: message header should be either string (for headers w/o fields) or array (for headers with fields)")
    }

    private fun parseFieldlessMessageHeader(eventType: String): Header {
        return this.knownHeaders[eventType]
            ?: throw IllegalStateException("Failed to deserialize: fields are not defined. should be part of first massage")
    }

    private fun parseFieldfullMessageHeader(node: JsonArray): Header {
        if (node.size != 2) {
            throw IllegalArgumentException("Failed to deserialize: initial description should contain 2 elements")
        }
        val eventTypeNode: JsonElement = node[0]
        if (!eventTypeNode.isTextual()) {
            throw IllegalArgumentException("Failed to deserialize: type name is not found")
        }
        val fieldsNode: JsonElement = node[1]
        if (fieldsNode !is JsonArray) {
            throw IllegalArgumentException("Failed to deserialize: second element have to contain fields")
        }
        val fields: MutableList<String> = ArrayList()
        for (fieldNode in fieldsNode) {
            if (!fieldNode.isTextual()) {
                throw IllegalArgumentException("Failed to deserialize: field name must have string type")
            }
            fields.add(fieldNode.jsonPrimitive.content)
        }
        val header = Header(
                eventTypeNode.jsonPrimitive.content,
                fields.toTypedArray()
            )
        this.knownHeaders[header.eventType] = header
        return header
    }

    private fun parseData(header: Header, data: JsonArray): List<Event> {
        val result: MutableList<Event> = ArrayList()
        val nodes: Array<JsonElement?> = arrayOfNulls(header.fields.size)
        for (i in 0 until data.size) {
            val index = i % nodes.size
            nodes[index] = data[i]
            if (index == nodes.size - 1) {
                result.add(getParser(header).parse(header.fields, nodes))
            }
        }
        return result
    }

    private fun getParser(header: Header): EventParser {
        return parsers.getParser(header.eventType)
    }

    private data class Header constructor(val eventType: String, val fields: Array<String>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Header

            if (eventType != other.eventType) return false
            if (!fields.contentEquals(other.fields)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = eventType.hashCode()
            result = 31 * result + fields.contentHashCode()
            return result
        }
    }

    private inline fun JsonElement.isTextual(): Boolean {
        return this is JsonPrimitive && this.jsonPrimitive.isString
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Array<Event>", PrimitiveKind.STRING)

}