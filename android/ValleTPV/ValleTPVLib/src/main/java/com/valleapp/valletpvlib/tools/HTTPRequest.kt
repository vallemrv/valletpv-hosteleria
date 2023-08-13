package com.valleapp.valletpvlib.tools

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.content.ContentValues
import java.net.ConnectException

class HTTPRequest(
        strUrl: String,
        params: ContentValues,
        op: String,
        private val handlerExternal: Handler?
) {
    init {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(strUrl)
            conn = url.openConnection() as HttpURLConnection
            conn.doOutput = true
            conn.requestMethod = "POST"
            conn.setRequestProperty("Accept-Charset", "UTF-8")

            Thread {
                var statusCode = -1
                try {
                    DataOutputStream(conn.outputStream).use { wr ->
                            wr.writeBytes(getParams(params))
                        wr.flush()
                    }

                    statusCode = conn.responseCode
                    val result = BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                            reader.lineSequence().joinToString("\n")
                    }
                    handlerExternal?.let { sendMessage(it, op, result) }
                } catch (e: ConnectException) {
                    handlerExternal?.let { sendMessage(it, "ERROR", "Fallo en la conexion") }
                } catch (e: Exception) {
                    Log.e("ERROR", e.message ?: "Unknown error")
                    handlerExternal?.let {
                        when (statusCode) {
                            500 -> sendMessage(it, "ERROR", "Hay un error en el servidor")
                            403 -> sendMessage(it, "ERROR", "Dispositivo no autorizado")
                            else -> sendMessage(it, "ERROR", "Servidor no responde")
                        }
                    } ?: run {
                        Log.e("ERROR", e.message ?: "Unknown error")
                    }
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn?.disconnect()
        }
    }

    private fun getParams(params: ContentValues): String {
        val sbParams = StringBuilder()
        var i = 0
        for (key in params.keySet()) {
            try {
                if (i != 0) {
                    sbParams.append("&")
                }
                sbParams.append(key).append("=")
                        .append(URLEncoder.encode(params.getAsString(key), "UTF-8"))
                i++
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
        return sbParams.toString()
    }

    private fun sendMessage(handler: Handler, op: String, res: String) {
        val msg = handler.obtainMessage()
        val bundle = msg.data ?: Bundle()
        bundle.putString("RESPONSE", res)
        bundle.putString("op", op)
        msg.data = bundle
        handler.sendMessage(msg)
    }
}
