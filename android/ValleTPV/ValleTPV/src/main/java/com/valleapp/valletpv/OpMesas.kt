package com.valleapp.valletpv

import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tu.paquete.CustomToast
import com.valleapp.valletpv.tools.ServiceTPV
import com.valleapp.valletpvlib.db.DBCuenta
import com.valleapp.valletpvlib.db.DBMesas
import com.valleapp.valletpvlib.db.DBZonas
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.roundToInt

class OpMesas : AppCompatActivity() {

    private var dbMesas = DBMesas(this)
    private var dbZonas = DBZonas(this)
    private var dbCuenta = DBCuenta(this)

    private var servicioCom: ServiceTPV? = null

    private var server = ""
    private lateinit var mesa: JSONObject
    private lateinit var op: String
    private var lsmesas: JSONArray? = null
    private var lszonas: JSONArray? = null
    private var zn: JSONObject? = null
    private lateinit var cx: Context
    private val customToast = CustomToast(this)

    private val mConexion = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            servicioCom = (iBinder as ServiceTPV.MyBinder).service
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            servicioCom = null
        }
    }

    private fun rellenarZonas() {
        try {
            lszonas = dbZonas.getAll()

            if (lszonas!!.length() > 0) {
                val ll = findViewById<LinearLayout>(R.id.pneZonas)
                ll.removeAllViews()

                val metrics = resources.displayMetrics

                val params = LinearLayout.LayoutParams(
                    (metrics.density * 100).roundToInt(),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                params.setMargins(5, 0, 5, 0)
                for (i in 0 until lszonas!!.length()) {
                    val z = lszonas!!.getJSONObject(i)

                    if (zn == null && i == 0) zn = z
                    val btn = Button(cx)
                    btn.id = i
                    btn.isSingleLine = false
                    btn.textSize = 11f
                    btn.text = z.getString("Nombre").trim().replace(" ", "\n")
                    val rgb = z.getString("RGB").trim().split(",")
                    btn.setBackgroundColor(
                            Color.rgb(
                                    Integer.parseInt(rgb[0]),
                                    Integer.parseInt(rgb[1]),
                                    Integer.parseInt(rgb[2])
                            )
                    )

                    btn.setOnClickListener { view ->
                        try {
                            zn = lszonas!!.getJSONObject(view.id)
                            rellenarMesas()
                        } catch (e: JSONException) {
                            Log.e("OPMESAS_ERR_RELLENARZONA", e.toString())
                        }
                    }
                    ll.addView(btn, params)
                }

                rellenarMesas()
            }
        } catch (e: Exception) {
            Log.e("OPMESAS_ERR_RELLENARZONA", e.toString())
        }
    }

    private fun rellenarMesas() {
        try {
            lsmesas = dbMesas.getAllMenosUna(zn!!.getString("ID"), mesa.getString("ID"))

            if (lsmesas!!.length() > 0) {
                val ll = findViewById<TableLayout>(R.id.pneMesas)
                        ll.removeAllViews()
                val params = TableLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )

                val metrics = resources.displayMetrics

                val rowparams = TableRow.LayoutParams(
                    (metrics.density * 160).roundToInt(),
                    (metrics.density * 160).roundToInt()
                )

                rowparams.setMargins(5, 5, 5, 5)

                var row = TableRow(cx)
                ll.addView(row, params)

                for (i in 0 until lsmesas!!.length()) {
                    val m = lsmesas!!.getJSONObject(i)

                    val btn = Button(cx)
                    btn.id = i
                    btn.isSingleLine = false
                    btn.text = m.getString("Nombre")
                    btn.tag = m
                    btn.textSize = 15f
                    val rgb = m.getString("RGB").trim().split(",")
                    btn.setBackgroundColor(
                            Color.rgb(
                                    Integer.parseInt(rgb[0]),
                                    Integer.parseInt(rgb[1]),
                                    Integer.parseInt(rgb[2])
                            )
                    )

                    btn.setOnClickListener { view ->
                        try {
                            val m1 = view.tag as JSONObject
                            val p = ContentValues()
                            p.put("idp", mesa.getString("ID"))
                            p.put("ids", m1.getString("ID"))
                            val url = if (op == "cambiar") "cambiarmesas" else "juntarmesas"
                            if (servicioCom != null) {
                                servicioCom!!.opMesas(p, url)
                                finalizar(m1)
                            }
                        } catch (e: JSONException) {
                            Log.e("OPMESAS_ERR_RELLENARMESAS", e.toString())
                        }
                    }
                    row.addView(btn, rowparams)

                    if (((i + 1) % 5) == 0) {
                        row = TableRow(cx)
                        ll.addView(row, params)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("OPMESAS_ERR_RELLENARMESAS", e.toString())
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

        customToast.showBottom( "Realizando un cambio en las mesas.....", Toast.LENGTH_SHORT)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_op_mesas)
        cx = this
        server = intent.extras?.getString("url") ?: ""
        op = intent.extras?.getString("op") ?: ""
        try {
            mesa = JSONObject(intent.extras?.getString("mesa") ?: "")
            val l = findViewById<TextView>(R.id.lblTitulo)
            val titulo = if (op == "cambiar") "Cambiar mesa " + mesa.getString("Nombre") else "Juntar mesa " + mesa.getString("Nombre")
            l.text = titulo
        } catch (e: JSONException) {
            Log.e("OPMESAS_ERR_ONCREATE", e.toString())
        }

        rellenarZonas()
    }

    override fun onResume() {
        val intent = Intent(applicationContext, ServiceTPV::class.java)
        intent.putExtra("url", server)
        bindService(intent, mConexion, BIND_AUTO_CREATE)
        super.onResume()
    }

    override fun onDestroy() {
        unbindService(mConexion)
        super.onDestroy()
    }
}