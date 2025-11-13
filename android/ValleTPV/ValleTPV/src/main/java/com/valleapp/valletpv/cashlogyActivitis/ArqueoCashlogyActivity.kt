package com.valleapp.valletpv.cashlogyActivitis

import android.app.Activity
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.valleapp.valletpv.R
import com.valleapp.valletpv.tools.ServiceTPV
import com.valleapp.valletpvlib.cashlogymanager.ArqueoAction
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ArqueoCashlogyActivity : Activity() {

    private lateinit var tvInformacionSalida: TextView
    private lateinit var btnSalir: ImageButton
    private lateinit var btnArquearCaja: ImageButton

    private lateinit var arqueoAction: ArqueoAction
    private lateinit var server: String
    private var cambio: Double = 0.0
    private var stacke: Double = 0.0
    private var cambioReal: Double = 0.0

    private var myServicio: ServiceTPV? = null

    private val mConexion = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            myServicio = (iBinder as ServiceTPV.MyBinder).service
            inicializarCashlogyManager()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            myServicio = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arqueo_cashlogy)

        tvInformacionSalida = findViewById(R.id.tvInformacionSalida)
        btnSalir = findViewById(R.id.btnSalir)
        btnArquearCaja = findViewById(R.id.arquearCaja)

        btnArquearCaja.visibility = View.GONE

        intent?.let {
            server = it.getStringExtra("URL") ?: ""
            cambio = it.getDoubleExtra("cambio", 0.0)
            stacke = it.getDoubleExtra("stacke", 0.0)
            cambioReal = it.getDoubleExtra("cambio_real", 0.0)
            val hayArqueo = it.getBooleanExtra("hayArqueo", false)

            if (!hayArqueo) {
                mostrarMensaje("No se puede realizar el arqueo porque no hay ticket de cierre.")
                btnArquearCaja.visibility = View.GONE
            } else {
                val intentService = Intent(applicationContext, ServiceTPV::class.java)
                bindService(intentService, mConexion, Context.BIND_AUTO_CREATE)
            }
        }

        btnSalir.setOnClickListener { finish() }
        btnArquearCaja.setOnClickListener {
            realizarArqueo()
            it.visibility = View.GONE
        }
    }

    private fun inicializarCashlogyManager() {
        val uiHandler = Handler(Looper.getMainLooper()) { manejarMensajeCashlogy(it) }
        myServicio?.let {
            arqueoAction = it.cashlogyArqueo(cambio, uiHandler)
        }
    }

    private fun realizarArqueo() {
        val objEfectivo = JSONArray()
        val denominaciones = arqueoAction.getDenominaciones()
        val totalEfectivoAnterior = stacke + cambioReal
        val totalEfectivoAhora = arqueoAction.totalRecicladores + arqueoAction.totalAlmacenes
        val totalCaja = totalEfectivoAhora - totalEfectivoAnterior

        try {
            for ((key, value) in denominaciones) {
                val moneda = key / 100.0
                val obj = JSONObject().apply {
                    put("Can", value)
                    put("Moneda", String.format(Locale.US, "%.2f", moneda))
                }
                objEfectivo.put(obj)
            }
            val obj = JSONObject().apply {
                put("Can", 1)
                put("Moneda", String.format(Locale.US, "%.2f", (totalCaja + cambio) - arqueoAction.totalRecicladores))
            }
            objEfectivo.put(obj)
        } catch (e: JSONException) {
            Log.e("ArqueoCashlogyActivity", e.toString())
        }

        val params = ContentValues().apply {
            put("cambio", String.format(Locale.US, "%.2f", cambio))
            put("efectivo", String.format(Locale.US, "%.2f", totalCaja + cambio))
            put("gastos", String.format(Locale.US, "%.2f", 0.0))
            put("des_efectivo", objEfectivo.toString())
            put("usaCashlogy", "true")
            put("des_gastos", "[]")
            put("uid", myServicio?.getUID())
        }


        HTTPRequest("$server/arqueos/arquear", params, "arqueo",
            Handler(Looper.getMainLooper()) { msg ->
                val response = msg.data.getString("RESPONSE")
                try {
                    // Parsear el JSON
                    val jsonResponse = response?.let { JSONObject(it) }
                    val success = jsonResponse?.getBoolean("success")

                    if (success == true) {
                        arqueoAction.cerrarCashlogy()
                    } else {
                        mostrarMensaje("Error al realizar el cierre de caja en el servidor.")
                    }
                } catch (e: Exception) {
                    // Manejar error en caso de que el JSON sea inválido
                    Log.e("ArqueoCashlogyActivity", "Error parseando JSON: ${e.message}")
                    mostrarMensaje("Error al procesar la respuesta del servidor.")
                }
                true // Indica que el mensaje fue manejado
            }
        )
    }

    private fun manejarMensajeCashlogy(message: Message): Boolean {
        val data = message.data
        val key = data.getString("key")
        val value = data.getString("value")

        when (key) {
            "CASHLOGY_DENOMINACIONES_LISTAS" -> {
                mostrarMensaje("La contabilidad está lista para cerrar caja. Puede pulsar el botón de cierre de caja.")
                btnArquearCaja.visibility = View.VISIBLE
            }
            "CASHLOGY_CIERRE_COMPLETADO" -> mostrarMensaje("Cierre completado ya puedes pulsar salir. Gracias por su colaboración.")
            "CASHLOGY_CASH" -> actualizarEfectivoEnServidor()
            "CASHLOGY_ERR" -> mostrarMensaje(value ?: "Error desconocido")
        }
        return true
    }

    private fun mostrarMensaje(mensaje: String) {
        tvInformacionSalida.text = mensaje
    }

    private fun actualizarEfectivoEnServidor() {
        val params = ContentValues().apply {
            put("cambio", String.format(Locale.US, "%.2f", cambio))
            put("stacke", String.format(Locale.US, "%.2f", arqueoAction.totalAlmacenes))
            put("cambio_real", String.format(Locale.US, "%.2f", arqueoAction.totalRecicladores))
            put("uid", myServicio?.getUID())
        }

        HTTPRequest("$server/arqueos/setcambio", params, "updateCash",
                Handler(Looper.getMainLooper()) { msg ->
                val response = msg.data.getString("RESPONSE")
            if (response == "success") {
                arqueoAction.cashLogyCerrado()
            } else {
                mostrarMensaje("Error al actualizar el efectivo en el servidor.")
            }
            true
        })
    }

    override fun onDestroy() {
        myServicio?.let { unbindService(mConexion) }
        super.onDestroy()
    }
}