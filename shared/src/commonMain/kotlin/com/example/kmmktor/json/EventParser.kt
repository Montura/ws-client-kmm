package com.example.kmmktor.json

import com.example.kmmktor.response.Event
import kotlinx.serialization.json.JsonElement

interface EventParser {
    /**
     * Creates event and fills it fields with values
     *
     * @param fields array of event fields
     * @param values array of events field's values
     * @return parsed Event
     */
    fun parse(fields: Array<String>, values: Array<JsonElement?>): Event
}