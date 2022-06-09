package com.example.kmmktor

import kotlin.reflect.KClass

expect class JsonUtil {
    companion object {
        fun toJson(value: Any?): String
        fun <T : Any> fromJson(value: String?, valueType: KClass<T>): T
    }
}