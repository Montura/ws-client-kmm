package com.example.kmmktor

expect class CallbackHandler {
    suspend fun onMessage()
}
expect class WebClient {
    fun run(
        host: String, port: Int? = null, path: String? = null,
        clientCallbackHandler: CallbackHandler
    )
}