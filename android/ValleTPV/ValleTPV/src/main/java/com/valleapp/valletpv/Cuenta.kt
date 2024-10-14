package com.valleapp.valletpv

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import com.valleapp.valletpv.adaptadoresDatos.AdaptadorTicket
import com.valleapp.valletpv.cashlogyActivitis.CobroCashlogyActivity
import com.valleapp.valletpv.dlg.DlgCobrar
import com.valleapp.valletpv.dlg.DlgPedirAutorizacion
import com.valleapp.valletpv.dlg.DlgSepararTicket
import com.valleapp.valletpv.dlg.DlgVarios
import com.valleapp.valletpv.interfaces.IAutoFinish
import com.valleapp.valletpv.interfaces.IControladorAutorizaciones
import com.valleapp.valletpv.interfaces.IControladorCuenta
import com.valleapp.valletpv.tools.ServiceCOM
import com.valleapp.valletpv.tpvcremoto.CobroTarjetaActivity
import com.valleapp.valletpvlib.db.DBCamareros
import com.valleapp.valletpvlib.db.DBCuenta
import com.valleapp.valletpvlib.db.DBMesas
import com.valleapp.valletpvlib.db.DBSecciones
import com.valleapp.valletpvlib.db.DBSubTeclas
import com.valleapp.valletpvlib.db.DBTeclas
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class Cuenta : Activity(), TextWatcher, IControladorCuenta, IControladorAutorizaciones, IAutoFinish {

    private var server = ""
    private var dbSecciones: DBSecciones? = null
    private var dbTeclas: DBTeclas? = null
    private var dbCuenta: DBCuenta? = null
    private var dbMesas: DBMesas? = null
    private var dbSubteclas: DBSubTeclas? = null

    private var cam: JSONObject? = null
    private var mesa: JSONObject? = null
    private var artSel: JSONObject? = null

    private var lineas: List<JSONObject>? = null
    private var lsartresul: JSONArray? = null

    private var totalMesa = 0.00

    private var tipo = ""
    private var sec = ""
    private var cantidad = 1
    private var reset = true
    private var stop = false
    private var timerAutoCancel: Timer? = null

    private val autoCancel: Long = 10000

    private var myServicio: ServiceCOM? = null

    private val cx: Context = this

    private var dlgCobrar: DlgCobrar? = null
    private var canRefresh = true

    private val mConexion = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            try {
                myServicio = (iBinder as ServiceCOM.MyBinder).service
                myServicio?.setExHandler("lineaspedido", handlerHttp)
                myServicio?.setExHandler("teclas", handlerSeccionesTeclas)
                myServicio?.setExHandler("secciones", handlerSeccionesTeclas)
                dbCuenta = myServicio?.getDb("lineaspedido") as DBCuenta
                dbMesas = myServicio?.getDb("mesas") as DBMesas
                dbSecciones = myServicio?.getDb("secciones") as DBSecciones
                dbTeclas = myServicio?.getDb("teclas") as DBTeclas
                dbSubteclas = myServicio?.getDb("subteclas") as DBSubTeclas
                rellenarSecciones()
                rellenarTicket()

                if (tipo == "c") {
                    val t = Timer()
                    findViewById<View>(R.id.loading).visibility = View.VISIBLE
                    t.schedule(object : TimerTask() {
                        override fun run() {
                            handlerMostrarCobrar.sendEmptyMessage(0)
                        }
                    }, 1000)
                }
                myServicio?.mesaAbierta(mesa)
                getCuenta()
            } catch (e: Exception) {
                Log.e("ERROR_CUENTA", e.toString())
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            myServicio = null
        }
    }

    @SuppressLint("HandlerLeak")
    private val handlerMostrarCobrar = @SuppressLint("HandlerLeak")
    object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            try {
                val idMesa = mesa?.getString("ID") ?: return
                mostrarCobrar(dbCuenta!!.filterGroup("IDMesa=$idMesa"), totalMesa)
                findViewById<View>(R.id.loading).visibility = View.GONE
            } catch (e: Exception) {
                Log.e("ERROR_CUENTA", e.toString())
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private val mostrarBusqueda = @SuppressLint("HandlerLeak")
    object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            rellenarArticulos(lsartresul)
        }
    }

    @SuppressLint("HandlerLeak")
    private val handlerSeccionesTeclas = @SuppressLint("HandlerLeak")
    object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            rellenarSecciones()
        }
    }

    @SuppressLint("HandlerLeak")
    private val handlerHttp = @SuppressLint("HandlerLeak")
    object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            try {
                reset = true
                val res = msg.data.getString("RESPONSE")
                if (res != null) {
                    val datos = JSONObject(res)
                    if (datos.getBoolean("soniguales")) return

                    val reg = datos.getJSONArray("reg")
                    dbCuenta!!.replaceMesa(reg, mesa!!.getString("ID"))
                    rellenarTicket()
                } else {
                    if (canRefresh) rellenarTicket()
                }
            } catch (e: JSONException) {
                Log.e("ERROR_CUENTA", e.toString())
            }
        }
    }

    private fun rellenarSecciones() {
        try {
            val lssec = dbSecciones?.all ?: return
            if (lssec.length() > 0) {
                val ll = findViewById<LinearLayout>(R.id.pneSecciones)
                ll.removeAllViews()

                val metrics = resources.displayMetrics

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, Math.round(metrics.density * 100)
                )
                params.setMargins(5, 5, 5, 5)

                for (i in 0 until lssec.length()) {
                    val z = lssec.getJSONObject(i)

                    if (sec.isEmpty() && i == 0) sec = z.getString("ID")

                    val btn = Button(cx)
                    btn.id = z.getInt("ID")
                    btn.isSingleLine = false
                    btn.text = z.getString("Nombre")
                    btn.tag = z.getString("ID")
                    btn.textSize = 16f
                    val rgbStr = z.getString("RGB")
                    if (rgbStr.isEmpty()) {
                        btn.setBackgroundResource(R.drawable.bg_pink)
                    } else {
                        val rgb = rgbStr.trim().split(",").toTypedArray()
                        btn.setBackgroundColor(
                            Color.rgb(
                                rgb[0].toInt(),
                                rgb[1].toInt(),
                                rgb[2].toInt()
                            )
                        )
                    }
                    btn.setOnClickListener {
                        setEstadoAutoFinish(r = true, s = false)
                        sec = it.tag.toString()
                        try {
                            val lsart = dbTeclas!!.getAll(sec, mesa!!.getInt("Tarifa"))
                            rellenarArticulos(lsart)
                            lsartresul = lsart
                        } catch (e: JSONException) {
                            Log.e("ERROR_CUENTA", e.toString())
                        }
                    }

                    btn.setOnLongClickListener {
                        asociarBotonera(it)
                        false
                    }
                    ll.addView(btn, params)
                }

                val lsart = dbTeclas!!.getAll(sec, mesa!!.getInt("Tarifa"))
                rellenarArticulos(lsart)
                lsartresul = lsart
            }
        } catch (e: Exception) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }

    private fun getCuenta() {
        if (myServicio != null && mesa != null) {
            try {
                val p = ContentValues()
                p.put("reg", dbCuenta!!.filter("IDMesa=" + mesa!!.getString("ID")).toString())
                p.put("idm", mesa!!.getString("ID"))
                myServicio!!.getCuenta(handlerHttp, p)
            } catch (e: JSONException) {
                Log.e("ERROR_CUENTA", "Error al cargar la cuenta")
            }
        }
    }

    private fun rellenarArticulos(lsart: JSONArray?) {
        try {
            if (lsart != null && lsart.length() > 0) {
                val ll = findViewById<TableLayout>(R.id.pneArt)
                ll.removeAllViews()

                val metrics = resources.displayMetrics

                val params = TableLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, Math.round(metrics.density * 100)
                )

                val rowparams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT
                )
                rowparams.setMargins(5, 5, 5, 5)
                rowparams.weight = 1f

                var row = LinearLayout(cx)
                row.orientation = LinearLayout.HORIZONTAL

                ll.addView(row, params)

                for (i in 0 until lsart.length()) {
                    val m = lsart.getJSONObject(i)

                    val btn = Button(cx)
                    btn.id = i
                    btn.tag = m

                    if (m.has("RGB")) {
                        btn.text =
                            String.format(Locale.getDefault(), "%s\n%01.2f €",
                                m.getString("Nombre"), m.getDouble("Precio"))

                        val rgb = m.getString("RGB").split(",").toTypedArray()
                        btn.setBackgroundColor(
                            Color.rgb(
                                rgb[0].toInt(),
                                rgb[1].toInt(),
                                rgb[2].toInt()
                            )
                        )

                        btn.setOnClickListener { view ->
                            try {
                                artSel = (view.tag as JSONObject).apply {
                                    put("Can", cantidad)
                                    put("Descripcion", componerDescripcion(this, "descripcion_r"))
                                    put("descripcion_t", componerDescripcion(this, "descripcion_t"))
                                }
                                if (artSel!!.getString("tipo") == "SP") {
                                    pedirArt(artSel!!)
                                } else {
                                    rellenarArticulos(dbSubteclas!!.getAll(artSel!!.getString("ID")))
                                }
                            } catch (e: Exception) {
                                Log.e("ERROR_CUENTA", e.toString())
                            }
                        }
                    } else {
                        val precio = artSel!!.getDouble("Precio") + m.getDouble("Incremento")
                        btn.text = String.format(Locale.getDefault(), "%s\n%01.2f €", m.getString("Nombre"), precio)


                        btn.setBackgroundResource(R.drawable.bg_pink)
                        btn.setOnClickListener { view ->
                            try {
                                val sub = view.tag as JSONObject
                                val it = intent
                                var des = sub.getString("descripcion_r")
                                if (des != "null" && des.isNotEmpty()) {
                                    artSel!!.put("Descripcion", des)
                                } else {
                                    val nom = artSel!!.getString("Descripcion")
                                    val subnom = sub.getString("Nombre")
                                    artSel!!.put("Descripcion", "$nom $subnom")
                                }
                                des = sub.getString("descripcion_t")
                                if (des != "null" && des.isNotEmpty()) {
                                    artSel!!.put("descripcion_t", des)
                                } else if (artSel!!.getString("descripcion_t") == artSel!!.getString("Nombre")) {
                                    val nom = artSel!!.getString("descripcion_t")
                                    val subnom = sub.getString("Nombre")
                                    artSel!!.put("descripcion_t", "$nom $subnom")
                                }
                                artSel!!.put("Precio", precio)
                                it.putExtra("art", artSel.toString())
                                setResult(RESULT_OK, it)
                                pedirArt(artSel!!)
                                rellenarArticulos(lsartresul)
                            } catch (e: Exception) {
                                Log.e("ERROR_CUENTA", e.toString())
                            }
                        }
                    }
                    row.addView(btn, rowparams)

                    if ((i + 1) % 5 == 0) {
                        row = LinearLayout(cx)
                        row.orientation = LinearLayout.HORIZONTAL
                        row.minimumHeight = 130

                        ll.addView(row, params)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }

    private fun componerDescripcion(o: JSONObject, descipcion: String): String {
        return try {
            val des = o.getString(descipcion)
            if (des != "null" && des.isNotEmpty()) des else o.getString("Nombre")
        } catch (e: Exception) {
            Log.e("ERROR_CUENTA", e.toString())
            ""
        }
    }

    private fun rellenarTicket() {
        try {
            resetCantidad()
            lineas = dbCuenta?.getAll(mesa!!.getString("ID"))
            totalMesa = dbCuenta!!.getTotal(mesa!!.getString("ID"))

            if (dlgCobrar != null) {
                dlgCobrar?.dismiss()
                val t = Timer()
                findViewById<View>(R.id.loading).visibility = View.VISIBLE
                t.schedule(object : TimerTask() {
                    override fun run() {
                        handlerMostrarCobrar.sendEmptyMessage(0)
                    }
                }, 1000)
            }

            val l = findViewById<TextView>(R.id.lblPrecio)
            val lst = findViewById<ListView>(R.id.lstCamareros)
            l.text = String.format(Locale.getDefault(),"%01.2f €", totalMesa)
            lst.adapter = AdaptadorTicket(cx, lineas as ArrayList<JSONObject>, this)
        } catch (e: JSONException) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }

    fun mostrarSeparados(v: View?) {
        if (totalMesa > 0) {
            try {
                setEstadoAutoFinish(r = true, s = true)
                aparcar(mesa!!.getString("ID"), dbCuenta!!.getNuevos(mesa!!.getString("ID")))
                lineas = dbCuenta!!.getAll(mesa!!.getString("ID"))
                val dlg = DlgSepararTicket(this, this)
                dlg.setTitle("Separar ticket " + mesa!!.getString("Nombre"))
                dlg.setLineasTicket(lineas!!)
                dlg.show()
            } catch (e: JSONException) {
                Log.e("ERROR_CUENTA", e.toString())
            }
        }
    }

    fun mostrarVarios(v: View?) {
        setEstadoAutoFinish(true, true)
        val dlg = DlgVarios(this, this)
        dlg.show()
    }


    fun preImprimir(v: View?) {
        try {
            setEstadoAutoFinish(r = true, s = false)
            aparcar(mesa!!.getString("ID"), dbCuenta!!.getNuevos(mesa!!.getString("ID")))

            lineas = dbCuenta!!.getAll(mesa!!.getString("ID"))

            if (totalMesa > 0) {
                val p = ContentValues()
                p.put("idm", mesa!!.getString("ID"))
                myServicio?.preImprimir(p)
                dbMesas?.marcarRojo(mesa!!.getString("ID"))
            }
        } catch (e: JSONException) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }

    fun abrirCajon() {
        setEstadoAutoFinish(r = true, s = false)
        val dbCamareros = myServicio?.getDb("camareros") as DBCamareros?
        if (dbCamareros?.getConPermiso("abrir_cajon")?.isNotEmpty() == true) {
            try {
                val p = JSONObject()
                p.put("idc", cam!!.getString("ID"))
                val dlg = DlgPedirAutorizacion(cx, this, dbCamareros, this, p, "abrir_cajon")
                dlg.show()
            } catch (e: JSONException) {
                Log.e("ERROR_CUENTA", e.toString())
            }
        } else {
            myServicio?.abrirCajon()
        }
    }

    @SuppressLint("DefaultLocale")
    fun cobrarMesa(v: View?) {
        try {
            aparcar(mesa!!.getString("ID"), dbCuenta!!.getNuevos(mesa!!.getString("ID")))
            val t = Timer()
            findViewById<View>(R.id.loading).visibility = View.VISIBLE
            t.schedule(object : TimerTask() {
                override fun run() {
                    handlerMostrarCobrar.sendEmptyMessage(0)
                    try {
                        val l = dbCuenta!!.filterGroup("IDMesa=" + mesa!!.getString("ID"))
                        mostrarCobrar(l, totalMesa)
                    } catch (e: JSONException) {
                        Log.e("ERROR_CUENTA", e.toString())
                    }
                }
            }, 1000)
        } catch (e: JSONException) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // data es opcional
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) { // Este es el código que usamos en startActivityForResult
            if (resultCode == RESULT_OK) {
                // Verificar si el Intent no es nulo
                data?.let {
                    // Cobro exitoso
                    val totalIngresado = it.getDoubleExtra("totalIngresado", 0.0)

                    // Recibir los datos adicionales
                    val totalMesa = it.getDoubleExtra("totalMesa", 0.0)
                    val lineasString = it.getStringExtra("lineas")
                    val recibo = it.getStringExtra("recibo") ?: ""

                    // Convertir lineasString de vuelta a JSONArray si lo necesitas
                    var lst: JSONArray? = null
                    try {
                        lst = JSONArray(lineasString)
                    } catch (e: JSONException) {
                        Log.e("ERROR_CUENTA", "Error al convertir lineasString a JSONArray: $e")
                    }

                    lst?.let { jsonArray ->
                        cobrar(jsonArray, totalMesa, totalIngresado, recibo)
                    } ?: run {
                        Log.e("ERROR_CUENTA", "El JSONArray 'lineas' es nulo")
                    }

                } ?: run {
                    Log.e("Cobro", "Data es nulo aunque el resultado fue OK")
                }
            } else if (resultCode == RESULT_CANCELED) {
                // Cobro cancelado
                Toast.makeText(this, "Cobro cancelado", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun aparcar(idm: String, nuevos: JSONArray) {
        if (nuevos.length() > 0) {
            val p = ContentValues()
            p.put("idm", idm)
            p.put("idc", cam!!.getString("ID"))
            p.put("pedido", nuevos.toString())
            myServicio?.nuevoPedido(p)
            dbMesas?.abrirMesa(idm)
            canRefresh = false
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    canRefresh = true
                }
            }, 2000)
        }
    }

    @SuppressLint("SetTextI18n")
    fun clickCantidad(v: View) {
        setEstadoAutoFinish(r = true, s = false)
        cantidad = (v as Button).text.toString().toInt()
        val lbl = findViewById<TextView>(R.id.lblCantida)
        lbl.text = "Cantidad $cantidad"
    }

    fun abrirCajon(v: View?){
        if (!myServicio!!.usaCashlogy()){
            myServicio!!.abrirCajon()
        }
    }

    override fun pedirArt(art: JSONObject) {
        try {
            setEstadoAutoFinish(r = true, s = false)
            dbCuenta?.addArt(mesa!!.getInt("ID"), art)
            rellenarTicket()
        } catch (e: JSONException) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cuenta)
        cargarPreferencias()

        val bus = findViewById<EditText>(R.id.txtBuscar)
        bus.addTextChangedListener(this)

        try {
            server = intent.extras?.getString("url") ?: ""
            cam = JSONObject(intent.extras?.getString("cam") ?: "{}")
            mesa = JSONObject(intent.extras?.getString("mesa") ?: "{}")
            tipo = intent.extras?.getString("op") ?: ""

            val title = findViewById<TextView>(R.id.txtTitulo)
            title.text = cam!!.getString("nombre") + " " +
                    cam!!.getString("apellidos") + " -- " + mesa!!.getString("Nombre")
        } catch (e: JSONException) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }

    override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
        reset = true
        if (charSequence.isNotEmpty()) {
            try {
                val str = charSequence.toString()
                val t = mesa!!.getString("Tarifa")
                Thread {
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        Log.e("ERROR_CUENTA", e.toString())
                    }
                    lsartresul = dbTeclas!!.findLike(str, t)
                    mostrarBusqueda.sendEmptyMessage(1)
                }.start()
            } catch (e: JSONException) {
                Log.e("ERROR_CUENTA", e.toString())
            }
        }
    }

    override fun afterTextChanged(editable: Editable) {}

    override fun onPause() {
        stop = true
        try {
            val idm = mesa!!.getString("ID")
            aparcar(idm, dbCuenta!!.getNuevos(idm))
        } catch (e: Exception) {
            Log.e("ERROR_CUENTA", e.toString())
        }
        super.onPause()
    }

    override fun onDestroy() {
        timerAutoCancel?.cancel()
        myServicio?.mesaAbierta(null)
        unbindService(mConexion)
        super.onDestroy()
    }

    override fun onResume() {
        try {
            stop = false
            if (timerAutoCancel == null) {
                timerAutoCancel = Timer()
                timerAutoCancel!!.schedule(object : TimerTask() {
                    override fun run() {
                        if (!stop) {
                            if (!reset) {
                                finish()
                            } else reset = false
                        }
                    }
                }, 5000, autoCancel)
            }

            val intent = Intent(applicationContext, ServiceCOM::class.java)
            intent.putExtra("url", server)
            bindService(intent, mConexion, Context.BIND_AUTO_CREATE)
            findViewById<View>(R.id.loading).visibility = View.GONE
        } catch (e: Exception) {
            Log.e("ERROR_CUENTA", e.toString())
        }
        super.onResume()
    }

    override fun setEstadoAutoFinish(r: Boolean, s: Boolean) {
        tipo = "m"
        reset = r
        stop = s
    }

    override fun mostrarCobrar(lsart: JSONArray, totalCobro: Double) {
        if (totalCobro > 0) {
            try {
                setEstadoAutoFinish(r = true, s = true)
                runOnUiThread {  // Asegúrate de que esta parte se ejecute en el hilo principal
                    if (dlgCobrar != null) dlgCobrar!!.dismiss()
                    dlgCobrar = DlgCobrar(
                        this@Cuenta, this@Cuenta,
                        myServicio!!.usaCashlogy(),
                        myServicio!!.usaTPV()
                    )
                    dlgCobrar!!.setTitle("Cobrar " + mesa!!.getString("Nombre"))
                    dlgCobrar!!.setDatos(lsart, totalCobro)
                    dlgCobrar!!.setOnDismissListener { dlgCobrar = null }
                    if (!this.isFinishing && !this.isDestroyed) {
                        // Mostrar el diálogo o Snackbar
                        dlgCobrar!!.show()
                    }

                }
            } catch (e: JSONException) {
                Log.e("ERROR_CUENTA", e.toString())
            }
        }
    }

    override fun cobrarConCashlogy(lsart: JSONArray, totalCobro: Double) {
        val intent = Intent(this, CobroCashlogyActivity::class.java)
        intent.putExtra("totalMesa", totalCobro)
        intent.putExtra("lineas", lsart.toString())
        startActivityForResult(intent, 1)
    }

    override fun cobrarConTpvPC(lsart: JSONArray, totalCobro: Double) {
        val intent = Intent(this, CobroTarjetaActivity::class.java)
        intent.putExtra("totalMesa", totalCobro)
        intent.putExtra("lineas", lsart.toString())
        intent.putExtra("urlTPVPC", myServicio?.getIPTPV())
        startActivityForResult(intent, 1)  // Cambia 2 si deseas otro requestCode
    }



    override fun cobrar(lsart: JSONArray, totalCobro: Double, entrega: Double, recibo: String) {
        try {
            setEstadoAutoFinish(r = true, s = true)
            dlgCobrar?.dismiss()
            dlgCobrar = null

            val p = ContentValues()
            p.put("idm", mesa!!.getString("ID"))
            p.put("idc", cam!!.getString("ID"))
            p.put("entrega", entrega.toString())
            p.put("art", lsart.toString())
            p.put("recibo", recibo)

            dbCuenta!!.eliminar(mesa!!.getString("ID"), lsart)
            myServicio?.cobrarCuenta(p)
            if (totalCobro - totalMesa == 0.0) {
                dbMesas!!.cerrarMesa(mesa!!.getString("ID"))
                finish()
            } else {
                rellenarTicket()
            }
            sendMessageMesaCobrada(entrega, entrega - totalCobro)

        } catch (e: JSONException) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }

    @SuppressLint("SetTextI18n")
    override fun clickMostrarBorrar(obj: JSONObject) {
        setEstadoAutoFinish(r = true, s = true)

        val dlg = Dialog(cx)
        dlg.setContentView(R.layout.borrar_art)
        dlg.setOnCancelListener { setEstadoAutoFinish(r = true, s = false) }

        dlg.setTitle("Borrar artículos")
        val motivo = dlg.findViewById<EditText>(R.id.txtMotivo)
        val error = dlg.findViewById<Button>(R.id.btnError)
        val simpa = dlg.findViewById<Button>(R.id.btnSimpa)
        val inv = dlg.findViewById<Button>(R.id.btnInv)
        val ok = dlg.findViewById<ImageButton>(R.id.btn_ok)
        val edit = dlg.findViewById<ImageButton>(R.id.btnEdit)
        val exit = dlg.findViewById<ImageButton>(R.id.btn_salir_monedas)
        val pneEdit = dlg.findViewById<LinearLayout>(R.id.pneEditarMotivo)
        val txtInfo = dlg.findViewById<TextView>(R.id.txt_info_borrar)

        try {
            val art = JSONObject(obj.toString())
            val canArt = art.getInt("Can")
            cantidad = minOf(cantidad, canArt)
            art.put("Can", cantidad)
            txtInfo.text = "Borrar $cantidad " + art.getString("descripcion_t")
            resetCantidad()

            pneEdit.visibility = View.GONE

            exit.setOnClickListener { dlg.cancel() }
            edit.setOnClickListener {
                pneEdit.visibility = if (pneEdit.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
            ok.setOnClickListener {
                if (motivo.text.isNotEmpty()) {
                    borrarLinea(art, motivo.text.toString())
                    dlg.cancel()
                }
            }

            error.setOnClickListener {
                borrarLinea(art, error.text.toString())
                dlg.cancel()
            }

            simpa.setOnClickListener {
                borrarLinea(art, simpa.text.toString())
                dlg.cancel()
            }

            inv.setOnClickListener {
                borrarLinea(art, inv.text.toString())
                dlg.cancel()
            }
        } catch (e: JSONException) {
            Log.e("ERROR_CUENTA", e.toString())
        }

        dlg.show()
    }

    override fun borrarArticulo(art: JSONObject) {
        try {
            reset = true
            art.put("Can", 1)
            dbCuenta!!.eliminar(mesa!!.getString("ID"), JSONArray().put(art))
            rellenarTicket()
        } catch (e: JSONException) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }



    override fun pedirAutorizacion(params: ContentValues) {
        myServicio?.pedirAutorizacion(params)
    }

    override fun pedirAutorizacion(id: String) {
        // Implementación vacía en este caso
    }

    private fun cargarPreferencias() {
        val json = JSON()
        try {
            val pref = json.deserializar("preferencias.dat", this)
            if (!pref.isNull("sec")) {
                sec = pref.getString("sec")
            }
        } catch (e: Exception) {
            Log.e("ERROR_CUENTA", "Error al cargar las preferencias")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun resetCantidad() {
        cantidad = 1
        val lbl = findViewById<TextView>(R.id.lblCantida)
        lbl.text = "Cantidad $cantidad"
    }

    private fun asociarBotonera(view: View) {
        reset = true
        val json = JSON()
        try {
            val pref = json.deserializar("preferencias.dat", this)
            pref.put("sec", view.id.toString())
            json.serializar("preferencias.dat", pref, cx)
            val toast = Toast.makeText(applicationContext, "Asociación realizada", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 200)
            toast.show()
        } catch (e: JSONException) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }

    private fun borrarLinea(art: JSONObject, motivo: String) {
        try {
            reset = true
            val dbCamareros = myServicio?.getDb("camareros") as DBCamareros?
            if (!dbCamareros?.getConPermiso("borrar_linea").isNullOrEmpty()) {
                val p = JSONObject()
                p.put("idm", mesa!!.getString("ID"))
                p.put("Precio", art.getString("Precio"))
                p.put("idArt", art.getString("IDArt"))
                p.put("can", art.getString("Can"))
                p.put("idc", cam!!.getString("ID"))
                p.put("motivo", motivo)
                p.put("Estado", art.getString("Estado"))
                p.put("Descripcion", art.getString("Descripcion"))
                val dlg = DlgPedirAutorizacion(cx, this, dbCamareros!!, this, p, "borrar_linea")
                dlg.show()
            } else {
                val p = ContentValues()
                p.put("idm", mesa!!.getString("ID"))
                p.put("Precio", art.getString("Precio"))
                p.put("idArt", art.getString("IDArt"))
                p.put("can", art.getString("Can"))
                p.put("idc", cam!!.getString("ID"))
                p.put("motivo", motivo)
                p.put("Estado", art.getString("Estado"))
                p.put("Descripcion", art.getString("Descripcion"))
                dbCuenta!!.eliminar(mesa!!.getString("ID"), JSONArray().put(art))
                myServicio?.rmLinea(p)
                rellenarTicket()
            }
        } catch (e: JSONException) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }


    private fun sendMessageMesaCobrada(entrega: Double, cambio: Double) {
        val handlerMesas = myServicio?.getExHandler("camareros")
        val msg = handlerMesas?.obtainMessage()
        val bundle = msg?.data ?: Bundle()
        bundle.putString("op", "show_info_cobro")
        bundle.putDouble("entrega", entrega)
        bundle.putDouble("cambio", cambio)
        msg?.data = bundle
        if (msg != null) {
            handlerMesas.sendMessage(msg)
        }
    }
}

