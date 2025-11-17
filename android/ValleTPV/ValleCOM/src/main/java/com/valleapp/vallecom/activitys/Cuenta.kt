package com.valleapp.vallecom.activitys

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.ListView
import android.widget.TextView
import com.valleapp.vallecom.adaptadores.AdaptadorTicket
import com.valleapp.vallecom.utilidades.ActivityBase
import com.valleapp.vallecom.utilidades.ServiceCOM
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import com.valleapp.valletpvlib.db.DBCuenta
import com.valleapp.valletpvlib.db.DBMesas
import com.valleapp.valletpvlib.tools.Instruccion
import org.json.JSONException
import org.json.JSONObject

class Cuenta : ActivityBase() {

    private var totalMesa: String = ""
    private lateinit var mesa: JSONObject
    private lateinit var dbCuenta: DBCuenta
    private lateinit var dbMesa: DBMesas

    private val mConexion = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            try {
                myServicio = (iBinder as ServiceCOM.MyBinder).getService()
                myServicio?.setExHandler("lineaspedido", handlerHttp)
                dbCuenta = myServicio?.getDb("lineaspedido") as DBCuenta
                dbMesa = myServicio?.getDb("mesas") as DBMesas
                rellenarTicket()

                // Obtener datos actuales de la mesa para enviar al servidor
                val datosActuales = dbCuenta.filter("IDMesa=" + mesa.getString("ID"))

                // Actualizar cuenta
                val p = ContentValues().apply {
                    put("idm", mesa.getString("ID"))
                    put("reg", datosActuales.toString())
                    put("uid", myServicio?.getUid())
                }
                HTTPRequest("$server/cuenta/get_cuenta", p, "actualizar", handlerHttp)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            myServicio = null
        }
    }

    @SuppressLint("HandlerLeak")
    private val handlerHttp = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val op = msg.data.getString("op")
            val res = msg.data.getString("RESPONSE") ?: return

            if (op == "actualizar") {
                try {
                    val raw = res.trim()

                    // Normalizar la respuesta: puede venir como array o como objeto
                    val datos: JSONObject = when {
                        raw.startsWith("[") -> {
                            // Si viene un array JSON directamente, lo envolvemos en un objeto con "reg"
                            JSONObject().apply {
                                put("soniguales", false)
                                put("reg", org.json.JSONArray(raw))
                            }
                        }
                        raw.contains("'") && !raw.contains("\"") -> {
                            // Si usa comillas simples, las convertimos a dobles
                            val fixed = raw.replace("'", "\"")
                            JSONObject(fixed)
                        }
                        else -> {
                            // Respuesta normal como JSONObject
                            JSONObject(raw)
                        }
                    }

                    val soniguales = datos.optBoolean("soniguales", false)

                    if (!soniguales) {
                        // Procesar delta del servidor
                        if (datos.has("delta")) {
                            val delta = datos.getJSONObject("delta")

                            // Procesar inserts
                            if (delta.has("inserts")) {
                                val inserts = delta.getJSONArray("inserts")
                                for (i in 0 until inserts.length()) {
                                    val registro = inserts.getJSONObject(i)
                                    dbCuenta.insert(registro)
                                }
                            }

                            // Procesar updates
                            if (delta.has("updates")) {
                                val updates = delta.getJSONArray("updates")
                                for (i in 0 until updates.length()) {
                                    val registro = updates.getJSONObject(i)
                                    dbCuenta.update(registro)
                                }
                            }

                            // Procesar deletes
                            if (delta.has("deletes")) {
                                val deletes = delta.getJSONArray("deletes")
                                for (i in 0 until deletes.length()) {
                                    val registro = deletes.getJSONObject(i)
                                    dbCuenta.rm(registro)
                                }
                            }

                            rellenarTicket()

                        } else {
                            // Fallback al método anterior si no viene delta
                            val lineas = if (datos.has("reg")) {
                                datos.getJSONArray("reg")
                            } else {
                                // Buscar el primer array en el objeto
                                val keys = datos.keys()
                                var found: org.json.JSONArray? = null
                                while (keys.hasNext()) {
                                    val k = keys.next()
                                    if (datos.opt(k) is org.json.JSONArray) {
                                        found = datos.getJSONArray(k)
                                        break
                                    }
                                }
                                found ?: org.json.JSONArray()
                            }

                            if (lineas.length() > 0) {
                                dbCuenta.replaceMesa(lineas, mesa.getString("ID"))
                                rellenarTicket()
                            } else {
                                val db = myServicio?.getDb("mesas") as DBMesas
                                db.cerrarMesa(mesa.getString("ID"))
                                finish()
                            }
                        }
                    }
                } catch (e: JSONException) {
                    android.util.Log.e("GET_CUENTA", "Error procesando respuesta: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun rellenarTicket() {
        try {
            val l = findViewById<TextView>(R.id.txtTotal)
            val lst = findViewById<ListView>(R.id.lstCuenta)
            val lineasTicket = dbCuenta.getLineasTicket(mesa.getString("ID"))
            totalMesa = String.format("%.2f", dbCuenta.getTotal(mesa.getString("ID")))
            l.text = buildString {
                append(totalMesa)
                append(" €")
            }
            val adaptador = AdaptadorTicket(cx, lineasTicket)
            lst.adapter = adaptador.toListAdapter() // Convierte AdaptadorTicket a un adaptador compatible con ListView
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cuenta)
        val lbl = findViewById<TextView>(R.id.lblMesa)
        try {
            server = intent.extras?.getString("url") ?: ""
            mesa = JSONObject(intent.extras?.getString("mesa") ?: "")
            lbl.text = "Mesa ${mesa.getString("Nombre")}"
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        if (myServicio == null) {
            val intent = Intent(applicationContext, ServiceCOM::class.java).apply {
                putExtra("url", server)
            }
            bindService(intent, mConexion, BIND_AUTO_CREATE)
        } else {
            rellenarTicket()
        }
        super.onResume()
    }

    override fun onDestroy() {
        unbindService(mConexion)
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
    }

    fun clickImprimir(v: View) {
        if (totalMesa.replace(",", ".").toDouble() > 0) {
            try {
                val p = ContentValues().apply {
                    put("idm", mesa.getString("ID"))
                }
                dbMesa.marcarRojo(mesa.getString("ID"))
                myServicio?.agregarInstruccion(Instruccion(p, "$server/impresion/preimprimir"))
                finish()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    fun clickMarcarRojo(v: View) {
        if (totalMesa.replace(",", ".").toDouble() > 0) {
            try {
                val p = ContentValues().apply {
                    put("idm", mesa.getString("ID"))
                }
                myServicio?.agregarInstruccion(Instruccion(p, "$server/comandas/marcar_rojo"))
                dbMesa.marcarRojo(mesa.getString("ID"))
                finish()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    fun clickSalir(v: View) {
        finish()
    }
}
