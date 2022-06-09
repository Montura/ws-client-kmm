package com.example.kmmktor

import kotlin.reflect.KClass

actual class JsonUtil {
    actual companion object {
        actual fun toJson(value: Any?): String {
            TODO("Not yet implemented")
        }

        actual fun <T : Any> fromJson(value: String?, valueType: KClass<T>): T {
            TODO("Not yet implemented")
        }

    }
}