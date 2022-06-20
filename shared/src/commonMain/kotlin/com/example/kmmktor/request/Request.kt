package com.example.kmmktor.request

interface Request {
    fun getEventType(): String

    fun getSymbols(): Array<String>

    fun getFromTime(): Long?

    fun getFields(): Array<String>?
}