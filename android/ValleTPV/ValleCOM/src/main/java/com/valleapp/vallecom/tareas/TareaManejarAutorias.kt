package com.valleapp.vallecom.tareas

import android.content.ContentValues
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import org.json.JSONArray
import java.util.TimerTask

class TareaManejarAutorias(
    private val mainHandler: Handler?,
    private val idautorizado: String,
    private val timeOut: Int,
    private val url: String,
    private val uid: String
) : TimerTask() {

    private var autorizacionesSize = 0
    private var procesado = true

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            try {
                val res = msg.data.getString("RESPONSE")
                if (!res.isNullOrEmpty()) {
                    val l = JSONArray(res)
                    if (l.length() != autorizacionesSize) {
                        autorizacionesSize = l.length()
                        mainHandler?.handleMessage(msg)
                    }
                }
            } catch (e: Exception) {
                Log.w("Autorias", "Respuesta erronea del servidor")
            }
            procesado = true
        }
    }

    override fun run() {
        if (procesado) {
            procesado = false
            val p = ContentValues().apply {
                put("idautorizado", idautorizado)
                put("uid", uid)
            }
            HTTPRequest(url, p, "autorias", handler)
        }
        synchronized(this) {
            try {
                (this as Object).wait(timeOut.toLong())
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}
