package com.valleapp.valletpv

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.tu.paquete.CustomToast
import com.valleapp.valletpv.cashlogyActivitis.ArqueoCashlogyActivity
import com.valleapp.valletpv.tools.ServiceTPV
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONException
import org.json.JSONObject

class Arqueo : Activity() {

    private var myServicio: ServiceTPV? = null

    private val mConexion = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
            myServicio = (iBinder as ServiceTPV.MyBinder).service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            myServicio = null
        }
    }

    private lateinit var server: String
    private lateinit var pneGastos: LinearLayout
    private lateinit var pneEfectivo: LinearLayout
    private lateinit var txtCambio: TextView
    private lateinit var txtGastos: TextView
    private lateinit var txtEfectivo: TextView
    private var cambio = 0.0
    private var gastos = 0.0
    private var efectivo = 0.0
    private val objGastos = ArrayList<JSONObject>()
    private val objEfectivo = ArrayList<JSONObject>()
    private lateinit var cx: Context
    private val custuomToast = CustomToast(this)

    @SuppressLint("HandlerLeak")
    private val controllerHttp = object : Handler(Looper.getMainLooper()) {
        @SuppressLint("DefaultLocale")
        override fun handleMessage(msg: Message) {
            val op = msg.data.getString("op")
            val res = msg.data.getString("RESPONSE")
            if (op == null || res == null) return
            if (op == "cambio") {
                try {
                    val obj = JSONObject(res)
                    cambio = obj.getDouble("cambio")
                    txtCambio.text = String.format("%.2f €", cambio)
                    if (!obj.getBoolean("hay_arqueo")) {
                        custuomToast.showCenter(
                            "No hay Ticket para hacer un arqueo...\n " +
                                    "Este arqueo remplaza al ultimo"
                        )
                    }
                } catch (e: JSONException) {
                    Log.e("ARQUEO_ERR", e.toString())
                }
            } else if (op == "arqueo") {
                try {
                    val obj = JSONObject(res)
                    if (obj.getBoolean("success")) finish()
                    else {
                        mostrarMensaje(
                            "No hay Ticket para hacer un arqueo...\n " +
                                    "Este arqueo remplaza al ultimo"
                        )
                    }
                } catch (e: Exception) {
                    Log.e("ARQUEO_ERR", e.toString())
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun rellenarGastos() {
        gastos = 0.0
        pneGastos.removeAllViews()

        for (gasto in objGastos) {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            val inflater = cx.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            @SuppressLint("InflateParams")
            val v = inflater.inflate(R.layout.item_gastos, null)
            val can = v.findViewById<TextView>(R.id.cantidad)
            val des = v.findViewById<TextView>(R.id.Descripcion)
            val rm = v.findViewById<ImageButton>(R.id.btn_borrar)
            rm.tag = gasto

            try {
                val cantidad = gasto.getDouble("Importe")
                val descrip = gasto.getString("Des")

                if (cantidad > 0 && descrip.isNotEmpty()) {
                    can.text = String.format("%.2f €", cantidad)
                    des.text = descrip
                    gastos += cantidad
                    pneGastos.addView(v, params)
                }
            } catch (e: JSONException) {
                Log.e("ARQUEO_ERR", e.toString())
            }
        }
        txtGastos.text = String.format("%.2f €", gastos)
    }

    @SuppressLint("DefaultLocale")
    private fun rellenarEfectivo() {
        efectivo = 0.0
        pneEfectivo.removeAllViews()

        for (e in objEfectivo) {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            val inflater = cx.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            @SuppressLint("InflateParams")
            val v = inflater.inflate(R.layout.item_efectivo, null)
            val mon = v.findViewById<TextView>(R.id.txt_moneda)
            val can = v.findViewById<TextView>(R.id.txt_cantidad)
            val tot = v.findViewById<TextView>(R.id.Total)
            val rm = v.findViewById<ImageButton>(R.id.btn_borrar)
            rm.tag = e

            try {
                val moneda = e.getDouble("Moneda")
                val cantidad = e.getInt("Can")
                mon.text = String.format("%01.2f €", moneda)
                can.text = String.format("%s", cantidad)
                tot.text = String.format("%01.2f €", (cantidad * moneda))
                efectivo += cantidad * moneda
                pneEfectivo.addView(v, params)
            } catch (x: JSONException) {
                Log.e("ARQUEO_ERR", x.toString())
            }
        }
        txtEfectivo.text = String.format("%.2f €", efectivo)
    }

    fun clickAbrirCaja(v: View) {
        val p = ContentValues()
        p.put("uid", myServicio?.getUID())
        HTTPRequest("$server/impresion/abrircajon", p, "open", controllerHttp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arqueo)

        // Enlazar con el servicio
        val intent = Intent(this, ServiceTPV::class.java)
        bindService(intent, mConexion, BIND_AUTO_CREATE)

        cargarPreferencias()
        pneGastos = findViewById(R.id.pneGastos)
        pneEfectivo = findViewById(R.id.pneEfectivo)
        txtCambio = findViewById(R.id.lblCambio)
        txtEfectivo = findViewById(R.id.lblEfectivo)
        txtGastos = findViewById(R.id.lblGastos)
        this.cx = this
        val btnSalir = findViewById<ImageButton>(R.id.btnSalir)
        btnSalir.setOnClickListener { finish() }
    }

    fun addEfectivo(v: View) {
        val dlg = Dialog(this)
        dlg.setContentView(R.layout.add_efectivo)
        dlg.setTitle("Agregar efectivo")
        val s = dlg.findViewById<ImageButton>(R.id.btn_salir_monedas)
        val ok = dlg.findViewById<ImageButton>(R.id.btn_guardar_preferencias)
        val m = dlg.findViewById<TextView>(R.id.txtMoneda)
        val c = dlg.findViewById<TextView>(R.id.txtCantidad)
        s.setOnClickListener { dlg.cancel() }
        ok.setOnClickListener {
            try {
                val moneda = m.text.toString().replace(",", ".").toDouble()
                val cantidad = c.text.toString().toInt()
                if ((moneda * cantidad) > 0) {
                    try {
                        val obj = JSONObject()
                        obj.put("Can", cantidad)
                        obj.put("Moneda", moneda)
                        objEfectivo.add(obj)
                        rellenarEfectivo()
                    } catch (e: JSONException) {
                        Log.e("ARQUEO_ERR", e.toString())
                    }
                }
                dlg.cancel()
            } catch (exp: Exception) {
                Log.e("ARQUEO_ERR", exp.toString())
            }
        }
        dlg.show()
    }

    fun addGastos(v: View) {
        val dlg = Dialog(this)
        dlg.setContentView(R.layout.add_gastos)
        dlg.setTitle("Agregar gasto")
        val s = dlg.findViewById<ImageButton>(R.id.Salir)
        val ok = dlg.findViewById<ImageButton>(R.id.Aceptar)
        val txtDes = dlg.findViewById<TextView>(R.id.txtDescripcion)
        val imp = dlg.findViewById<TextView>(R.id.txtImporte)
        s.setOnClickListener { dlg.cancel() }
        ok.setOnClickListener {
            try {
                val importe = imp.text.toString().replace(",", ".").toDouble()
                val des = txtDes.text.toString()
                if (importe > 0 && des.isNotEmpty()) {
                    val obj = JSONObject()
                    obj.put("Des", des)
                    obj.put("Importe", importe)
                    objGastos.add(obj)
                    rellenarGastos()
                }
                dlg.cancel()
            } catch (e: Exception) {
                Log.e("ARQUEO_ERR", e.toString())
            }
        }
        dlg.show()
    }

    fun editCambio(v: View) {
        val dlg = Dialog(this)
        dlg.setContentView(R.layout.edit_cambio)
        dlg.setTitle("Editar Cambio")
        val s = dlg.findViewById<ImageButton>(R.id.salirCambio)
        val ok = dlg.findViewById<ImageButton>(R.id.aceptarCam)
        val txtDes = dlg.findViewById<TextView>(R.id.cambio)
        s.setOnClickListener { dlg.cancel() }
        ok.setOnClickListener {
            try {
                cambio = txtDes.text.toString().toDouble()
                txtCambio.text = String.format("%s €", cambio)
                dlg.cancel()
            } catch (e: Exception) {
                Log.e("ARQUEO_ERR", e.toString())
            }
        }
        dlg.show()
    }

    fun arquearCaja(v: View) {
        if (cambio > 0) {
            val p = ContentValues()
            p.put("cambio", cambio.toString())
            p.put("efectivo", efectivo.toString())
            p.put("gastos", gastos.toString())
            p.put("des_efectivo", objEfectivo.toString())
            p.put("des_gastos", objGastos.toString())
            p.put("uid", myServicio?.getUID())
            HTTPRequest("$server/arqueos/arquear", p, "arqueo", controllerHttp)
        } else {
            mostrarMensaje("El cambio debe ser mayor que 0 €")
        }
    }

    fun cargarPreferencias() {
        val json = JSON()
        try {
            val pref = json.deserializar("preferencias.dat", this)
            if (pref == null) {
                val intent = Intent(this, PreferenciasTPV::class.java)
                startActivity(intent)
            } else {
                server = pref.getString("URL")

                // Verifica si la preferencia indica que se debe usar Cashlogy
                val usaCashlogy = pref.getBoolean("usaCashlogy")

                if (usaCashlogy) {
                    // Solicita los datos del servidor primero
                    val p = ContentValues()
                    p.put("uid", myServicio?.getUID())
                    HTTPRequest("$server/arqueos/getcambio", p, "cambio", object : Handler(Looper.getMainLooper()) {
                        override fun handleMessage(msg: Message) {
                            val res = msg.data.getString("RESPONSE")
                            if (res != null) {
                                try {
                                    val obj = JSONObject(res)
                                    // Recoge los datos del servidor
                                    val cambio = obj.getDouble("cambio")
                                    val hayArqueo = obj.getBoolean("hay_arqueo")
                                    val stacke = obj.getDouble("stacke")
                                    val cambioReal = obj.getDouble("cambio_real")

                                    // Inicia ArqueoCashlogyActivity pasando los datos
                                    val intent = Intent(this@Arqueo, ArqueoCashlogyActivity::class.java)
                                    intent.putExtra("cambio", cambio)
                                    intent.putExtra("hayArqueo", hayArqueo)
                                    intent.putExtra("stacke", stacke)
                                    intent.putExtra("cambio_real", cambioReal)

                                    // Añadir preferencias al Intent
                                    intent.putExtra("URL", server)
                                    intent.putExtra("URL_Cashlogy", pref.getString("URL_Cashlogy"))
                                    intent.putExtra("usaCashlogy", pref.getBoolean("usaCashlogy"))

                                    startActivity(intent)
                                    finish() // Finaliza la actividad actual
                                } catch (e: JSONException) {
                                    Log.e("ARQUEO_ERR", e.toString())
                                }
                            }
                        }
                    })
                } else {
                    // Si no se usa Cashlogy, continúa con la lógica normal
                    val p = ContentValues()
                    p.put("uid", myServicio?.getUID())
                    HTTPRequest("$server/arqueos/getcambio", p, "cambio", controllerHttp)
                }
            }
        } catch (e: Exception) {
            Log.e("ARQUEO_ERR", e.toString())
        }
    }

    fun clickBorrarEfc(v: View) {
        val obj = v.tag as JSONObject
        objEfectivo.remove(obj)
        rellenarEfectivo()
    }

    fun clickBorrarGasto(v: View) {
        val obj = v.tag as JSONObject
        objGastos.remove(obj)
        rellenarGastos()
    }

    private fun mostrarMensaje(texto: String) {
        // Este método no está implementado en el código Java original,
        // asumo que debería mostrar un Toast o similar
        custuomToast.showCenter(texto)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Desenlazar del servicio
        myServicio?.let { unbindService(mConexion) }
    }
}