package com.example.kmmktor

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class Greeting {
    // todo: use devex-mobile Wi-Fi both for server and for client!
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }

    private val client = HttpClient() {
        install(WebSockets)
    }

    suspend fun getHtml(): String {
        val response = client.get("https://ktor.io/docs")
        return response.bodyAsText()
    }
}
