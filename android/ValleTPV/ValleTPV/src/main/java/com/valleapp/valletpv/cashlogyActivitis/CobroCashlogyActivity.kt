package com.valleapp.valletpv.cashlogyActivitis

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
import com.valleapp.valletpvlib.cashlogymanager.PaymentAction
import java.util.Locale

class CobroCashlogyActivity : AppCompatActivity() {

    private var totalMesa: Double = 0.0
    private var lineas: String? = null
    private var myServicio: ServiceTPV? = null
    private var paymentAction: PaymentAction? = null

    private lateinit var tvTotalCobro: TextView
    private lateinit var tvTotalIngresado: TextView
    private lateinit var tvCambio: TextView
    private lateinit var btnCobrar: ImageButton
    private lateinit var btnCancelar: ImageButton
    private val customToast = CustomToast(this)

    private val mConexion = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            myServicio = (iBinder as ServiceTPV.MyBinder).service
            iniciarCobro() // Iniciar el proceso de cobro después de conectar el servicio
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            myServicio = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cobro_cashlogy)

        // Configurar el comportamiento al presionar "Atrás"
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (paymentAction?.cancelarCobro() == true) {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        })

        // Recoger datos de la Intent
        totalMesa = intent.getDoubleExtra("totalMesa", 0.0)
        lineas = intent.getStringExtra("lineas")

        // Inicializar las vistas
        tvTotalCobro = findViewById(R.id.tvTotalCobro)
        tvTotalIngresado = findViewById(R.id.tvTotalIngresado)
        tvCambio = findViewById(R.id.tvCambio)
        btnCobrar = findViewById(R.id.btnAceptar)
        btnCancelar = findViewById(R.id.btnSalir)

        // Configurar la UI con los datos iniciales
        tvTotalCobro.text = String.format(Locale.UK, "%01.2f €", totalMesa)
        tvTotalIngresado.text = String.format(Locale.UK, " %01.2f €", 0.0)
        tvCambio.text = String.format(Locale.UK, "%01.2f €", 0.0)

        // Conectar al ServicioCom
        val intent = Intent(this, ServiceTPV::class.java)
        bindService(intent, mConexion, Context.BIND_AUTO_CREATE)

        // Configurar los botones
        btnCobrar.setOnClickListener { finalizarCobroParcial(it) }
        btnCancelar.setOnClickListener {
            if (paymentAction?.cancelarCobro() == true){
                  setResult(RESULT_CANCELED)
                   finish()
            }
        } // Cancelar y cerrar la actividad
    }

    private fun iniciarCobro() {
        // Obtener PaymentAction a través de myServicio
        myServicio?.let {
            paymentAction = it.cashLogyPayment(totalMesa, Handler(Looper.getMainLooper()) { message ->
                    val data = message.data

                data?.let {
                    val key = it.getString("key", "")
                    val value = it.getString("value", "")

                    when (key) {
                        "CASHLOGY_IMPORTE_ADMITIDO" -> {
                            // Actualizar el TextView para el importe admitido
                            val importeAdmitido = value.toDouble()
                            tvTotalIngresado.text = String.format(Locale.UK, "%01.2f €", importeAdmitido)

                            // Calcular y actualizar el cambio
                            var cambio = importeAdmitido - totalMesa
                            if (cambio <= 0) cambio = 0.0
                            tvCambio.text = String.format(Locale.UK, "%01.2f €", cambio)
                        }
                        "CASHLOGY_COBRO_COMPLETADO" -> {
                            customToast.showBottom( value, Toast.LENGTH_SHORT)
                            finalizarCobro()
                        }
                        else -> Log.d("CASHLOGY", "Clave no reconocida: $key")
                    }
                }

                true
            })
        }
    }

    private fun finalizarCobro() {
        val resultData = Intent()
        val totalIngresado = tvTotalIngresado.text.toString().replace("€", "").trim().toDouble()
        val cambio = tvCambio.text.toString().replace("€", "").trim().toDouble()

        // Añadir los datos al Intent
        resultData.putExtra("totalIngresado", totalIngresado)
        resultData.putExtra("cambio", cambio)

        // Añadir los datos que recibiste al iniciar la actividad
        resultData.putExtra("totalMesa", totalMesa)
        resultData.putExtra("lineas", lineas)

        // Establecer el resultado de la actividad
        setResult(RESULT_OK, resultData)
        finish()
    }

    // Método para manejar la acción de finalizar el cobro parcial
    private fun finalizarCobroParcial(view: View) {
        paymentAction?.let {
            if (it.sePuedeCobrar()) {
                it.cobrar() // Finalizar el proceso de cobro utilizando PaymentAction
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(mConexion) // Desvincular el servicio cuando la actividad se destruya
    }
}