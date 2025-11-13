package com.valleapp.valletpvlib.comunicacion

import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

class HTTPRequest(
    strUrl: String,
    params: ContentValues,
    op: String,
    handlerExternal: Handler?
) {

    // En la clase HTTPRequest

        init {
            // Iniciamos un hilo para procesar TODA la solicitud HTTP de forma asíncrona
            thread {
                try {
                    // Aseguramos que la URL tenga protocolo (http o https)
                    var finalStrUrl = strUrl
                    if (!strUrl.contains("http://") && !strUrl.contains("https://")) {
                        finalStrUrl = "http://$strUrl"
                    }

                    // Movemos la creación de la conexión DENTRO del hilo secundario
                    val url = URL(finalStrUrl)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.doOutput = true
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Accept-Charset", "UTF-8")

                    var statusCode = -1
                    try {
                        // Enviamos los parámetros al servidor
                        val wr = DataOutputStream(conn.getOutputStream())
                        wr.writeBytes(getParams(params))
                        wr.flush()
                        wr.close()

                        // Configuramos timeouts
                        conn.connectTimeout = 10000
                        conn.readTimeout = 30000

                        statusCode = conn.responseCode

                        if (statusCode == 502) {
                            handlerExternal?.let {
                                sendMessage(it, op, null, statusCode, "server_unreachable")
                            }
                            Log.e("HTTPRequest", "Error 502 - Bad Gateway en $finalStrUrl")
                            return@thread // Salimos del hilo tras manejar el 502
                        }

                        val inputStream = if (statusCode >= 400) {
                            BufferedInputStream(conn.errorStream)
                        } else {
                            BufferedInputStream(conn.inputStream)
                        }

                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val result = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            result.append(line)
                        }

                        handlerExternal?.let {
                            sendMessage(it, op, result.toString(), statusCode, "server_response")
                        }

                    } catch (e: ConnectException) {
                        // --- MANEJO DE ERROR DE CONEXIÓN ---
                        handlerExternal?.let {
                            sendMessage(it, op, null, -1, "server_unreachable")
                        }
                        Log.e("HTTPRequest", "No se pudo alcanzar el servidor en $finalStrUrl: ${e.message}")

                    } catch (e: SocketTimeoutException) {
                        // --- MANEJO DE ERROR DE TIMEOUT ---
                        handlerExternal?.let {
                            sendMessage(it, op, null, -1, "server_unreachable")
                        }
                        Log.e("HTTPRequest", "Timeout al intentar alcanzar $finalStrUrl: ${e.message}")

                    } catch (e: Exception) {
                        // --- MANEJO DE OTROS ERRORES DE RED ---
                        handlerExternal?.let {
                            sendMessage(it, op, null, -1, "server_unreachable")
                        }
                        Log.e("HTTPRequest", "Error al intentar conectar con $finalStrUrl: ${e.message}")
                    } finally {
                        conn.disconnect()
                    }

                } catch (e: Exception) {
                    // Este catch ahora solo capturará errores muy básicos, como una URL mal formada
                    handlerExternal?.let {
                        sendMessage(it, op, null, -1, "server_unreachable")
                    }
                    Log.e("HTTPRequest", "Error al inicializar la conexión en el hilo secundario: ${e.message}")
                }
            }
        }

    // Dentro de la clase HTTPRequest
    private fun getParams(params: ContentValues): String {
        val sbParams = StringBuilder()
        var i = 0
        for (key in params.keySet()) {
            try {
                if (i != 0) {
                    sbParams.append("&")
                }

                // --- INICIO DE LA CORRECCIÓN ---
                // 1. Obtenemos el valor de forma segura.
                val valor = params.getAsString(key)

                // 2. Si el valor es nulo, usamos un string vacío ("").
                //    El operador "?:" (Elvis) hace esto de forma muy concisa.
                val valorCodificado = URLEncoder.encode(valor ?: "", "UTF-8")

                // 3. Añadimos los valores ya seguros.
                sbParams.append(key).append("=").append(valorCodificado)
                // --- FIN DE LA CORRECCIÓN ---

            } catch (e: UnsupportedEncodingException) {
                Log.e("HTTPRequest", "Error al codificar parámetros: ${e.message}")
            }
            i++
        }
        return sbParams.toString()
    }

    // Método para enviar mensajes al Handler con la operación original, respuesta, código de estado y estado de conexión
    private fun sendMessage(handler: Handler, op: String, res: String?, statusCode: Int, connectionStatus: String) {
        val msg = handler.obtainMessage()
        val bundle = msg.data ?: Bundle()
        bundle.putString("RESPONSE", res) // Respuesta del servidor (o null si no hay)
        bundle.putString("op", op) // Operación original pasada al constructor
        bundle.putInt("codigoEstado", statusCode) // Código HTTP o -1 si no hay respuesta
        bundle.putString("connectionStatus", connectionStatus) // "server_response" o "server_unreachable"
        msg.data = bundle
        handler.sendMessage(msg)
    }
}