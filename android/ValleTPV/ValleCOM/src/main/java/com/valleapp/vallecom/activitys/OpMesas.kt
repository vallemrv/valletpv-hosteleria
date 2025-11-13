package com.valleapp.vallecom.activitys


import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import com.valleapp.vallecom.utilidades.ActivityBase
import com.valleapp.vallecom.utilidades.ServiceCOM
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.db.DBCuenta
import com.valleapp.valletpvlib.db.DBMesas
import com.valleapp.valletpvlib.db.DBZonas
import com.valleapp.valletpvlib.tools.Instruccion
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.roundToInt
import com.valleapp.valletpvlib.R as LibR

class OpMesas : ActivityBase() {
    private lateinit var dbZonas: DBZonas
    private lateinit var dbMesas: DBMesas
    private lateinit var dbCuenta: DBCuenta
    private lateinit var mesa: JSONObject
    private lateinit var op: String
    private var lsmesas: JSONArray? = null
    private var lszonas: JSONArray? = null
    private var zn: JSONObject? = null
    private var artsToMove: JSONArray? = null // Usaremos este para guardar el array de artículos
    private lateinit var url: String
    private var servicioCom: ServiceCOM? = null

    private val mConexion: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            servicioCom = (iBinder as ServiceCOM.MyBinder).getService()
            if (servicioCom != null) {
                dbZonas = servicioCom!!.getDb("zonas") as DBZonas
                dbMesas = servicioCom!!.getDb("mesas") as DBMesas
                dbCuenta = servicioCom!!.getDb("lineaspedido") as DBCuenta
                rellenarZonas()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            servicioCom = null
        }
    }

    @SuppressLint("InflateParams")
    private fun rellenarZonas() {
        try {
            lszonas = dbZonas.getAll()
            if (lszonas!!.length() > 0) {
                val ll = findViewById<LinearLayout>(R.id.pneZonas)
                ll.removeAllViews()
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
                )
                params.setMargins(5, 0, 5, 0)
                for (i in 0 until lszonas!!.length()) {
                    val z = lszonas!!.getJSONObject(i)
                    if (zn == null && i == 0) zn = z
                    val inflater = cx.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val v = inflater.inflate(LibR.layout.btn_simple, null)
                    val btn = v.findViewById<Button>(LibR.id.btn_simple)
                    btn.id = i
                    btn.isSingleLine = false
                    btn.textSize = 11f
                    btn.text = z.getString("Nombre").trim { it <= ' ' }.replace(" ", "\n")
                    val rgb = z.getString("RGB").trim { it <= ' ' }.split(",").toTypedArray()
                    btn.setBackgroundColor(
                        Color.rgb(
                            rgb[0].trim { it <= ' ' }.toInt(),
                            rgb[1].trim { it <= ' ' }.toInt(),
                            rgb[2].trim { it <= ' ' }.toInt()
                        )
                    )
                    btn.setOnClickListener { view ->
                        try {
                            zn = lszonas!!.getJSONObject(view.id)
                            rellenarMesas()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    ll.addView(v, params)
                }
                rellenarMesas()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun rellenarMesas() {
        try {
            lsmesas = dbMesas.getAll(zn!!.getString("ID"))
            if (lsmesas!!.length() > 0) {
                val ll = findViewById<TableLayout>(R.id.pneMesas)
                ll.removeAllViews()
                val params = TableLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val metrics = resources.displayMetrics
                val rowparams = TableRow.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (metrics.density * 120).roundToInt()
                )
                rowparams.setMargins(5, 5, 5, 5)
                var row = TableRow(cx)
                ll.addView(row, params)
                for (i in 0 until lsmesas!!.length()) {
                    val mShow = lsmesas!!.getJSONObject(i)
                    val inflater = cx.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val v = inflater.inflate(LibR.layout.btn_simple, ll, false)
                    val btn = v.findViewById<Button>(LibR.id.btn_simple)
                    btn.id = i
                    btn.isSingleLine = false
                    btn.text = mShow.getString("Nombre")
                    btn.tag = mShow
                    btn.textSize = 15f
                    val rgb = mShow.getString("RGB").trim { it <= ' ' }.split(",").toTypedArray()
                    if (mShow.getInt("ID") != mesa.getInt("ID")) {
                        btn.setBackgroundColor(
                            Color.rgb(
                                rgb[0].trim { it <= ' ' }.toInt(),
                                rgb[1].trim { it <= ' ' }.toInt(),
                                rgb[2].trim { it <= ' ' }.toInt()
                            )
                        )
                        btn.setOnClickListener { view ->
                            try {
                                val m = view.tag as JSONObject
                                val p = ContentValues()
                                // Aquí comprobamos si estamos moviendo artículos o mesas completas
                                if (op == "arts") { // Si la operación es para mover artículos (uno o más)
                                    val idm = m.getString("ID") // ID de la mesa destino
                                    val idLineas = mutableListOf<String>()

                                    // Iteramos sobre el array de artículos a mover
                                    artsToMove?.let {
                                        for (j in 0 until it.length()) {
                                            val obj = it.getJSONObject(j)
                                            val idLinea = obj.getString("ID")
                                            idLineas.add(idLinea)
                                            dbCuenta.cambiarLinea(idLinea, idm) // Mueve cada línea en la DB local
                                        }
                                    }
                                    p.put("idm", idm)
                                    p.put("idlineas", JSONArray(idLineas).toString()) // Enviamos el array de IDs de líneas
                                    url = "$server/cuenta/mvlineamultiple" // Usamos siempre la URL para múltiples líneas
                                } else { // Si la operación es para cambiar/juntar mesas completas
                                    p.put("idp", mesa.getString("ID"))
                                    p.put("ids", m.getString("ID"))
                                    finalizar(m) // Llama a la lógica de mesas completas
                                }

                                if (servicioCom != null) {
                                    servicioCom!!.agregarInstruccion(Instruccion(p, url))
                                    finish()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        btn.setBackgroundResource(LibR.drawable.bg_pink)
                        btn.setOnClickListener {
                            Toast.makeText(cx, "Esta es la misma mesa..", Toast.LENGTH_SHORT).show()
                        }
                    }
                    row.addView(v, rowparams)
                    if ((i + 1) % 3 == 0) {
                        row = TableRow(cx)
                        ll.addView(row, params)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(JSONException::class)
    private fun finalizar(m: JSONObject) {
        if (op == "cambiar") {
            if (m.getString("abierta") == "0") {
                dbMesas.abrirMesa(m.getString("ID"))
                dbMesas.cerrarMesa(mesa.getString("ID"))
                dbCuenta.cambiarCuenta(mesa.getString("ID"), m.getString("ID"))
            } else {
                dbCuenta.cambiarCuenta(mesa.getString("ID"), "-100")
                dbCuenta.cambiarCuenta(m.getString("ID"), mesa.getString("ID"))
                dbCuenta.cambiarCuenta("-100", m.getString("ID"))
            }
        } else {
            if (m.getString("abierta") == "1") {
                dbMesas.cerrarMesa(m.getString("ID"))
                dbCuenta.cambiarCuenta(m.getString("ID"), mesa.getString("ID"))
            } else {
                dbMesas.abrirMesa(m.getString("ID"))
            }
        }
        Toast.makeText(applicationContext, "Realizando un cambio en las mesas.....", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_op_mesas)
        server = intent.extras!!.getString("url")!!
        op = intent.extras!!.getString("op")!!
        try {
            val l = findViewById<TextView>(R.id.title)
            var titulo = ""
            mesa = JSONObject(intent.extras!!.getString("mesa")!!)

            // Aquí se unifica la lógica para "art" y "artmultiple"
            if (op == "arts") { // Ahora se espera siempre un array de artículos
                titulo = "Cambiar artículo(s) " + mesa.getString("Nombre")
                // Siempre esperamos un JSONArray para 'artsToMove'
                // Si antes venía un solo JSONObject en "art", ahora debe venir envuelto en un JSONArray.
                // Si antes venía en "reg" para "artmultiple", ahora debe venir como "artsToMove"
                artsToMove = JSONArray(intent.extras!!.getString("arts")!!) // Cambiamos de "art" o "reg" a "arts"
                url = "$server/cuenta/mvlineamultiple" // Siempre usaremos la URL para múltiples líneas
            } else { // El resto de operaciones (cambiar/juntar mesas) quedan igual
                url = if (op == "cambiar") "$server/cuenta/cambiarmesas" else "$server/cuenta/juntarmesas"
                titulo = (if (op == "cambiar") "Cambiar mesa " else "Juntar mesa ") + mesa.getString("Nombre")
            }
            l.text = titulo
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        if (servicioCom == null) {
            val intent = Intent(applicationContext, ServiceCOM::class.java)
            intent.putExtra("url", server)
            bindService(intent, mConexion, BIND_AUTO_CREATE)
        } else {
            rellenarZonas()
        }
        super.onResume()
    }

    override fun onDestroy() {
        unbindService(mConexion)
        super.onDestroy()
    }
}