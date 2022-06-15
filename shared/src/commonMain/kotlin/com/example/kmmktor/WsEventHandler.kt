package com.example.kmmktor

interface   WsEventHandler {
    suspend fun processIncomingMessage(client: WebClient, msg: String, json: HashMap<String, Any?>)
}