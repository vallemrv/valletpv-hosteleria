package com.valleapp.valletpv.tpvcremoto

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.valleapp.valletpv.R
import java.util.Locale



class CobroTarjetaActivity : Activity() {

    private lateinit var tvEstadoCobro: TextView
    private lateinit var btnCancelar: ImageButton
    private lateinit var socketManager: SocketManager
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

        tvEstadoCobro.text = String.format(Locale.getDefault(), "Comprobando pinpad...")

        // Verificar si la URL tiene el formato correcto (IP:port)
        val ip: String?
        val port: Int

        if (urlTPVPC != null && urlTPVPC!!.contains(":")) {
            val parts = urlTPVPC!!.split(":")
            ip = parts[0]
            port = parts[1].toInt()
        } else {
            Toast.makeText(this, "Formato de URL incorrecto", Toast.LENGTH_SHORT).show()
            return
        }

        // Inicializar SocketManager
        socketManager = SocketManager(ip, port)

        // Iniciar la conexión al servidor TPVPC
        socketManager.iniciarConexionSocket(
            onSuccess = {
                // Iniciar el cobro
                socketManager.iniciarCobro(totalToCobrar)
            },
            onError = { errorMsg ->
                runOnUiThread {
                    Toast.makeText(this, "Error de conexión: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            },
            onRespuesta = { bundle ->
                manejarRespuesta(bundle)
            }
        )

        // Manejar el botón de cancelar
        btnCancelar.setOnClickListener {
            cancelarCobro()
        }
    }

    // Función para procesar las respuestas recibidas desde SocketManager
    private fun manejarRespuesta(bundle: Bundle) {
        val estado = bundle.getString("estado")
        when (estado) {
            "cancelado" -> {
                cancelado = true
                tvEstadoCobro.text = "Operación cancelada"
            }
            "denegada" -> {
                cancelado = true
                runOnUiThread { tvEstadoCobro.text = "Operación denegada" }
            }
            "fallo" -> {
                cancelado = true
                runOnUiThread { tvEstadoCobro.text = "Pinpad error de conexión, reinicie la aplicacion." }
            }
            "pinpad" -> {
                cancelado = true
                runOnUiThread { tvEstadoCobro.text = "Eperando respuesta del pinpad... Esperemos un momento." }
            }
            "error" -> {
                cancelado = true
                runOnUiThread { tvEstadoCobro.text = "Error en el pinpad. reinicadado..." }
            }
            "esperando" -> {
                cancelado = false
                runOnUiThread {
                    tvEstadoCobro.text = String.format(
                        Locale.getDefault(),
                        "Esperando tarjeta de crédito %.2f €", totalToCobrar
                    )
                }
            }
            "iniciando" -> {
                cancelado = false
                runOnUiThread {
                    tvEstadoCobro.text = "El pinpad se esta iniciando....."
                }
            }
            "iniciado" -> {
                cancelado = true
                runOnUiThread {
                    tvEstadoCobro.text = "El pinpad está iniciado correctamente."
                }
            }
            "acpetado" -> {
                val resultData = Intent()
                resultData.putExtra("recibo", bundle.getString("recibo"))
                resultData.putExtra("totalIngresado", 0.0)
                resultData.putExtra("cambio", 0.0)
                resultData.putExtra("totalMesa", totalToCobrar)
                resultData.putExtra("lineas", lineas)

                setResult(RESULT_OK, resultData)
                finish()
            }
        }
    }

    private fun cancelarCobro() {
        if (!cancelado) {
            socketManager.cancelarCobro()
        }
        finish()
    }

    override fun onDestroy() {
        // Cerrar la conexión del socket al destruir la Activity
        socketManager.cerrarConexion()
        super.onDestroy()
    }
}
