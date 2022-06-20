package com.example.kmmktor.request

import com.example.kmmktor.response.Event
import kotlin.reflect.KClass

/**
 * Request to Add Symbol to subscription
 */
@kotlinx.serialization.Serializable
class AddSymbolRequest(
    private val eventType: String,
    private val symbols: Array<String>,
    private val fromTime: Long?,
    private val fields: Array<String>?
) : Request {

    constructor(eventType: KClass<out Event>, symbols: Array<String>) :
            this(eventType, symbols, null, null)


    constructor(eventType: KClass<out Event>, symbols: Array<String>, fromTime: Long?) :
            this(eventType, symbols, fromTime, null)


    constructor(eventType: KClass<out Event>, symbols: Array<String>, fields: Array<String>?) :
            this(eventType, symbols, null, fields)


    constructor(
        eventType: KClass<out Event>,
        symbols: Array<String>,
        fromTime: Long?,
        fields: Array<String>?
    ) : this(eventType.simpleName!!, symbols, fromTime, fields)

    /**
     * Type of event
     */
    override fun getEventType(): String {
        return this.eventType
    }

    /**
     * List of event symbols
     */
    override fun getSymbols(): Array<String> {
        return this.symbols
    }

    /**
     * Specifies time to receive historical events from.
     */
    override fun getFromTime(): Long? {
        return this.fromTime
    }

    /**
     * List of event's fields to receive
     */
    override fun getFields(): Array<String>? {
        return this.fields
    }
}