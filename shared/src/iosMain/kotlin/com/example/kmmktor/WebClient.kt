package com.example.kmmktor

import platform.Foundation.NSThread

actual fun logWithThreadName(msg: String?) {
    println("[${NSThread.currentThread()}]: $msg")
}

fun main() {
    logWithThreadName("Run iOS WebClientKt ...")
}