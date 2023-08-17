package com.valleapp.valletpvlib.tools.tareas

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.valleapp.valletpvlib.tools.HTTPRequest
import com.valleapp.valletpvlib.tools.Instrucciones
import java.util.Queue
import java.util.TimerTask


class TareaManejarInstrucciones(
    private val cola: Queue<Instrucciones>,
    private val timeout: Long
) : TimerTask() {

    private var procesado = true
    private var count = 0

    private val handleHttp = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            try {
                val op = msg.data.getString("op")
                if (op != null && op == "ERROR") {
                    cola.poll()
                } else {
                    val res = msg.data.getString("RESPONSE")
                    if (!res.isNullOrEmpty()) {
                        synchronized(cola) {
                            val inst = cola.poll()
                            inst?.let {
                                val handlerInst = it.handler
                                handlerInst?.let { handler ->
                                    val msgInst = handler.obtainMessage()
                                    var bundle = msg.data
                                    if (bundle == null) bundle = Bundle()
                                    bundle.putString("RESPONSE", res)
                                    bundle.putString("op", inst.op)
                                    msgInst.data = bundle
                                    handler.sendMessage(msgInst)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("Instrucciones", "Error al ejecutar instruccion en el servidor")
            }
            procesado = true
            count = 0
            super.handleMessage(msg)
        }
    }

    override fun run() {
        try {
            if (procesado) {
                synchronized(cola) {
                    val inst = cola.peek()
                    inst?.let {
                        HTTPRequest(it.url, it.params, "", handleHttp)
                        procesado = false
                    }
                }
            } else {
                count++
            }

            if (count > 20) {
                count = 0
                procesado = true
            }

            Thread.sleep(timeout)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
