package com.example.kmmktor

import kotlin.reflect.KClass

expect class JsonUtil {
    companion object {
        fun toJson(value: Any?): String
        fun <T : Any> fromJson(value: String?, valueType: KClass<T>): T
    }
}

fun HashMap<String, Any>.value(channelAsKey: String): Any? = this[channelAsKey]
fun HashMap<String, Any>.channel(): String? = this[WebClientUtil.CHANNEL] as? String