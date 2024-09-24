package com.valleapp.valletpvlib.cashlogymanager

import android.os.Handler
import android.os.Looper
import android.util.Log

class InitializeAction(socketManager: CashlogySocketManager) :
    CashlogyAction(socketManager, Handler(Looper.getMainLooper())) {

    override fun execute() {
        socketManager.sendCommand("#I#", Handler(Looper.getMainLooper()) { msg ->
            Log.d("CASHLOGY", "Respuesta de #I#: ${msg.data.getString("value")}")
            return@Handler true
        })
    }
}
