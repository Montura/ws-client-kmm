package com.example

import io.ktor.server.application.*
import com.example.plugins.*

// todo: investigate server connection:
//   -- clients aren't connecting with JAVA_HOME .../graalvm-ce-java17-22.2.0-dev/Contents/Home/bin/java
//   -- works fine with JAVA_HOME .../corretto-11.0.15/Contents/Home/bin/java
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureRouting()
    configureSockets()
}
