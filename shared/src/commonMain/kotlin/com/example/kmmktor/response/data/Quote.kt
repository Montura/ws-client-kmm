/**
* Generated code. Please do not change it
*/
package com.example.kmmktor.response.data

import com.example.kmmktor.response.Event


class Quote : Event {
    var eventSymbol: String? = null
    var eventTime: Long? = null
    var sequence: Int? = null
    var timeNanoPart: Int? = null
    var bidTime: Long? = null
    var bidExchangeCode: Char? = null
    var bidPrice: Double? = null
    var bidSize: Double? = null
    var askTime: Long? = null
    var askExchangeCode: Char? = null
    var askPrice: Double? = null
    var askSize: Double? = null

    override fun toString(): String {
        return "Quote(eventSymbol=$eventSymbol, eventTime=$eventTime, sequence=$sequence, timeNanoPart=$timeNanoPart, bidTime=$bidTime, bidExchangeCode=$bidExchangeCode, bidPrice=$bidPrice, bidSize=$bidSize, askTime=$askTime, askExchangeCode=$askExchangeCode, askPrice=$askPrice, askSize=$askSize)"
    }
}
