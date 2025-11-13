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
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.tu.paquete.CustomToast
import com.valleapp.valletpv.adaptadoresDatos.AdaptadorTicket
import com.valleapp.valletpv.cashlogyActivitis.CobroCashlogyActivity
import com.valleapp.valletpv.dlg.DlgCobrar
import com.valleapp.valletpv.dlg.DlgEditExistencias
import com.valleapp.valletpv.dlg.DlgPedirAutorizacion
import com.valleapp.valletpv.dlg.DlgSepararTicket
import com.valleapp.valletpv.dlg.DlgVarios
import com.valleapp.valletpv.interfaces.IAutoFinish
import com.valleapp.valletpv.interfaces.IControladorAutorizaciones
import com.valleapp.valletpv.interfaces.IControladorCuenta
import com.valleapp.valletpv.tools.ServiceTPV
import com.valleapp.valletpv.tpvcremoto.CobroTarjetaActivity
import com.valleapp.valletpvlib.db.DBCamareros
import com.valleapp.valletpvlib.db.DBCuenta
import com.valleapp.valletpvlib.db.DBMesas
import com.valleapp.valletpvlib.db.DBSecciones
import com.valleapp.valletpvlib.db.DBTeclas
import com.valleapp.valletpvlib.tools.JSON
import com.valleapp.valletpvlib.R as LibR
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

    private var myServicio: ServiceTPV? = null

    private val cx: Context = this

    private var dlgCobrar: DlgCobrar? = null
    private val customToast = CustomToast(this)


    private val mConexion = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            try {
                myServicio = (iBinder as ServiceTPV.MyBinder).service
                myServicio?.setExHandler("lineaspedido", handlerHttp)
                myServicio?.setExHandler("teclas", handlerSeccionesTeclas)
                myServicio?.setExHandler("secciones", handlerSeccionesTeclas)
                dbCuenta = myServicio?.getDb("lineaspedido") as DBCuenta
                dbMesas = myServicio?.getDb("mesas") as DBMesas
                dbSecciones = myServicio?.getDb("seccionescom") as DBSecciones
                dbTeclas = myServicio?.getDb("teclas") as DBTeclas

                rellenarSecciones()
                rellenarTicket()
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

            // Ejecutar en el hilo principal
            runOnUiThread {
                try {

                    findViewById<View>(R.id.loading).visibility = View.GONE

                    if (tipo == "c") {
                        try {
                            setEstadoAutoFinish(reset = true, stop = true)
                            val idMesa = mesa?.getString("ID")
                            mostrarCobrar(dbCuenta!!.filterGroup("IDMesa=$idMesa"), totalMesa, false)
                            tipo = "m"
                        } catch (e: Exception) {
                            Log.e("ERROR_CUENTA", e.toString())
                        }
                    }else{
                        setEstadoAutoFinish(reset = true, stop = stop)
                    }

                    val res = msg.data.getString("RESPONSE")

                    if (res != null) {
                        val datos = JSONObject(res)
                        if (datos.getBoolean("soniguales")) return@runOnUiThread
                        val reg = datos.getJSONArray("reg")
                        dbCuenta!!.replaceMesa(reg, mesa!!.getString("ID"))
                        rellenarTicket()
                    } else {
                        rellenarTicket()
                    }
                } catch (e: JSONException) {
                    Log.e("ERROR_CUENTA", e.toString())
                }
            }
        }
    }
    private fun rellenarSecciones() {
        try {
            val lssec = dbSecciones?.getAll() ?: return
            if (lssec.length() > 0) {
                val ll = findViewById<LinearLayout>(R.id.pneSecciones)
                ll.removeAllViews()

                // Asegurar que el LinearLayout tenga orientación vertical
                ll.orientation = LinearLayout.VERTICAL

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                )
                params.setMargins(5, 5, 5, 5)

                for (i in 0 until lssec.length()) {
                    val z = lssec.getJSONObject(i)

                    if (sec.isEmpty() && i == 0) sec = z.getString("ID")

                    val imgBtn = ImageButton(cx)
                    imgBtn.id = z.getInt("ID")

                    // Obtener el icono con validación
                    val nombreIcono = z.optString("icono", "")

                    imgBtn.setImageDrawable(
                        com.valleapp.valletpvlib.tools.getDrawable(cx, nombreIcono))
                    imgBtn.tag = z.getString("ID")
                    imgBtn.scaleType = ImageView.ScaleType.FIT_CENTER
                    imgBtn.setBackgroundResource(LibR.drawable.fondo_btn_xml)

                    // Configurar el peso para que los botones se expandan
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                    )
                    layoutParams.setMargins(5, 5, 5, 5)

                    imgBtn.setOnClickListener {
                        setEstadoAutoFinish(reset = true, stop = false)
                        sec = it.tag.toString()
                        try {
                            val lsart = dbTeclas!!.getAll(sec, mesa!!.getInt("Tarifa"))
                            rellenarArticulos(lsart)
                            lsartresul = lsart
                        } catch (e: JSONException) {
                            Log.e("ERROR_CUENTA", e.toString())
                        }
                    }

                    imgBtn.setOnLongClickListener {
                        asociarBotonera(it)
                        false
                    }
                    ll.addView(imgBtn, layoutParams)
                }

                val lsart = dbTeclas!!.getAll(sec, mesa!!.getInt("Tarifa"))
                rellenarArticulos(lsart)
                lsartresul = lsart
            }
        } catch (e: Exception) {
            Log.e("ERROR_CUENTA", "Error en rellenarSecciones: ${e.message}")
        }
    }


    private fun getCuenta() {
        if (myServicio != null && mesa != null) {
            try {
                val p = ContentValues()
                p.put("reg", dbCuenta!!.filter("IDMesa=" + mesa!!.getString("ID")).toString())
                p.put("idm", mesa!!.getString("ID"))
                myServicio!!.getCuenta(handlerHttp, p)
                setEstadoAutoFinish(reset = true, stop = false)
                findViewById<View>(R.id.loading).visibility = View.VISIBLE

                // Llama a la función para ocultar el loading después de 2 segundos
                hideLoadingAfterDelay()

            } catch (e: JSONException) {
                Log.e("ERROR_CUENTA", "Error al cargar la cuenta $e")
            }
        }
    }

    private fun hideLoadingAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            findViewById<View>(R.id.loading).visibility = View.GONE
            if (tipo == "c") {
                try {
                    setEstadoAutoFinish(reset = true, stop = true)
                    val idMesa = mesa?.getString("ID")
                    mostrarCobrar(dbCuenta!!.filterGroup("IDMesa=$idMesa"), totalMesa, false)
                    tipo = "m"
                } catch (e: Exception) {
                    Log.e("ERROR_CUENTA", e.toString())
                }
            }else{
                setEstadoAutoFinish(reset = true, stop = stop)
            }

        }, 2000) // 2000 milisegundos = 2 segundos
    }

    private fun rellenarArticulos(lsart: JSONArray?) {
        try {
            if (lsart != null && lsart.length() > 0) {
                val tableLayout = findViewById<TableLayout>(R.id.pneArt)
                tableLayout.removeAllViews()

                val buttonHeightDp = calculateButtonHeight(this)
                val buttonHeightPx = dpToPx(buttonHeightDp, this)

                // Estos rowParams están bien para las filas de LinearLayout
                val rowParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    buttonHeightPx,
                    if (lsart.length() < 13) 0f else 1f // Peso dinámico
                )

                // !!! Aquí es donde cambiamos: Crear LinearLayout.LayoutParams para los botones
                // Esto asegura que cada botón ocupe un tercio de la fila.
                val buttonInRowParams = LinearLayout.LayoutParams(
                    0, // Ancho 0 para que el peso funcione
                    LinearLayout.LayoutParams.MATCH_PARENT, // Alto que ocupe toda la altura de la fila
                    1f // Peso de 1 para que cada botón ocupe 1/3 de la fila
                ).apply {
                    // Márgenes para separar los botones
                    setMargins(dpToPx(5, applicationContext), dpToPx(5, applicationContext), dpToPx(5, applicationContext), dpToPx(5, applicationContext))
                }


                var currentRow: LinearLayout? = null

                for (i in 0 until lsart.length()) {
                    if (i % 3 == 0) { // Crear una nueva fila cada 3 botones
                        currentRow = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = rowParams // Usamos los rowParams aquí
                        }
                        tableLayout.addView(currentRow)
                    }

                    val article = lsart.getJSONObject(i)
                    // !!! Pasamos los nuevos buttonInRowParams al createButton
                    val buttonView = createButton(article, buttonInRowParams)
                    currentRow?.addView(buttonView)
                }
            }
        } catch (e: Exception) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }

    @SuppressLint("InflateParams")
    private fun createButton(article: JSONObject, layoutParams: LinearLayout.LayoutParams): View {
        val inflate = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflate.inflate(LibR.layout.btn_art, null)
        val button = view.findViewById<Button>(LibR.id.boton_art).apply {
            textSize = 20f
            id = article.getInt("ID")
            tag = article
        }

        article.getInt("hay_existencias").let { hayExistencias ->
            if (hayExistencias == 1) {
                view.findViewById<ImageView>(LibR.id.ic_no_hay_existencias).visibility = View.GONE
                button.setOnClickListener { view ->
                    try {
                        artSel = (view.tag as JSONObject).apply {
                            put("Can", cantidad)
                            val descripcionOriginal = getString("descripcion_r")
                            put("Descripcion", descripcionOriginal)
                        }

                        if (artSel!!.getString("tipo") == "SP") {
                            pedirArt(artSel!!)
                            rellenarArticulos(lsartresul)
                        } else {
                            rellenarArticulos(dbTeclas!!.getAllSub(artSel!!.getString("ID")))
                        }
                    } catch (e: Exception) {
                        Log.e("ERROR_CUENTA", e.toString())
                    }
                }
            } else {
                view.findViewById<ImageView>(LibR.id.ic_no_hay_existencias).visibility = View.VISIBLE
            }
        }

        configureRgbButton(button, article)

        // Agregar setOnLongClickListener para abrir el diálogo
        button.setOnLongClickListener {
            setEstadoAutoFinish(reset = true, stop = true)
            val dialog = DlgEditExistencias(this, server, article)
            dialog.setOnDismissListener {
                setEstadoAutoFinish(reset = true, stop = false)
                val lsart = dbTeclas!!.getAll(sec, mesa!!.getInt("Tarifa"))
                rellenarArticulos(lsart)
            }
            dialog.show()
            true // Indica que el evento fue manejado
        }

        return view.apply {
            this.layoutParams = layoutParams
        }
    }

    private fun configureRgbButton(button: Button, article: JSONObject) {
        // Configurar el texto del botón
        var textoDelBoton: String
        val precio = article.getDouble("Precio")
        val nombre = article.getString("Nombre")

        if (precio == 0.0) {
            // Si el precio es 0, solo muestra el nombre
            textoDelBoton = nombre
        } else {
            // Si el precio no es 0, muestra nombre y precio formateado
            textoDelBoton = String.format(Locale.getDefault(), "%s\n%.2f €", nombre, precio)
            // Nota: Cambié %01.2f a %.2f que es más común para solo 2 decimales
        }

        button.text = textoDelBoton 

        val rgb = article.getString("RGB").split(",").map { it.toInt() }.toIntArray()
        button.setBackgroundColor(Color.rgb(rgb[0], rgb[1], rgb[2]))


    }



    private fun calculateButtonHeight(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        val screenHeightDp = displayMetrics.heightPixels / displayMetrics.density
        return (screenHeightDp / 7).toInt() // Ajusta el factor según sea necesario
    }

    private fun dpToPx(dp: Int, context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * displayMetrics.density).toInt()
    }




    private fun rellenarTicket() {
        try {
            resetCantidad()
            lineas = dbCuenta?.getLineasTicket(mesa!!.getString("ID"))
            totalMesa = dbCuenta!!.getTotal(mesa!!.getString("ID"))

            val l = findViewById<TextView>(R.id.lblPrecio)
            val lst = findViewById<ListView>(R.id.lstCamareros)
            l.text = String.format(Locale.getDefault(),"%01.2f €", totalMesa)
            lst.adapter = AdaptadorTicket(cx, lineas as ArrayList<JSONObject>, this)
        } catch (e: JSONException) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }

    fun mostrarSeparados( v: View?) {
        if (totalMesa > 0) {
            try {
                setEstadoAutoFinish(reset = true, stop = true)
                val mesaId = mesa!!.getString("ID")
                val nuevos = dbCuenta!!.getNuevos(mesaId)

                val ejecutarDlg = {
                    val lineas = dbCuenta!!.getAllByMesa(mesaId)

                    DlgSepararTicket(this, this).apply {
                        setLineasTicket(lineas)
                        show()
                    }

                    findViewById<View>(R.id.loading).visibility = View.GONE
                }

                if (nuevos.length() == 0) {
                    ejecutarDlg()
                } else {
                    aparcar(mesaId, nuevos)
                    findViewById<View>(R.id.loading).visibility = View.VISIBLE
                    Handler(Looper.getMainLooper()).postDelayed(ejecutarDlg, 1000)
                }
            } catch (e: JSONException) {
                Log.e("ERROR_CUENTA", e.toString())
            }
        }
    }

    fun mostrarVarios(v: View?) {
        setEstadoAutoFinish(reset = true, stop = true)
        val dlg = DlgVarios(this, this)
        dlg.show()
    }


    fun preImprimir( v: View?) {
        try {
            setEstadoAutoFinish(reset = true, stop = false)
            val mesaId = mesa!!.getString("ID")
            val nuevos = dbCuenta!!.getNuevos(mesaId)

            val ejecutarDlg = {

                if (totalMesa > 0) {
                    val p = ContentValues().apply {
                        put("idm", mesaId)
                    }
                    myServicio?.preImprimir(p)
                    dbMesas?.marcarRojo(mesaId)
                }

                findViewById<View>(R.id.loading).visibility = View.GONE
            }

            if (nuevos.length() > 0) {
                aparcar(mesaId, nuevos)
                findViewById<View>(R.id.loading).visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed(ejecutarDlg, 1500)
            } else {
                ejecutarDlg()
            }
        } catch (e: JSONException) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }

    fun abrirCajon(v: View?) {
        setEstadoAutoFinish(reset = true, stop = false)
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
    fun cobrarMesa( v: View?) {
        try {
            val mesaId = mesa!!.getString("ID")
            val nuevosRegistros = dbCuenta!!.getNuevos(mesaId)

            val ejecutarDlg = {
                val lineas = dbCuenta!!.filterGroup("IDMesa=$mesaId")
                mostrarCobrar(lineas, totalMesa, false)
                findViewById<View>(R.id.loading).visibility = View.GONE
            }

            if (nuevosRegistros.length() > 0) {
                aparcar(mesaId, nuevosRegistros)
                findViewById<View>(R.id.loading).visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed(ejecutarDlg, 1000)
            } else {
                ejecutarDlg()
            }

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
                customToast.showBottom( "Cobro cancelado", Toast.LENGTH_LONG)
            }
        }
    }


    private fun aparcar(idm: String, nuevos: JSONArray) {
        if (nuevos.length() == 0) return
        val p = ContentValues()
        p.put("idm", idm)
        p.put("idc", cam!!.getString("ID"))
        p.put("pedido", nuevos.toString())
        myServicio?.nuevoPedido(p)
        dbCuenta?.enviarLineas(idm)
        dbMesas?.abrirMesa(idm)
    }

    @SuppressLint("SetTextI18n")
    fun clickCantidad(v: View) {
        setEstadoAutoFinish(reset = true, stop = false)
        cantidad = (v as Button).text.toString().toInt()
        val lbl = findViewById<TextView>(R.id.lblCantida)
        lbl.text = "Cantidad $cantidad"
    }


    override fun pedirArt(art: JSONObject) {
        try {
            setEstadoAutoFinish(reset = true, stop = false)
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
        setEstadoAutoFinish(reset=true, stop=false)
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
        super.onDestroy()
        timerAutoCancel?.cancel()
        myServicio?.mesaAbierta(null)
        unbindService(mConexion)

        if (dlgCobrar != null && dlgCobrar!!.isShowing) {
            dlgCobrar!!.dismiss()
        }
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
                            } else{
                                setEstadoAutoFinish(reset = false, stop = false)
                            }
                        }
                    }
                }, 5000, autoCancel)
            }

            val intent = Intent(applicationContext, ServiceTPV::class.java)
            intent.putExtra("url", server)
            bindService(intent, mConexion, BIND_AUTO_CREATE)
            findViewById<View>(R.id.loading).visibility = View.GONE
        } catch (e: Exception) {
            Log.e("ERROR_CUENTA", e.toString())
        }
        super.onResume()
    }

    override fun setEstadoAutoFinish(reset: Boolean, stop: Boolean) {
        this@Cuenta.reset = reset
        this@Cuenta.stop = stop
    }

    override fun mostrarCobrar(lsart: JSONArray, totalCobro: Double, separetar: Boolean) {
        if (totalCobro > 0) {
            try {
                setEstadoAutoFinish(reset = true, stop = true)
                // Comprobamos si la actividad está finalizando antes de mostrar un diálogo
                if (isFinishing || isDestroyed) {
                    return
                }


                dlgCobrar?.dismiss()
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
                    dlgCobrar?.show()
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
            dlgCobrar?.dismiss()
            dlgCobrar = null

            val p = ContentValues()
            p.put("idm", mesa!!.getString("ID"))
            p.put("idc", cam!!.getString("ID"))
            p.put("entrega", entrega.toString())

            p.put("recibo", recibo)

            val idsBorrados = dbCuenta!!.eliminar(mesa!!.getString("ID"), lsart)

            // --- SOLUCIÓN AQUÍ ---
            // 1. Crear un JSONArray a partir de tu lista de Longs.
            val idsJsonArray = JSONArray(idsBorrados)

            // 2. Convertir el JSONArray a su representación de texto.
            // Esto crea un string con el formato JSON correcto: "[101,102,105]"
            p.put("idsCobrados", idsJsonArray.toString())
            // --- FIN DE LA SOLUCIÓN ---

            myServicio?.cobrarCuenta(p)

            if (totalCobro - totalMesa == 0.0) {
                dbMesas!!.cerrarMesa(mesa!!.getString("ID"))
                finish()
            } else {
                rellenarTicket()
                setEstadoAutoFinish(reset = true, stop = false)
            }
            sendMessageMesaCobrada(entrega, entrega - totalCobro)

        } catch (e: JSONException) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }

    @SuppressLint("SetTextI18n")
    override fun clickMostrarBorrar(art: JSONObject) {
        setEstadoAutoFinish(reset = true, stop = true)

        val dlg = Dialog(cx)
        dlg.setContentView(R.layout.borrar_art)
        dlg.setOnCancelListener { setEstadoAutoFinish(reset = true, stop = false) }


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
            val art = JSONObject(art.toString())
            val canArt = art.getInt("Can")
            cantidad = minOf(cantidad, canArt)
            art.put("Can", cantidad)
            txtInfo.text = "Borrar $cantidad " + art.getString("descripcion_t")
            resetCantidad()

            pneEdit.visibility = View.GONE

            exit.setOnClickListener { dlg.cancel() }
            edit.setOnClickListener {
                pneEdit.visibility = if (pneEdit.isVisible) View.GONE else View.VISIBLE
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
            setEstadoAutoFinish(reset=true, stop=false)
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
        Log.d("PEDIR_AUTORIZACION", "Has sido llamada con id: $id")
    }

    private fun cargarPreferencias() {
        val json = JSON()
        try {
            val pref = json.deserializar("preferencias.dat", this)
            pref?.isNull("sec")?.let {
                if (!it) {
                    sec = pref.getString("sec")
                }
            }
        } catch (e: Exception) {
            Log.e("ERROR_CUENTA", "Error al cargar las preferencias del archivo JSON: $e")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun resetCantidad() {
        cantidad = 1
        val lbl = findViewById<TextView>(R.id.lblCantida)
        lbl.text = "Cantidad $cantidad"
    }

    private fun asociarBotonera(view: View) {
       setEstadoAutoFinish(reset=true, stop=false)
        val json = JSON()
        try {
            val pref = json.deserializar("preferencias.dat", this)
            pref?.put("sec", view.id.toString())
            json.serializar("preferencias.dat", pref, cx)
            customToast.showCenter( "Asociación realizada", Toast.LENGTH_SHORT)

        } catch (e: JSONException) {
            Log.e("ERROR_CUENTA", e.toString())
        }
    }

    private fun borrarLinea(art: JSONObject, motivo: String) {
        try {
            setEstadoAutoFinish(reset=true, stop=false)
            val dbCamareros = myServicio?.getDb("camareros") as DBCamareros
            if (dbCamareros.getConPermiso("borrar_linea").isNotEmpty()) {
                val p = JSONObject()
                p.put("idm", mesa!!.getString("ID"))
                p.put("Precio", art.getString("Precio"))
                p.put("idArt", art.getString("IDArt"))
                p.put("can", art.getString("Can"))
                p.put("idc", cam!!.getString("ID"))
                p.put("motivo", motivo)
                p.put("Estado", art.getString("Estado"))
                p.put("Descripcion", art.getString("Descripcion"))
                val dlg = DlgPedirAutorizacion(cx, this, dbCamareros, this, p, "borrar_linea")
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
