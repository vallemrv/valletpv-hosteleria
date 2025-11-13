package com.valleapp.valletpv.cashlogyActivitis

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.tu.paquete.CustomToast
import com.valleapp.valletpv.R
import com.valleapp.valletpv.tools.ServiceTPV
import com.valleapp.valletpvlib.cashlogymanager.ChangeAction
import java.util.Locale

class CambioCashlogyActivity : AppCompatActivity() {

    private lateinit var tvTotalAdmitido: TextView
    private var myServicio: ServiceTPV? = null
    private var changeAction: ChangeAction? = null
    private var mensajeMostrado = false
    private val customToast = CustomToast(this)

    // Mapa para asociar denominaciones con sus respectivos botones
    private val botonDenominacionesMap: MutableMap<Int, ImageButton> = mutableMapOf()

    // Definición del ServiceConnection para conectar y desconectar el servicio ServicioCom
    private val mConexion = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            myServicio = (iBinder as ServiceTPV.MyBinder).service
            iniciarAccion()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            myServicio = null
        }
    }

    private fun iniciarAccion() {
        // Configurar la acción de cambio de Cashlogy
        changeAction = myServicio?.cashLogyChange(Handler(Looper.getMainLooper()) { message ->
                val data = message.data

            data?.let {
                val key = it.getString("key", "")
                val value = it.getString("value", "")

                when (key) {
                    "CASHLOGY_WR" -> {
                        // Mostrar un Toast con la advertencia
                        customToast.showBottom( "Advertencia: $value", Toast.LENGTH_LONG)
                    }
                    "CASHLOGY_ERR" -> {
                        // Mostrar un Toast con el error y finalizar la actividad
                        customToast.showBottom( "Error: $value", Toast.LENGTH_LONG)
                        if (!value.startsWith("Error de ocupación")) {
                            finish()
                        } else {

                        }
                    }
                    "CASHLOGY_IMPORTE_ADMITIDO" -> {
                        // Actualizar el TextView para el importe admitido
                        val importeAdmitido = value.toDouble()
                        tvTotalAdmitido.text = String.format(Locale.UK, "%01.2f €", importeAdmitido)
                    }
                    "CASHLOGY_CAMBIO" -> {
                        customToast.showBottom( value, Toast.LENGTH_LONG)
                        finish()
                    }
                    "CASHLOGY_DENOMINACIONES_DISPONIBLES" -> {
                        val denominaciones = parseDenominaciones(value)
                        actualizarBotonesConDenominaciones(denominaciones)

                        // Mostrar el mensaje solo una vez
                        if (!mensajeMostrado) {
                            val tvMensajeUsuario = findViewById<TextView>(R.id.tvMensajeUsuario)
                                    tvMensajeUsuario.text = "Ahora elija la fracción más pequeña que desea recibir en el cambio."
                            mensajeMostrado = true
                        } else {

                        }
                    }
                    else -> Log.d("CASHLOGY", "Clave no reconocida: $key")
                }
            }
            true
        })
    }

    @SuppressLint("FindViewByIdCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cambio_cahslogy)
        // Configurar el comportamiento al presionar "Atrás"
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cancelarCambio()
            }
        })

        // Vincular las vistas
        tvTotalAdmitido = findViewById(R.id.tvTotalAdmitido)
        val btnSalir = findViewById<ImageButton>(R.id.btnSalir)

                // Vincular los botones a las variables y agregarlos al mapa
                botonDenominacionesMap[2000] = findViewById(R.id.btnVeinteEuros) // 20 euros -> 2000 céntimos
        botonDenominacionesMap[1000] = findViewById(R.id.btnDiezEuros) // 10 euros -> 1000 céntimos
        botonDenominacionesMap[500] = findViewById(R.id.btnCincoEuros) // 5 euros -> 500 céntimos
        botonDenominacionesMap[200] = findViewById(R.id.btnDosEuros) // 2 euros -> 200 céntimos
        botonDenominacionesMap[100] = findViewById(R.id.btnUnEuro) // 1 euro -> 100 céntimos
        botonDenominacionesMap[50] = findViewById(R.id.btnCincuentaCents) // 0.50 euros -> 50 céntimos
        botonDenominacionesMap[20] = findViewById(R.id.btnVeinteCents) // 0.20 euros -> 20 céntimos
        botonDenominacionesMap[10] = findViewById(R.id.btnDiezCents) // 0.10 euros -> 10 céntimos
        botonDenominacionesMap[5] = findViewById(R.id.btnCincoCents) // 0.05 euros -> 5 céntimos
        botonDenominacionesMap[2] = findViewById(R.id.btnDosCents) // 0.02 euros -> 2 céntimos
        botonDenominacionesMap[1] = findViewById(R.id.btnUnCentimo) // 0.01 euros -> 1 céntimo

        // Configurar un único listener para todos los botones
        botonDenominacionesMap.forEach { (denominacionEnCentimos, boton) ->
                boton.setOnClickListener {
            changeAction?.cambiar(denominacionEnCentimos)
        }
        }

        btnSalir.setOnClickListener {
            cancelarCambio()
        }

        // Conectar al ServicioCom
        val intent = Intent(this, ServiceTPV::class.java)
        bindService(intent, mConexion, Context.BIND_AUTO_CREATE)
    }

    private fun parseDenominaciones(value: String): Map<Int, Int> {
        val denominacionesMap: MutableMap<Int, Int> = mutableMapOf()
        val denominacionesArray = value.split(",")

        denominacionesArray.forEach { denominacion ->
                val parts = denominacion.split(":")
            if (parts[0].isNotEmpty()) {
                val denominacionEnCentimos = parts[0].toInt()
                val cantidad = parts[1].toInt()
                denominacionesMap[denominacionEnCentimos] = cantidad
            }
        }
        return denominacionesMap
    }

    private fun actualizarBotonesConDenominaciones(denominacionesDisponibles: Map<Int, Int>) {
        botonDenominacionesMap.forEach { (valorEnCentimos, boton) ->
            if (denominacionesDisponibles.containsKey(valorEnCentimos) && denominacionesDisponibles[valorEnCentimos]!! > 0) {
                boton.visibility = View.VISIBLE
            } else {
                boton.visibility = View.GONE
            }
        }
    }

    private fun cancelarCambio() {

        changeAction?.cancelar()
    }

    override fun onDestroy() {
        super.onDestroy()
        myServicio?.let {
            unbindService(mConexion)
            myServicio = null
        }
    }
}