package com.valleapp.valletpv

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import com.valleapp.valletpv.adaptadoresDatos.AdaptadorSettings
import com.valleapp.valletpv.cashlogyActivitis.CambioCashlogyActivity
import com.valleapp.valletpv.dlg.DlgMensajes
import com.valleapp.valletpv.dlg.DlgPedirAutorizacion
import com.valleapp.valletpv.dlg.DlgSelCamareros
import com.valleapp.valletpv.interfaces.IAutoFinish
import com.valleapp.valletpv.interfaces.IControlMensajes
import com.valleapp.valletpv.interfaces.IControladorAutorizaciones
import com.valleapp.valletpv.tools.ServiceTPV
import com.valleapp.valletpvlib.db.DBCamareros
import com.valleapp.valletpvlib.db.DBCuenta
import com.valleapp.valletpvlib.db.DBMesas
import com.valleapp.valletpvlib.db.DBZonas
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
import java.util.Objects
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import androidx.core.view.isVisible
import kotlin.math.roundToInt

class Mesas : AppCompatActivity(), IAutoFinish, IControladorAutorizaciones, IControlMensajes {

    val cx: Context = this
    private val periodFinish: Long = 5000

    // Variables para salir con temporizador
    private var autoSalir = Timer()
    var stop = false
    var reset = false

    var dbZonas: DBZonas? = null
    var dbMesas: DBMesas? = null
    var dbCuenta: DBCuenta? = null

    private var cam: JSONObject? = null
    var zn: JSONObject? = null
    var lsTicket: JSONArray? = null


    private var dlgListadoTicket: Dialog? = null
    private var idTicket = ""

    var adaptadorSettings: AdaptadorSettings? = null
    var listaSetting: ListView? = null
    var myServicio: ServiceTPV? = null
    var server = ""

    private val mConexion = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            myServicio = (iBinder as ServiceTPV.MyBinder).service
            myServicio!!.setExHandler("mesas", handleHttp)
            myServicio!!.setExHandler("mesasabiertas", handleHttp)
            dbMesas = myServicio!!.getDb("mesas") as DBMesas
            dbCuenta = myServicio!!.getDb("lineaspedido") as DBCuenta
            dbZonas = myServicio!!.getDb("zonas") as DBZonas
            zn = myServicio!!.zona
            rellenarZonas()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            myServicio = null
        }
    }

    @SuppressLint("HandlerLeak")
    private val handleHttp = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            try {
                val op = msg.data.getString("op")
                if (op == null) {
                    rellenarZonas()
                } else {
                    val res = msg.data.getString("RESPONSE")
                    when (op) {
                        "get_lista_ticket" -> {
                            lsTicket = JSONArray(res)
                            mostrarListadoTicket(lsTicket!!)
                        }
                        "get_lineas_ticket" -> {
                            if (res != null) {
                                mostrarlineasTicket(res)
                            }
                        }
                        "get_lista_receptores" -> {
                            if (listaSetting != null) {
                                try {
                                    val lista = JSONArray(res)
                                    Log.d("MESAS_ERR", lista.toString())
                                    val alista = ArrayList<JSONObject>()

                                    for (i in 0 until lista.length()) {
                                        val obj = lista.getJSONObject(i)
                                        val nombre = obj.optString("Nombre", "").trim().lowercase(
                                            Locale.ROOT
                                        )

                                        // Filtrar nombres "nulo" o "null" antes de añadirlos
                                        if (nombre != "null" && nombre != "nulo") {
                                            alista.add(obj)
                                        }
                                    }

                                    adaptadorSettings = AdaptadorSettings(cx, alista)
                                    listaSetting!!.adapter = adaptadorSettings
                                } catch (e: Exception) {
                                    Log.e("MESAS_ERR", "Error al procesar la lista", e)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MESAS_ERR", e.toString())
            }
        }
    }

    private fun rellenarZonas() {
        try {
            val lszonas = dbZonas!!.getAll()
            if (lszonas.length() > 0) {
                val ll = findViewById<LinearLayout>(R.id.pneZonas)
                ll.removeAllViews()

                val metrics = resources.displayMetrics

                val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (metrics.density * 100).roundToInt()
                )

                params.setMargins(5, 5, 5, 5)

                for (i in 0 until lszonas.length()) {
                    val z = lszonas.getJSONObject(i)

                    if (zn == null) zn = z

                    val btn = Button(cx)
                    btn.id = i
                    btn.textSize = 20f
                    btn.isSingleLine = false
                    btn.text = z.getString("Nombre")
                    btn.tag = z
                    val rgb = z.getString("RGB").trim().split(",")
                    btn.setBackgroundColor(
                            Color.rgb(
                                    Integer.parseInt(rgb[0]),
                                    Integer.parseInt(rgb[1]),
                                    Integer.parseInt(rgb[2])
                            )
                    )

                    btn.setOnClickListener { view ->
                        zn = view.tag as JSONObject
                        myServicio!!.zona = zn!!
                        rellenarMesas()
                    }
                    ll.addView(btn, params)
                }

                rellenarMesas()
            }
        } catch (e: Exception) {
            Log.e("MESAS_ERR", e.toString())
        }
    }

    private fun rellenarMesas() {
        try {
            reset = true //reseteamos contador auto salida
            val znID = zn!!.getString("ID")
            val lsmesas = dbMesas!!.getAll(znID)
            if (lsmesas.length() > 0) {
                val ll = findViewById<TableLayout>(R.id.pneMesas)
                        ll.removeAllViews()

                val params = TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.MATCH_PARENT
                )

                val metrics = resources.displayMetrics

                val rowparams = TableRow.LayoutParams(
                    (metrics.density * 160).roundToInt(),
                    (metrics.density * 160).roundToInt()
                )

                rowparams.setMargins(5, 5, 5, 5)

                var row = TableRow(cx)
                ll.addView(row, params)

                for (i in 0 until lsmesas.length()) {
                    val m = lsmesas.getJSONObject(i)
                    val inflater = cx.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

                    @SuppressLint("InflateParams")
                    val v = inflater.inflate(R.layout.boton_mesa, null)

                    val btnCm = v.findViewById<ImageButton>(R.id.btnCambiarMesa)
                    val btnC = v.findViewById<ImageButton>(R.id.btnCobrarMesa)
                    val btnRm = v.findViewById<ImageButton>(R.id.btnBorrarMesa)
                    val panel = v.findViewById<LinearLayout>(R.id.pneBtnMesa)
                    val abierta = m.getString("abierta")

                    if (abierta == "0") {
                        panel.visibility = View.GONE
                    } else {
                        panel.visibility = View.VISIBLE
                        inicializarBtnAux(btnC, btnCm, btnRm, m)
                    }

                    m.put("Tarifa", zn!!.getString("Tarifa"))

                    val btn = v.findViewById<Button>(R.id.btnMesa)
                            btn.textSize = 20f
                    btn.id = i
                    btn.setPadding(10, 10, 10, 10)
                    btn.text = m.getString("Nombre")

                    btn.tag = m

                    val rgb = m.getString("RGB").trim().split(",")
                    btn.setBackgroundColor(
                            Color.rgb(
                                    Integer.parseInt(rgb[0]),
                                    Integer.parseInt(rgb[1]),
                                    Integer.parseInt(rgb[2])
                            )
                    )

                    btn.setOnClickListener { view: View ->
                        val intent = Intent(cx, Cuenta::class.java)
                        val obj = view.tag as JSONObject
                        intent.putExtra("url", server)
                        intent.putExtra("op", "m")
                        intent.putExtra("cam", cam.toString())
                        intent.putExtra("mesa", obj.toString())
                        startActivity(intent)
                    }
                    row.addView(v, rowparams)

                    if (((i + 1) % 5) == 0) {
                        row = TableRow(cx)
                        ll.addView(row, params)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MESAS_ERR", e.toString())
        }
    }

    private fun inicializarBtnAux(btnC: ImageButton, btnCm: ImageButton, btnRm: ImageButton, m: JSONObject) {
        btnCm.tag = m
        btnC.tag = m
        btnRm.tag = m
        btnRm.setOnClickListener { view ->
                clickBorrarMesa(view)
            false
        }
        btnC.setOnClickListener { view ->
                clickCobrarMesa(view)
            false
        }
        btnCm.setOnClickListener { view ->
                clickCambiarMesa(view)
            false
        }
        btnCm.setOnLongClickListener { view ->
                clickJuntarMesa(view)
            false
        }
    }

    fun clickAddCamareros(v: View) {
        try {
            stop = true //paramos contador
            val selCam = DlgSelCamareros(cx, myServicio, false, this)
            val dbCamareros = myServicio!!.getDb("camareros") as DBCamareros
            selCam.setNoautorizados(dbCamareros.getAutorizados(false))
            selCam.setAutorizados(dbCamareros.getAutorizados(true))
            selCam.show()
            selCam.getBtnOk().setOnClickListener {
                selCam.cancel()
                setEstadoAutoFinish(reset = true, stop = false)
            }
        } catch (e: Exception) {
            Log.e("MESAS_ERR", e.toString())
        }
    }

    fun clickSettings(v: View) {
        setEstadoAutoFinish(reset = true, stop = true)
        val settings = Dialog(cx)

        settings.setOnCancelListener { setEstadoAutoFinish(reset = true, stop = false) }
        settings.setTitle("Opciones impresión...")
        settings.setContentView(R.layout.dialog_settings)
        listaSetting = settings.findViewById(R.id.lista_settings)
        val salir = settings.findViewById<ImageButton>(R.id.btn_guardar)
                salir.setOnClickListener {
                    if (adaptadorSettings != null) {
                val lista = adaptadorSettings!!.getLista()
                if (myServicio != null) myServicio!!.setSettings(lista.toString())
            }
            settings.cancel()
        }
        if (myServicio != null) myServicio!!.getSettings(handleHttp)
        settings.show()
    }

    fun clickListaTicket(v: View) {
        stop = true
        dlgListadoTicket = Dialog(this)
        dlgListadoTicket!!.setOnCancelListener {
            stop = false
            reset = true
        }
        dlgListadoTicket!!.setContentView(R.layout.listado_ticket)
        dlgListadoTicket!!.setTitle("Lista de ticket")
        val window = dlgListadoTicket!!.window
        assert(window != null)
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        val imp = dlgListadoTicket!!.findViewById<ImageButton>(R.id.btnImprimir)
        val impFactura = dlgListadoTicket!!.findViewById<ImageButton>(R.id.btnImprimirFactura)
        val salir = dlgListadoTicket!!.findViewById<ImageButton>(R.id.btn_salir_monedas)
        val ls = dlgListadoTicket!!.findViewById<ImageButton>(R.id.btnListado)
        imp.visibility = View.GONE
        ls.visibility = View.GONE
        impFactura.visibility = View.GONE

        salir.setOnClickListener {
            if(ls.isVisible) {
                imp.visibility = View.GONE
                ls.visibility = View.GONE
                impFactura.visibility = View.GONE
                mostrarListadoTicket(lsTicket!!)
            }else {
                dlgListadoTicket!!.cancel()
            }
        }

        ls.setOnClickListener {
            imp.visibility = View.GONE
            ls.visibility = View.GONE
            impFactura.visibility = View.GONE
            mostrarListadoTicket(lsTicket!!)
        }
        imp.setOnClickListener {
            if (myServicio != null) myServicio!!.imprimirTicket(idTicket)
            dlgListadoTicket!!.cancel()
        }
        impFactura.setOnClickListener {
            if (myServicio != null) myServicio!!.imprimirFactura(idTicket)
            dlgListadoTicket!!.cancel()
        }
        if (myServicio != null) myServicio!!.getListaTickets(handleHttp)
        dlgListadoTicket!!.show()
    }

    fun clickVerTicket(v: View) {
        idTicket = v.tag.toString()
        dlgListadoTicket!!.findViewById<ImageButton>(R.id.btnImprimir).visibility = View.VISIBLE
        dlgListadoTicket!!.findViewById<ImageButton>(R.id.btnListado).visibility = View.VISIBLE
        dlgListadoTicket!!.findViewById<ImageButton>(R.id.btnImprimirFactura).visibility = View.VISIBLE
        if (myServicio != null) {
            myServicio!!.getLineasTicket(handleHttp, idTicket)
        }
    }

    fun clickSendMensajes(v: View) {
        try {
            setEstadoAutoFinish(reset = true, stop = true)
            val dlg = DlgMensajes(cx, this)
            dlg.setOnCancelListener { setEstadoAutoFinish(reset = true, stop = false) }
            dlg.show()
            DBCamareros(cx).use { db ->
                    val lista = db.getAutorizados(true)
                val o = JSONObject()
                o.put("ID", UUID.randomUUID().toString())
                o.put("nombre", "PARA TODOS")
                o.put("apellidos", "")
                lista.add(o)
                dlg.mostrarReceptores(lista)
            }
        } catch (e: Exception) {
            Log.e("MESAS_ERR", e.toString())
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun mostrarListadoTicket(ls: JSONArray) {
        try {
            if (ls.length() > 0) {
                val ll = dlgListadoTicket!!.findViewById<LinearLayout>(R.id.pneListados)
                ll.removeAllViews()
                val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )

                params.setMargins(5, 5, 5, 5)

                for (i in 0 until ls.length()) {
                    val z = ls.getJSONObject(i)

                    val inflater = cx.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    @SuppressLint("InflateParams")
                    val v = inflater.inflate(R.layout.item_cabecera_ticket, null)

                    val n = v.findViewById<TextView>(R.id.lblNumTicket)
                    val f = v.findViewById<TextView>(R.id.lblHoraFecha)
                    val m = v.findViewById<TextView>(R.id.lblNombre)
                    val e = v.findViewById<TextView>(R.id.lblEntrega)
                    val t = v.findViewById<TextView>(R.id.lblTotal)

                    m.text = z.getString("Mesa")
                    f.text = z.getString("Fecha") + " - " + z.getString("Hora")
                    e.text = String.format("%01.2f €", z.getDouble("Entrega"))
                    val total = if (z.isNull("Total")) 0.0 else z.getDouble("Total")
                    t.text = String.format("%01.2f €", total)
                    n.text = z.getString("ID")

                    v.findViewById<View>(R.id.btnVerTicket).tag = z.getString("ID")
                    ll.addView(v, params)
                }
            }
        } catch (e: Exception) {
            Log.e("MESAS_ERR_EN_MOSTRARLISTADO", e.toString())
        }
    }

    @SuppressLint("DefaultLocale")
    private fun mostrarlineasTicket(res: String) {
        try {
            val ticket = JSONObject(res)
            val ls = ticket.getJSONArray("lineas")
            val total = if (ticket.isNull("total")) 0.0 else ticket.getDouble("total")

            if (ls.length() > 0) {
                val ll = dlgListadoTicket!!.findViewById<LinearLayout>(R.id.pneListados)
                ll.removeAllViews()
                val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )

                params.setMargins(5, 5, 5, 5)
                for (i in 0 until ls.length()) {
                    val z = ls.getJSONObject(i)

                    val inflater = cx.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    @SuppressLint("InflateParams")
                    val v = inflater.inflate(R.layout.item_linea_ticket, null)

                    val c = v.findViewById<TextView>(R.id.lblCan)
                    val p = v.findViewById<TextView>(R.id.lblPrecio)
                    val n = v.findViewById<TextView>(R.id.lblNombre)
                    val t = v.findViewById<TextView>(R.id.lblTotal)

                    c.text = z.getString("Can")
                    p.text = String.format("%01.2f €", z.getDouble("Precio"))
                    n.text = z.getString("Nombre")
                    t.text = String.format("%01.2f €", z.getDouble("Total"))

                    ll.addView(v, params)
                }

                val inflater = cx.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                @SuppressLint("InflateParams")
                val v = inflater.inflate(R.layout.item_linea_total, null)
                val t = v.findViewById<TextView>(R.id.lblTotalTicket)
                        t.text = String.format("%01.2f €", total)

                ll.addView(v, params)
            }
        } catch (e: Exception) {
            Log.e("MESAS_ERR", e.toString())
        }
    }

    fun clickCobrarMesa(v: View) {
        val m = v.tag as JSONObject
        val intent = Intent(cx, Cuenta::class.java)
        intent.putExtra("op", "c")
        intent.putExtra("cam", cam.toString())
        intent.putExtra("mesa", m.toString())
        startActivity(intent)
    }

    fun clickAbrirCaja(v: View) {
        setEstadoAutoFinish(reset = true, stop = false)

        if (myServicio != null && myServicio!!.usaCashlogy()) {
            // Si usa Cashlogy, mostrar la actividad CambioCashlogyActivity
            val intent = Intent(this, CambioCashlogyActivity::class.java)
            startActivity(intent)
        } else {
            // Si no usa Cashlogy, proceder con la lógica actual
            assert(myServicio != null)
            val dbCamareros = myServicio!!.getDb("camareros") as DBCamareros
            if (dbCamareros.getConPermiso("abrir_cajon").isNotEmpty()) {
                try {
                    val p = JSONObject()
                    p.put("idc", cam!!.getString("ID"))
                    val dlg = DlgPedirAutorizacion(cx, this, dbCamareros, this, p, "abrir_cajon")
                    dlg.show()
                } catch (e: JSONException) {
                    Log.e("MESAS_ERR", e.toString())
                }
            } else {
                if (myServicio != null) {
                    myServicio!!.abrirCajon()
                }
            }
        }
    }

    fun clickCambiarMesa(v: View) {
        val intent = Intent(cx, OpMesas::class.java)
        intent.putExtra("mesa", v.tag.toString())
        intent.putExtra("op", "cambiar")
        startActivity(intent)
    }

    private fun clickJuntarMesa(v: View) {
        val intent = Intent(cx, OpMesas::class.java)
        intent.putExtra("mesa", v.tag.toString())
        intent.putExtra("op", "juntar")
        startActivity(intent)
    }

    @SuppressLint("SetTextI18n")
    fun clickBorrarMesa(v: View) {
        setEstadoAutoFinish(reset = true, stop = true)

        val m = v.tag as JSONObject
        val dlg = Dialog(cx)

        dlg.setOnCancelListener { setEstadoAutoFinish(reset = true, stop = false) }

        dlg.setContentView(R.layout.borrar_art)

        val motivo = dlg.findViewById<EditText>(R.id.txtMotivo)
                val error = dlg.findViewById<Button>(R.id.btnError)
                val simpa = dlg.findViewById<Button>(R.id.btnSimpa)
                val inv = dlg.findViewById<Button>(R.id.btnInv)
                val ok = dlg.findViewById<ImageButton>(R.id.btn_ok)
                val edit = dlg.findViewById<ImageButton>(R.id.btnEdit)
                val exit = dlg.findViewById<ImageButton>(R.id.btn_salir_monedas)
                val pneEdit = dlg.findViewById<LinearLayout>(R.id.pneEditarMotivo)
                val txtInfo = dlg.findViewById<TextView>(R.id.txt_info_borrar)
                txtInfo.text = "Borrado de mesa completa"
        pneEdit.visibility = View.GONE

        // Configurar los botones
        error.isSingleLine = false
        error.maxLines = 3
        error.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(dlg.context, R.color.pink))
        error.setTextColor(ContextCompat.getColor(dlg.context, android.R.color.black))
        error.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)

        simpa.isSingleLine = false
        simpa.maxLines = 3
        simpa.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(dlg.context, R.color.pink))
        simpa.setTextColor(ContextCompat.getColor(dlg.context, android.R.color.black))
        simpa.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)

        inv.isSingleLine = false
        inv.maxLines = 3
        inv.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(dlg.context, R.color.pink))
        inv.setTextColor(ContextCompat.getColor(dlg.context, android.R.color.black))
        inv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)

        exit.setOnClickListener { dlg.cancel() }

        edit.setOnClickListener {
            if (pneEdit.isVisible) pneEdit.visibility = View.GONE
            else pneEdit.visibility = View.VISIBLE
        }

        ok.setOnClickListener {
            if (motivo.text.isNotEmpty()) {
                try {
                    dlg.cancel()
                    val idm = m.getString("ID")
                    borrarMesa(idm, motivo.text.toString())
                } catch (e: JSONException) {
                    Log.e("MESAS_ERR", e.toString())
                }
            }
        }

        error.setOnClickListener {
            try {
                dlg.cancel()
                borrarMesa(m.getString("ID"), error.text.toString())
            } catch (e: JSONException) {
                Log.e("MESAS_ERR", e.toString())
            }
        }

        simpa.setOnClickListener {
            try {
                dlg.cancel()
                borrarMesa(m.getString("ID"), simpa.text.toString())
            } catch (e: JSONException) {
                Log.e("MESAS_ERR", e.toString())
            }
        }

        inv.setOnClickListener {
            try {
                dlg.cancel()
                borrarMesa(m.getString("ID"), inv.text.toString())
            } catch (e: JSONException) {
                Log.e("MESAS_ERR", e.toString())
            }
        }

        dlg.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mesas)
        server = intent.getStringExtra("server") ?: ""
        try {
            cam = JSONObject(Objects.requireNonNull(intent.getStringExtra("cam")))
            val title = findViewById<TextView>(R.id.lblTitulo)
                    val nombre = String.format(Locale.getDefault(), "%s %s",
                    cam!!.getString("nombre"), cam!!.getString("apellidos"))
            title.text = nombre
        } catch (e: JSONException) {
            Log.e("MESAS_ERR", e.toString())
        }

        autoSalir.schedule(object : TimerTask() {
            override fun run() {
                if (!stop) {
                    if (!reset) {
                        finish()
                    } else reset = false
                }
            }
        }, 5000, periodFinish)
    }

    override fun onDestroy() {
        unbindService(mConexion)
        super.onDestroy()
    }

    override fun onPause() {
        stop = true
        super.onPause()
    }

    override fun onResume() {
        stop = false
        val intent = Intent(applicationContext, ServiceTPV::class.java)
        bindService(intent, mConexion, BIND_AUTO_CREATE)
        if (dbZonas != null) {
            rellenarZonas()
        }

        super.onResume()
    }

    override fun setEstadoAutoFinish(reset: Boolean, stop: Boolean) {
        this.reset = reset
        this.stop = stop
    }

    override fun pedirAutorizacion(p: ContentValues) {
        myServicio!!.pedirAutorizacion(p)
    }

    override fun pedirAutorizacion(id: String) {}

    override fun sendMensaje(idReceptor: String, men: String) {
        setEstadoAutoFinish(reset = true, stop = false)
        try {
            val p = ContentValues()
            p.put("idreceptor", idReceptor)
            p.put("accion", "informacion")
            p.put("mensaje", men)
            p.put("autor", cam!!.getString("nombre"))
            myServicio!!.sendMensaje(p)
        } catch (e: Exception) {
            Log.e("MESAS_ERR", e.toString())
        }
    }

    //Utilidades
    private fun borrarMesa(idm: String, motivo: String) {
        try {
            if (myServicio != null) {
                val dbCamareros = myServicio!!.getDb("camareros") as DBCamareros
                if (dbCamareros.getConPermiso("borrar_mesa").isNotEmpty()) {
                    val p = JSONObject()
                    p.put("motivo", motivo)
                    p.put("idm", idm)
                    p.put("idc", cam!!.getString("ID"))
                    val dlg = DlgPedirAutorizacion(cx, this, dbCamareros, this, p, "borrar_mesa")
                    dlg.show()
                } else {
                    val p = ContentValues()
                    p.put("motivo", motivo)
                    p.put("idm", idm)
                    p.put("idc", cam!!.getString("ID"))
                    dbCuenta!!.eliminar(idm)
                    dbMesas!!.cerrarMesa(idm)
                    myServicio!!.rmMesa(p)
                    rellenarMesas()
                }
            }
        } catch (e: JSONException) {
            Log.e("MESAS_ERR", e.toString())
        }
    }
}