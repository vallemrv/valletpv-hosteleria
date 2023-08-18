package com.valleapp.valletpvlib.tools.tareas

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.valleapp.valletpvlib.tools.HTTPRequest
import com.valleapp.valletpvlib.tools.Instrucciones
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Queue
import java.util.TimerTask


class TareaManejarInstrucciones(
    private val cola: Queue<Instrucciones>,
    private val timeout: Long
) {

    private var procesado = true
    private var count = 0
    private val mutex = Mutex(true)

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
                                    var bundle =  Bundle()
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

    suspend fun startProcessing() {
        while (true) {
            mutex.withLock {  // Se bloqueará aquí si el mutex está bloqueado
                if (cola.isEmpty()) {
                    break
                }
                delay(timeout)
                processInstruction()
            }
        }
    }
    private suspend fun processInstruction() {
        try {
            if (procesado) {
                mutex.withLock {
                    val inst = cola.peek()
                    inst?.let {
                        it.params?.let { it1 -> HTTPRequest(it.url, it1, "", handleHttp) }
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

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
