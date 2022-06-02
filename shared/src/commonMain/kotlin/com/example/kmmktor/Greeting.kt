package com.example.kmmktor

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.websocket.*

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

    suspend fun onWebSocket(host: String, port: Int?, path:String, getMsg: () -> String?) {
        return client.webSocket(method = HttpMethod.Get, host = host, port = port, path = path) {
            while (true) {
                val othersMessage = incoming.receive() as? Frame.Text ?: continue
                println(othersMessage.readText())
                val myMessage = getMsg.invoke()
                if (myMessage != null) {
                    send(myMessage)
                }
            }
        }
    }

    suspend fun onConnect() {
//        onWebSocket("192.168.12.69", 8080, "/chat", foo);

        client.webSocket(method = HttpMethod.Get, host = "192.168.12.69", port = 8080, path = "/chat") {
            while (true) {
                val othersMessage = incoming.receive() as? Frame.Text ?: continue
                println(othersMessage.readText())
                val myMessage = "Hello, from iOS" // readLine()
                if (myMessage != null) {
                    send(myMessage)
                }
            }
        }
    }
    fun onClose() {
        client.close()
        println("Connection closed. Goodbye!")
    }
}
