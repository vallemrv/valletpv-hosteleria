package com.valleapp.vallecom.activitys

import android.annotation.SuppressLint
import android.content.*
import android.os.*

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.valleapp.vallecom.utilidades.ActivityBase
import com.valleapp.vallecom.utilidades.ServiceCOM
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import com.valleapp.valletpvlib.db.DBCuenta
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MostrarPedidos : ActivityBase() {

    private lateinit var mesa: JSONObject
    private lateinit var dbCuenta: DBCuenta
    private var btnMultiple: Button? = null

    private var seleccionMultiple = JSONArray() // Ya es un JSONArray

    private val mConexion = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            try {
                myServicio = (iBinder as ServiceCOM.MyBinder).getService()
                myServicio?.setExHandler("lineaspedido", handlerHttp)
                dbCuenta = myServicio?.getDb("lineaspedido") as DBCuenta
                val p = ContentValues().apply {
                    put("mesa_id", mesa.getString("ID"))
                    put("reg", dbCuenta.filter("IDMesa=" + mesa.getString("ID")).toString())
                    put("uid", myServicio?.getUid())
                }
                HTTPRequest("$server/cuenta/get_cuenta", p, "actualizar", handlerHttp)
                rellenarPedido()
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
            val res = msg.data.getString("RESPONSE")
            if (op == "actualizar") {
                try {
                    val datos = JSONObject(res.toString())
                    val soniguales = datos.getBoolean("soniguales")

                    if (!soniguales) {
                        dbCuenta.replaceMesa(datos.getJSONArray("reg"), mesa.getString("ID"))
                    }
                    rellenarPedido()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else if (op != null) {
                customToast.showBottom("Petición enviada....")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun rellenarPedido() {
        try {
            findViewById<LinearLayout>(R.id.pneSendMultiple).visibility = View.GONE
            // Limpiar seleccionMultiple al volver al modo normal
            seleccionMultiple = JSONArray()
            val txtSendMultiple = findViewById<TextView>(R.id.txtSendMultiple)
            txtSendMultiple.text = "0"
            btnMultiple?.text = "Multiple"

            val lineas = dbCuenta.getLineasByPedido("Estado != 'A' and Estado != 'C' and IDMesa = ${mesa.getString("ID")}")
            val ll = findViewById<LinearLayout>(R.id.pneListado)
            ll.removeAllViews()

            if (lineas.length() > 0) {
                val metrics = resources.displayMetrics
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (metrics.density * 80f).toInt()
                ).apply {
                    setMargins(5, 0, 5, 0)
                }

                for (i in 0 until lineas.length()) {
                    val art = lineas.getJSONObject(i)
                    val inflater = cx.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val v = inflater.inflate(R.layout.linea_pedido_interno, ll, false)
                    val t = v.findViewById<TextView>(R.id.lblCantidad)
                    val s = v.findViewById<TextView>(R.id.lblNombre)
                    t.text = art.getString("Can")
                    s.text = art.getString("Descripcion")
                    val btnCamb = v.findViewById<ImageButton>(R.id.btnCambiar)
                    btnCamb.tag = art
                    btnCamb.setOnClickListener { view -> clickCambiar(view) } // Asignar el click listener
                    s.tag = art
                    s.isLongClickable = true

                    s.setOnLongClickListener {
                        clickPedir(it)
                        false
                    }

                    ll.addView(v, params)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun rellenarPedidoMultiple() {
        try {
            findViewById<LinearLayout>(R.id.pneSendMultiple).visibility = View.VISIBLE
            val lineas = dbCuenta.filter("Estado != 'A' and Estado != 'C' and IDMesa = ${mesa.getString("ID")}")
            val ll = findViewById<LinearLayout>(R.id.pneListado)
            ll.removeAllViews()

            if (lineas.length() > 0) {
                val metrics = resources.displayMetrics
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (metrics.density * 80f).toInt()
                ).apply {
                    setMargins(5, 0, 5, 0)
                }

                for (i in 0 until lineas.length()) {
                    val art = lineas.getJSONObject(i)
                    val inflater = cx.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val v = inflater.inflate(R.layout.linea_pedido_interno, ll, false)
                    val t = v.findViewById<TextView>(R.id.lblCantidad)
                    val s = v.findViewById<TextView>(R.id.lblNombre)
                    t.text = "1"
                    s.text = art.getString("Descripcion")
                    val btnCamb = v.findViewById<ImageButton>(R.id.btnCambiar)
                    btnCamb.tag = art
                    btnCamb.setOnClickListener { view -> clickCambiar(view) } // Asignar el click listener
                    s.tag = art
                    s.isLongClickable = true


                    s.setOnLongClickListener {
                        clickPedir(it)
                        false
                    }

                    ll.addView(v, params)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mostrar_pedidos)
        val lbl = findViewById<TextView>(R.id.lblMesa)
        findViewById<Button>(R.id.btnSalir).setOnClickListener { clickSalir() }
        btnMultiple = findViewById(R.id.btnMultiple)
        btnMultiple?.setOnClickListener {
            if (btnMultiple?.text.toString().equals("Multiple", ignoreCase = true)) {
                rellenarPedidoMultiple()
                btnMultiple?.text = "Cancelar"
            } else {
                rellenarPedido()
                btnMultiple?.text = "Multiple"
            }
        }
        findViewById<ImageButton>(R.id.btnSendMultiple).setOnClickListener { clickSend() }
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
            rellenarPedido()
        }
        super.onResume()
    }

    override fun onDestroy() {
        unbindService(mConexion)
        super.onDestroy()
    }

    fun clickPedir(v: View) {
        try {
            val obj = v.tag as JSONObject
            val p = ContentValues().apply {
                put("idp", obj.getString("IDPedido"))
                put("id", obj.getString("IDArt"))
                put("Descripcion", obj.getString("Descripcion"))
                put("uid", myServicio?.getUid())
            }
            HTTPRequest("$server/impresion/reenviarlinea", p, "", handlerHttp)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun clickCambiar(v: View) {
        val m = v.tag as JSONObject
        if (btnMultiple?.text.toString().equals("Cancelar", ignoreCase = true)) {
            // Modo múltiple: agregar a seleccionMultiple
            seleccionMultiple.put(m)
            v.visibility = View.GONE // Oculta el elemento al seleccionarlo

            // Actualizar el número en txtSendMultiple
            val txtSendMultiple = findViewById<TextView>(R.id.txtSendMultiple)
            txtSendMultiple.text = seleccionMultiple.length().toString()

        } else {
            // Modo normal: siempre enviar como JSONArray de un solo elemento
            val articlesArray = JSONArray().put(m) // Envuelve el JSONObject en un JSONArray
            val intent = Intent(cx, OpMesas::class.java).apply {
                putExtra("op", "arts") // Cambiado a "arts"
                putExtra("mesa", mesa.toString())
                putExtra("arts", articlesArray.toString()) // Cambiado a "arts"
                putExtra("url", server)
            }
            startActivity(intent)
        }
    }

    fun clickSend() {
        val intent = Intent(cx, OpMesas::class.java).apply {
            putExtra("op", "arts") // Siempre "arts"
            putExtra("mesa", mesa.toString())
            putExtra("arts", seleccionMultiple.toString()) // Envía el JSONArray completo
            putExtra("url", server)
        }
        startActivity(intent)
    }

    fun clickSalir() {
        finish()
    }
}