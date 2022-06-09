package com.example.kmmktor

import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KClass

actual class JsonUtil {
    actual companion object {
        private val OBJECT_MAPPER = ObjectMapper()

        actual fun toJson(value: Any?): String {
            return try {
                OBJECT_MAPPER.writeValueAsString(value)
            } catch (e: java.lang.Exception) {
                println(e)
                throw RuntimeException(e)
            }
        }

        actual fun <T : Any> fromJson(value: String?, valueType: KClass<T>): T {
            return try {
                OBJECT_MAPPER.readValue(value, valueType.java)
            } catch (e: java.lang.Exception) {
                println(e)
                throw RuntimeException(e)
            }
        }
    }
}