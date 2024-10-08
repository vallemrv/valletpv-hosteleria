package com.valleapp.valletpv.tpvcremoto

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.valleapp.valletpv.R
import org.json.JSONException
import org.json.JSONObject
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.Locale
import kotlin.concurrent.thread

class CobroTarjetaActivity : Activity() {

    private lateinit var tvEstadoCobro: TextView
    private lateinit var btnCancelar: ImageButton
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private lateinit var handler: Handler
    private var urlTPVPC: String? = null
    private var lineas: String? = null
    private var totalToCobrar: Double = 0.0
    private var cancelado: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cobro_tarjeta)

        // Referencias UI
        tvEstadoCobro = findViewById(R.id.tvEstadoCobro)
        btnCancelar = findViewById(R.id.btnCancelar)

        // Obtener los datos del Intent
        urlTPVPC = intent.getStringExtra("urlTPVPC")
        totalToCobrar = intent.getDoubleExtra("totalMesa", 0.0)
        lineas = intent.getStringExtra("lineas")

        tvEstadoCobro.text = String.format(Locale.getDefault(),
            "Importe a cobrar: %.2f €", totalToCobrar)

        // Iniciar Handler
        handler = Handler(Looper.getMainLooper())

        // Verificar si la URL tiene el formato correcto (IP:port)
        val ip: String?
        val port: Int

        if (urlTPVPC != null && urlTPVPC!!.contains(":")) {
            // Dividir en IP y puerto
            val parts = urlTPVPC!!.split(":")
            ip = parts[0]
            port = parts[1].toInt()

        } else {
            Toast.makeText(this, "Formato de URL incorrecto", Toast.LENGTH_SHORT).show()
            return
        }
        // Iniciar el socket en segundo plano usando la IP y puerto obtenidos
          iniciarConexionSocket(ip, port, totalToCobrar)


        // Manejar el botón de cancelar
        btnCancelar.setOnClickListener {
            cancelarCobro()
        }
    }

    private fun iniciarConexionSocket(ip: String, port: Int, totalToCobrar: Double) {
        thread {
            try {
                Log.e("CobroTarjetaActivity", "Iniciando conexión con el servidor TPVPC")

                // Conectar al servidor TPVPC con un timeout de 5 segundos
                socket = Socket()
                val socketAddress = InetSocketAddress(ip, port)
                socket!!.connect(socketAddress, 5000)  // Timeout de 5 segundos

                writer = PrintWriter(socket!!.getOutputStream(), true)

                // Verificar si el socket está conectado
                if (socket!!.isConnected) {
                    cancelado = false

                    // Enviar el total a cobrar
                    val mensajeCobro = "{\"comando\": \"iniciar_cobro\", \"importe\": $totalToCobrar}"
                    writer?.println(mensajeCobro)

                    // Escuchar respuestas del servidor en un hilo separado
                    recibirRespuestasSocket()
                } else {
                    throw Exception("No se pudo conectar al servidor TPVPC")
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error de conexión con el servidor: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun recibirRespuestasSocket() {
        thread {
            try {
                val socketActual = socket ?: throw Exception("El socket no está inicializado")

                val inputStream = socketActual.getInputStream()
                val reader = inputStream.bufferedReader()

                val buffer = CharArray(1024)
                var charsLeidos: Int
                val respuestaCompleta = StringBuilder()

                while (socketActual.isConnected) {
                    try {
                        charsLeidos = reader.read(buffer)
                        if (charsLeidos > 0) {
                            respuestaCompleta.appendRange(buffer, 0, charsLeidos)
                            println("Respuesta del servidor: $respuestaCompleta")

                            if (respuestaCompleta.contains("cancelado")) {
                                runOnUiThread {
                                    cancelado = true
                                    setResult(RESULT_CANCELED)
                                    finish()
                                }
                                break
                            } else if (respuestaCompleta.contains("error")) {
                                runOnUiThread {
                                    cancelado = true
                                    // Mostrar el monto total a cobrar en la UI
                                    tvEstadoCobro.text = "Operacion Cancelada"
                                }
                                break
                            } else if (respuestaCompleta.contains("esperando")) {
                                runOnUiThread {
                                    cancelado = false
                                    // Mostrar el monto total a cobrar en la UI
                                    tvEstadoCobro.text = String.format(Locale.getDefault(),
                                        "Esperando tarjeta de credito %.2f €", totalToCobrar)
                                }
                            } else if (respuestaCompleta.contains("acpetado")) {
                                runOnUiThread {
                                    val resultData = Intent()
                                    try {
                                        val res= JSONObject(respuestaCompleta.toString())
                                        resultData.putExtra("recibo", res.getString("recibo"))
                                    }catch (ingnored: JSONException){
                                        resultData.putExtra("recibo", "0")
                                    }

                                    // Añadir los datos al Intent
                                    resultData.putExtra("totalIngresado", 0.0)
                                    resultData.putExtra("cambio", 0.0)


                                    // Añadir los datos que recibiste al iniciar la actividad
                                    resultData.putExtra("totalMesa", totalToCobrar)
                                    resultData.putExtra(
                                        "lineas",
                                        lineas
                                    ) // Convertir el JSONArray a String


                                    // Establecer el resultado de la actividad
                                    setResult(RESULT_OK, resultData)
                                    finish()
                                }
                                break
                            }
                            respuestaCompleta.clear()
                        }
                    } catch (e: SocketTimeoutException) {
                        println("No se recibió respuesta del servidor dentro del tiempo esperado.")
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@CobroTarjetaActivity, "Error en la conexión: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun cancelarCobro() {
        thread {
            try {
                if (cancelado) {
                    runOnUiThread {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                    return@thread
                }

                // Enviar mensaje de cancelación
                val mensajeCancelacion = "{\"comando\": \"cancelar_cobro\"}"
                writer?.println(mensajeCancelacion)
                Log.e("CobroTarjetaActivity", "Mensaje de cancelación enviado: $mensajeCancelacion")

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error al cancelar la operación", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cerrar la conexión del socket al destruir la Activity
        try {
            socket?.close()
        } catch (e: Exception) {
            println("Error al cerrar el socket: ${e.message}")
        }
    }
}
