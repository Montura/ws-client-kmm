package com.example.kmmktor

//import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*


fun HashMap<String, Any?>?.value(channelAsKey: String): Any? = this?.get(channelAsKey)

fun HashMap<String, Any?>?.booleanValue(channelAsKey: String): Boolean = this?.get(channelAsKey)?.let { it as String == "true" } ?: false
fun HashMap<String, Any?>?.channel(): String =
    this?.run { this[WebClientUtil.CHANNEL_KEY] as? String } ?: WebClientUtil.EMPTY_CHANNEL_KEY

class JsonUtil {

    object HashMapSerializer : KSerializer<List<HashMap<String, Any?>>> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HashMap<String, Any?>", PrimitiveKind.STRING)
        private val jArraySerializer = JsonArray.serializer()
        private fun List<*>.toJsonElement(): JsonElement {
            val list: MutableList<JsonElement> = mutableListOf()
            this.forEach {
                val value = it ?: "null"
                when (value) {
                    is Map<*, *> -> list.add((value).toJsonElement())
                    is List<*> -> list.add(value.toJsonElement())
                    else -> list.add(JsonPrimitive(value.toString()))
                }
            }
            return JsonArray(list)
        }

        private fun Map<*, *>.toJsonElement(): JsonElement {
            val map: MutableMap<String, JsonElement> = mutableMapOf()
            this.forEach {
                val key = it.key as? String ?: return@forEach
                when (val value = it.value ?: "null") {
                    is Map<*, *> -> map[key] = (value).toJsonElement()
                    is List<*> -> map[key] = value.toJsonElement()
                    else -> map[key] = JsonPrimitive(value.toString())
                }
            }
            return JsonObject(map)
        }

        private fun JsonElement.toKotlinObject() : Any {
            return when (this) {
                is JsonPrimitive -> this.content
                is JsonArray -> this.toKotlinObject()
                is JsonObject -> {
                    val hashMap = HashMap<String, Any?>()
                    this.forEach { (key: String, value) -> hashMap[key] = value.toKotlinObject() }
                    hashMap
                }
            }
        }

        private fun JsonArray.toKotlinObject(): Any {
            return this.map { it.toKotlinObject() }.toList()
        }

        override fun serialize(encoder: Encoder, value: List<HashMap<String, Any?>>) {
            val jsonElement = value.toJsonElement() as JsonArray
            encoder.encodeSerializableValue(jArraySerializer, jsonElement)
        }

        override fun deserialize(decoder: Decoder): List<HashMap<String, Any?>> {
            val jsonElement = decoder.decodeSerializableValue(jArraySerializer)
            return jsonElement.toKotlinObject() as List<HashMap<String, Any?>>
        }
    }
    companion object {
        //        private val OBJECT_MAPPER = ObjectMapper()
        private val serializer: HashMapSerializer = HashMapSerializer
        fun toJson(value: List<HashMap<String, Any?>>): String {
            return try {
//                OBJECT_MAPPER.writeValueAsString(value)
                Json.encodeToString(serializer, value)
            } catch (e: Exception) {
                println(e)
                throw RuntimeException(e)
            }
        }

        fun fromJson(value: String): HashMap<String, Any?> {
            return try {
//                OBJECT_MAPPER.readValue(value, valueType.java)
                Json.decodeFromString(serializer, value)[0]
            } catch (e: Exception) {
                println(e)
                throw RuntimeException(e)
            }
        }
    }
}