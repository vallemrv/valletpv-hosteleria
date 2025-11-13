package com.valleapp.valletpvlib.cashlogymanager

import android.os.Bundle
import android.os.Handler
import android.os.Message

abstract class CashlogyAction(
    protected val socketManager: CashlogySocketManager,
    protected  val uiHandler: Handler) {
    abstract fun execute()
    // Función 'open' para notificar al UI handler
    open fun notifyUI(key: String, value: String) {
        val msg = Message().apply {
            data = Bundle().apply {
                putString("key", key)
                putString("value", value)
            }
        }
        uiHandler.sendMessage(msg)
    }
}

