package com.example.kmmktor

expect class CallbackHandler {}
expect class WebClient {
    fun run(
        host: String, port: Int? = null, path: String? = null,
        clientCallbackHandler: CallbackHandler
    )
}