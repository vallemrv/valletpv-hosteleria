package com.valleapp.valletpv

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.tu.paquete.CustomToast
import com.valleapp.valletpv.tools.ServiceTPV
import com.valleapp.valletpv.tools.ToastShowInfoCuenta
import com.valleapp.valletpvlib.db.DBCamareros
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.roundToInt

class Camareros : AppCompatActivity() {

    private val cx: Context = this
    private var lscam: JSONArray? = null
    private var camSel: JSONObject? = null
    private var myServicio: ServiceTPV? = null
    private var dbCamareros: DBCamareros? = null
    private var presBack = 0
    private var toastShowInfoCuenta: ToastShowInfoCuenta? = null
    private val customToast = CustomToast(this)
    private var server = ""

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private val handleHttp = Handler(Looper.getMainLooper()) { msg ->
        val op = msg.data.getString("op")
        
        if (op == null) {
            rellenarCamareros()
        } else {
            if (op == "show_info_cobro") {
                val datos = msg.data
                val entrega = datos.getDouble("entrega")
                val cambio = datos.getDouble("cambio")
                val inflater = layoutInflater
                val layout = inflater.inflate(R.layout.toast_info_cambio, findViewById(R.id.toast_info_cobro_container))
                toastShowInfoCuenta?.cancel()
                toastShowInfoCuenta = ToastShowInfoCuenta()
                if (entrega > 0) {
                    toastShowInfoCuenta?.show(entrega, cambio, 10000, applicationContext, layout)
                } else {
                    toastShowInfoCuenta?.showMessageOnly("Cobro aceptado\ncon tarjeta", 10000, applicationContext, layout)
                }
            }
        }
        true
    }

    private fun rellenarCamareros() {
        try {
            lscam = dbCamareros?.filter("autorizado=1")
            if ((lscam?.length() ?: 0) > 0) {
                val ll = findViewById<TableLayout>(R.id.pneCamareros)
                ll.removeAllViews()

                val metrics = resources.displayMetrics

                val params = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )

                val rowparams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    (metrics.density * 120).roundToInt()
                ).apply {
                    setMargins(5, 5, 5, 5)
                }

                ll.isStretchAllColumns = true

                var row = TableRow(cx)
                ll.addView(row, params)

                for (i in 0 until lscam!!.length()) {
                    val cam = lscam!!.getJSONObject(i)
                    val btn = Button(cx).apply {
                        id = i
                        isSingleLine = false
                        text = String.format("%s\n%s", cam.getString("nombre"), cam.getString("apellidos"))
                        setBackgroundResource(com.valleapp.valletpvlib.R.drawable.fondo_btn_xml)
                        textSize = 20f // Establecer el tamaño del texto a 26sp
                        setOnClickListener {
                            try {
                                val obj = lscam!!.getJSONObject(id)
                                entrarEnMesas(obj)
                            } catch (_: JSONException) {
                                Log.e("ERROR", "Error al acceder al camarero")
                            }
                        }
                    }
                    row.addView(btn, rowparams)

                    if ((i + 1) % 5 == 0) {
                        row = TableRow(cx)
                        ll.addView(row, params)
                    }
                }
            }
        } catch (_: Exception) {
            Log.e("ERROR", "Error al acceder al camarero")
        }
    }

    private fun entrarEnMesas(camarero: JSONObject) {
        presBack = 0
        camSel = camarero
        val intent = Intent(cx, Mesas::class.java).apply {
            putExtra("cam", camSel.toString())
            putExtra("server", server)
        }
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camareros)
        server = intent.getStringExtra("server") ?: ""
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (presBack >= 1) {
                    finish() // Cierra la actividad
                } else {
                    customToast.showBottom(
                        "Pulsa otra vez para salir",
                        Toast.LENGTH_SHORT
                    )
                    presBack++
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }



    override fun onResume() {
        super.onResume()
        presBack = 0
        val intent = Intent(applicationContext, ServiceTPV::class.java)
        bindService(intent, mConexion, BIND_AUTO_CREATE)
        rellenarCamareros()
    }

    override fun onDestroy() {
        unbindService(mConexion)
        onBackPressedCallback.remove()
        super.onDestroy()
    }

    private val mConexion = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            myServicio = (iBinder as ServiceTPV.MyBinder).service
            myServicio?.setExHandler("camareros", handleHttp)
            dbCamareros = myServicio?.getDb("camareros") as? DBCamareros
            rellenarCamareros()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            myServicio = null
        }
    }
}