package com.example.kmmktor

import kotlin.reflect.KClass

fun HashMap<String, Any?>?.value(channelAsKey: String): Any? = this?.get(channelAsKey)

fun HashMap<String, Any?>?.booleanValue(channelAsKey: String): Boolean = this?.get(channelAsKey)?.let { it as String == "true" } ?: false
fun HashMap<String, Any?>?.channel(): String = this?.run { this[WebClientUtil.CHANNEL_KEY] as? String } ?: "null"

expect class JsonUtil {
    companion object {
        fun toJson(value: List<HashMap<String, Any?>>): String
        fun fromJson(value: String): HashMap<String, Any?>
    }
}