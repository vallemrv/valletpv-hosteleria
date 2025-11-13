package com.valleapp.valletpv.tpvcremoto

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.tu.paquete.CustomToast
import com.valleapp.valletpv.R
import java.util.Locale

class CobroTarjetaActivity : AppCompatActivity() {

    private lateinit var tvEstadoCobro: TextView
    private lateinit var btnCancelar: ImageButton
    private var socketManager: SocketManager? = null
    private var urlTPVPC: String? = null
    private var lineas: String? = null
    private var totalToCobrar = 0.0
    private var cancelado = false
    private val customToast = CustomToast(this)
    private var isSocketConnected = false // Nueva variable para rastrear el estado del socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cobro_tarjeta)

        tvEstadoCobro = findViewById(R.id.tvEstadoCobro)
        btnCancelar = findViewById(R.id.btnCancelar)

        // Configurar el comportamiento al presionar "Atrás"
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cancelarCobro()
            }
        })


        // Obtener los datos del Intent
        urlTPVPC = intent.getStringExtra("urlTPVPC")
        totalToCobrar = intent.getDoubleExtra("totalMesa", 0.0)
        lineas = intent.getStringExtra("lineas")

        tvEstadoCobro.text = String.format(Locale.getDefault(), "Comprobando pinpad...")

        // Verificar si la URL tiene el formato correcto (IP:port)
        val ip: String?
        val port: Int

        if (urlTPVPC != null && urlTPVPC!!.contains(":")) {
            val parts = urlTPVPC!!.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            ip = parts[0]
            port = parts[1].toInt()
        } else {
            customToast.showBottom("Formato de URL incorrecto", Toast.LENGTH_SHORT)
            finish() // Finalizamos si la URL es inválida
            return
        }

        // Inicializar SocketManager
        socketManager = SocketManager(ip, port)

        // Iniciar la conexión al servidor TPVPC
        socketManager!!.iniciarConexionSocket(
            {
                // onSuccess
                isSocketConnected = true // Marcamos el socket como conectado
                socketManager!!.iniciarCobro(totalToCobrar) // Iniciar el cobro solo si está conectado
            },
            { errorMsg: String ->
                // onError
                isSocketConnected = false // Marcamos el socket como no conectado
                runOnUiThread {
                    customToast.showBottom(
                        "Error de conexión: $errorMsg",
                        Toast.LENGTH_SHORT
                    )
                    tvEstadoCobro.text = "Error de conexión al pinpad"
                }
            },
            { bundle: Bundle -> manejarRespuesta(bundle) } // onRespuesta
        )

        // Manejar el botón de cancelar
        btnCancelar.setOnClickListener { cancelarCobro() }
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
                isSocketConnected = false // Marcamos el socket como no conectado si falla
                runOnUiThread {
                    tvEstadoCobro.text = "Pinpad error de conexión, reinicie la aplicación."
                }
            }

            "pinpad" -> {
                cancelado = true
                runOnUiThread {
                    tvEstadoCobro.text = "Esperando respuesta del pinpad... Esperemos un momento."
                }
            }

            "error" -> {
                cancelado = true
                isSocketConnected = false // Marcamos el socket como no conectado si hay error
                runOnUiThread { tvEstadoCobro.text = "Error en el pinpad. reiniciado..." }
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
                runOnUiThread { tvEstadoCobro.text = "El pinpad se está iniciando..." }
            }

            "iniciado" -> {
                cancelado = true
                runOnUiThread { tvEstadoCobro.text = "El pinpad está iniciado correctamente." }
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
        if (!cancelado && isSocketConnected) { // Solo intentamos cancelar si está conectado
            socketManager!!.cancelarCobro()
        }
        finish()
    }

    override fun onDestroy() {
        // Cerrar la conexión del socket al destruir la Activity solo si está conectado
        if (isSocketConnected) {
            socketManager!!.cerrarConexion()
        }
        super.onDestroy()
    }
}