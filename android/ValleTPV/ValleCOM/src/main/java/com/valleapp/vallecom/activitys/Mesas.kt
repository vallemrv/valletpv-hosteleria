package com.valleapp.vallecom.activitys


import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.valleapp.vallecom.adaptadores.AdaptadorMesas
import com.valleapp.vallecom.interfaces.IPedidos
import com.valleapp.vallecom.tab.ListaMesas
import com.valleapp.vallecom.tab.Pedidos
import com.valleapp.vallecom.utilidades.ActivityBase
import com.valleapp.vallecom.utilidades.ServiceCOM
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.db.DBCuenta
import com.valleapp.valletpvlib.db.DBMesas
import com.valleapp.valletpvlib.db.DBReceptores
import com.valleapp.valletpvlib.db.DBZonas
import com.valleapp.valletpvlib.tools.Instruccion
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Mesas : ActivityBase(), View.OnLongClickListener, IPedidos {

    companion object {
        private const val TAG = "Mesas"
    }

    private var listaMesas: ListaMesas? = null
    private var pedidos: Pedidos? = null

    private var presBack = 0
    private var uid = ""

    private var dbMesas: DBMesas? = null
    private var dbZonas: DBZonas? = null
    private var dbCuenta: DBCuenta? = null
    private var dbReceptores: DBReceptores? = null

    private var zn: JSONObject? = null

    private var peticiones = JSONArray()
    private var receptores: ArrayList<JSONObject>? = null
    private var isViewingMessages = false
    private var botonesReceptores: LinearLayout? = null

    private var selectedReceptorId = 0
    private var showAllWaiters = false

    private val handlerMesas = Handler(Looper.getMainLooper()) {
        rellenarZonas()
        true
    }

    private val handlerPedidos = Handler(Looper.getMainLooper()) {
        rellenarPedido(selectedReceptorId, showAllWaiters)
        true
    }

    private val handlerMensajes = Handler(Looper.getMainLooper()) {
        val op = it.data.getString("op")
        val res: String = it.data.getString("RESPONSE").toString()
        try {
            if (op == "men_once") {
                val o = JSONObject(res)
                val idautorizado = o.getString("idautorizado")
                val idcam = cam?.getString("ID")
                if (idautorizado == idcam) {
                    peticiones.put(o)
                }
            } else {
                peticiones = JSONArray(res)
            }
            if (!isViewingMessages && peticiones.length() > 0) {
                val m = MediaPlayer.create(cx, R.raw.mail)
                m.start()
            } else {
                // This call will now use the Activity Result API
                mostrarAutorias(null)
            }
            manejarAurotias()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling messages", e)
        }
        true
    }

    private val handlerOperaciones = Handler(Looper.getMainLooper()) {
        val op = it.data.getString("op") ?: ""
        when (op) {
            "exit" -> finish()
            "servido" -> customToast.showBottom("Articulos servidos")
            "reenviar" -> customToast.showBottom("Peticion enviadaaa")
            else -> findViewById<View>(R.id.loading).visibility = View.GONE
        }
        true
    }



    private val handlerEstadoWS = Handler(Looper.getMainLooper()) { msg ->

        // Manejar mensajes de nuestro TimerTask (identificados por MSG_STATUS_UPDATE)
        val numPendientes = msg.data.getString("numPendientes")
        val estadoWebSocket = msg.data.getBoolean("status")

        // Encuentra y actualiza el TextView para las tareas pendientes
        val txtTareas = findViewById<TextView>(R.id.txtInf_tareas)
        txtTareas.text = buildString {
                append("Tareas: ")
                append(numPendientes)
            } // Asigna directamente el número como texto

        // Encuentra y actualiza el TextView para el estado del WebSocket
        val txtWs = findViewById<TextView>(R.id.txtInf_ws)
        txtWs.text = if (estadoWebSocket) "Conectado" else "Desconectado" // Muestra el estado basado en el booleano

        true // Indica que el mensaje ha sido manejado
    }


    private fun manejarAurotias() {
        val v = findViewById<View>(R.id.show_autorias)
        val numPeticiones = peticiones.length()

        if (numPeticiones > 0) {
            v.visibility = View.VISIBLE
            val t = findViewById<TextView>(R.id.txt_num_autorias)
            t.text = numPeticiones.toString()
        } else {
            v.visibility = View.GONE
        }
    }

    private val mConexion = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            myServicio = (iBinder as ServiceCOM.MyBinder).getService()
            myServicio?.let { servicio ->
                dbMesas = servicio.getDb("mesas") as DBMesas
                dbZonas = servicio.getDb("zonas") as DBZonas
                dbCuenta = servicio.getDb("lineaspedido") as DBCuenta
                dbReceptores = servicio.getDb("receptores") as DBReceptores
                servicio.setExHandler("mesasabiertas", handlerMesas)
                servicio.setExHandler("zonas", handlerMesas)
                servicio.setExHandler("mesas", handlerMesas)
                servicio.setExHandler("mensajes", handlerMensajes)
                servicio.setExHandler("lineaspedido", handlerPedidos)
                servicio.setExHandler("estadows", handlerEstadoWS)
                server = servicio.getServerUrl()
                servicio.setCam(cam)
                rellenarZonas()
                manejarAurotias()

            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            myServicio = null
        }
    }

    private fun rellenarPedido(idReceptor: Int, all: Boolean) {
        try {
            // Si idReceptor es -1 (Todos) o fuera de rango, receptorId será null
            val receptorId = if (idReceptor >= 0 && idReceptor < (receptores?.size ?: 0)) {
                receptores?.get(idReceptor)?.getInt("ID")
            } else {
                null
            }

            val sqlWhere = if (all) "" else "camarero=${myServicio?.getCam()?.getString("ID")} and "

            // Construir el filtro. Si no hay receptorId, no se filtra por receptor.
            val filter = if (receptorId != null) {
                "$sqlWhere receptor=$receptorId and servido=0"
            } else {
                "${sqlWhere}servido=0"
            }

            val lineas = dbCuenta?.getLineasByPedido(filter)
            val ll: LinearLayout? = pedidos?.contenedor
            ll?.removeAllViews()


            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(5, 5, 5, 5)
            }
            var idPedido = -1
            var grupo: View?
            var listaGr: LinearLayout? = null
            var paramsLinea: LinearLayout.LayoutParams? = null
            lineas?.let {
                for (i in 0 until it.length()) {
                    val art = it.getJSONObject(i)
                    if (idPedido == -1 || idPedido != art.getInt("IDPedido")) {
                        idPedido = art.getInt("IDPedido")
                        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        grupo = inflater.inflate(R.layout.grupos_pedidos, ll, false)
                        listaGr = grupo?.findViewById(R.id.lista_pedidos_grupo)
                        val m = grupo?.findViewById<TextView>(R.id.texto_mesa)
                        m?.text = buildString {
                            append("Mesa: ")
                            append(art.getString("nomMesa"))
                        }
                        ll?.addView(grupo, params)
                        val btn = grupo?.findViewById<ImageButton>(R.id.borrar_pedido)

                        // Crear un objeto tag con la información necesaria
                        val tagInfo = JSONObject().apply {
                            put("idPedido", idPedido)
                            put("receptor", art.getString("receptor"))
                        }
                        btn?.tag = tagInfo

                        btn?.setOnClickListener { view ->
                            try {
                                val tagData = view.tag as JSONObject
                                val idp = tagData.getString("idPedido")
                                val receptorId = tagData.getString("receptor")

                                // Determinar qué líneas servir según el contexto
                                val finalLineas = if (selectedReceptorId >= (receptores?.size ?: 0)) {
                                    // Caso "Todos" (Unidos) - usar el receptor específico del tag
                                    dbCuenta?.filterGroup("IDPedido=$idp and receptor=$receptorId")
                                } else {
                                    // Caso receptor específico seleccionado - verificar límites
                                    val id = receptores?.get(selectedReceptorId)?.getString("ID")
                                    if (id != null) {
                                        dbCuenta?.filterGroup("IDPedido=$idp and receptor=$id")
                                    } else {
                                        dbCuenta?.filterGroup("IDPedido=$idp and receptor=$receptorId")
                                    }
                                }

                                val p = ContentValues().apply {
                                    put("art", finalLineas.toString())
                                    put("idz", zn?.getString("ID"))
                                }
                                myServicio?.agregarInstruccion(
                                    Instruccion(p, "${myServicio?.getServerUrl()}/pedidos/servido") // Usar getServerUrl() de myServicio
                                )
                                if (finalLineas != null) {
                                    dbCuenta?.artServido(finalLineas)
                                }

                                selectReceptor(selectedReceptorId, showAllWaiters)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error serving pedido", e)
                            }
                        }
                        val send = grupo?.findViewById<ImageButton>(R.id.reeviarPedido)

                        // Usar el mismo objeto tag para reenviar
                        send?.tag = tagInfo
                        send?.setOnClickListener { view ->
                            try {
                                val tagData = view.tag as JSONObject
                                val idp = tagData.getString("idPedido")
                                val receptorId = tagData.getString("receptor")

                                // Determinar qué receptor usar según el contexto
                                val finalReceptorId = if (selectedReceptorId >= (receptores?.size ?: 0)) {
                                    // Caso "Todos" (Unidos) - usar el receptor específico del tag
                                    receptorId
                                } else {
                                    // Caso receptor específico seleccionado - verificar límites
                                    receptores?.get(selectedReceptorId)?.getString("ID")
                                }

                                val p = ContentValues().apply {
                                    put("idp", idp)
                                    put("idr", finalReceptorId)
                                }
                                myServicio?.agregarInstruccion(
                                    Instruccion(p, "$server/impresion/reenviarpedido", handlerOperaciones, "reenviar")
                                )
                            } catch (e: Exception) {
                              Log.e(TAG, "Error resending pedido", e)
                            }
                        }
                        paramsLinea = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                    val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val v = inflater.inflate(R.layout.linea_pedido_externo, ll, false)
                    val c = v.findViewById<TextView>(R.id.lblCantidad)
                    val n = v.findViewById<TextView>(R.id.lblNombre)
                    val b = v.findViewById<ImageButton>(R.id.btnBorrarPedido)
                    b.tag = art
                    val linea = v.findViewById<LinearLayout>(R.id.btnReenviarLinea)
                    linea.tag = art
                    linea.setOnLongClickListener { view ->
                        pedir(view)
                        true
                    }
                    c.text = art.getString("Can")
                    n.text = art.getString("Descripcion")
                    listaGr?.addView(v, paramsLinea)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error filling pedido", e)
        }
    }

    override fun rellenarReceptores() {
        try {
            receptores = dbReceptores?.getAll()
            botonesReceptores = pedidos?.contenedorReceptores
            botonesReceptores?.removeAllViews()
            receptores?.let { rec ->
                if (rec.isNotEmpty()) {
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        setMargins(5, 0, 5, 0)
                    }
                    val metrics = resources.displayMetrics
                    val dp = 5f
                    val fpixels = metrics.density * dp
                    val pixels = (fpixels + 0.5f).toInt()
                    rec.forEachIndexed { i, r ->
                        val btn = Button(cx).apply {
                            id = i
                            isSingleLine = false
                            textSize = pixels.toFloat()
                            tag = r
                            text = r.getString("nombre").trim().replace(" ", "\n")
                            setBackgroundResource(com.valleapp.valletpvlib.R.drawable.bg_pink)
                            setOnLongClickListener(this@Mesas)
                            setOnClickListener { selectReceptor(id, false) }
                            setOnLongClickListener {
                                selectReceptor(id, true)
                                true
                            }
                        }
                        botonesReceptores?.addView(btn, params)
                    }

                    // Añadir botón "Todos"
                    val todosButtonId = rec.size
                    val btnTodos = Button(cx).apply {
                        id = todosButtonId
                        isSingleLine = false
                        textSize = pixels.toFloat()
                        text = buildString {
                                append("Unidos")
                            }
                        setBackgroundResource(com.valleapp.valletpvlib.R.drawable.bg_green) // Color distintivo
                        setOnClickListener { selectReceptor(id, false) }
                        setOnLongClickListener {
                            selectReceptor(id, true)
                            true
                        }
                    }
                    botonesReceptores?.addView(btnTodos, params)
                    selectReceptor(0, false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error filling receptores", e)
        }
    }

    private fun selectReceptor(id: Int, all: Boolean) {
        selectedReceptorId = id
        showAllWaiters = all
        val numReceptores = receptores?.size ?: 0

        // Resetear colores de los botones de receptores
        for (l in 0 until numReceptores) {
            botonesReceptores?.findViewById<View>(l)?.setBackgroundResource(com.valleapp.valletpvlib.R.drawable.bg_pink)
        }
        // Resetear color del botón "Todos"
        botonesReceptores?.findViewById<View>(numReceptores)?.setBackgroundResource(com.valleapp.valletpvlib.R.drawable.bg_green)

        // Resaltar el botón seleccionado
        val f = botonesReceptores?.findViewById<View>(id)
        f?.setBackgroundResource(if (all) com.valleapp.valletpvlib.R.drawable.bg_red else com.valleapp.valletpvlib.R.drawable.bg_blue_light)

        // Cargar los pedidos. Si el ID es el del botón "Todos", pasar -1.
        rellenarPedido(if (id < numReceptores) id else -1, all)
    }

    override fun rellenarZonas() {
        try {
            if (dbZonas == null) return
            val lszonas = dbZonas?.getAll()
            val ll = listaMesas?.pneZonas
            ll?.removeAllViews()
            lszonas?.let { zonas ->
                if (zonas.length() > 0) {
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        setMargins(5, 0, 5, 0)
                    }
                    val metrics = resources.displayMetrics
                    val dp = 5f
                    val fpixels = metrics.density * dp
                    val pixels = (fpixels + 0.5f).toInt()
                    for (i in 0 until zonas.length()) {
                        val z = zonas.getJSONObject(i)
                        if (zn == null && i == 0) {
                            val aux = myServicio?.zona
                            zn = aux ?: z
                        }
                        val btn = Button(cx).apply {
                            id = i
                            isSingleLine = false
                            textSize = pixels.toFloat()
                            tag = z
                            text = z.getString("Nombre").trim().replace(" ", "\n")
                            val strRgb = z.getString("RGB")
                            if (strRgb.contains(",")) {
                                val rgb = strRgb.split(",")
                                setBackgroundColor(
                                    Color.rgb(
                                        rgb[0].trim().toInt(),
                                        rgb[1].trim().toInt(),
                                        rgb[2].trim().toInt()
                                    )
                                )
                            } else {
                                setBackgroundResource(com.valleapp.valletpvlib.R.drawable.bg_pink)
                            }
                            setOnLongClickListener(this@Mesas)
                            setOnClickListener {
                                zn = it.tag as JSONObject
                                myServicio?.zona = zn
                                rellenarMesas()
                            }
                        }
                        ll?.addView(btn, params)
                    }

                    rellenarMesas()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error filling zonas", e)
        }
    }

    @SuppressLint("InflateParams")
    private fun rellenarMesas() {
        try {
            val idz = zn?.getString("ID")
            val lsmesas = dbMesas?.getAll(idz.toString())
            listaMesas?.clearTable()
            lsmesas?.let { mesas ->
                if (mesas.length() > 0) {
                    val params = TableLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    val metrics = resources.displayMetrics
                    val rowParams = TableRow.LayoutParams(
                        0,
                        (metrics.density * 150).toInt()
                    ).apply {
                        setMargins(5, 5, 5, 5)
                    }
                    var row = TableRow(cx)
                    listaMesas?.addView(row, params)
                    for (i in 0 until mesas.length()) {
                        val m = mesas.getJSONObject(i)
                        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val v = inflater.inflate(R.layout.boton_mesa, null)
                        val btnCm = v.findViewById<ImageButton>(R.id.btnCambiarMesa)
                        val btnC = v.findViewById<ImageButton>(R.id.btnTicket)
                        val btnLs = v.findViewById<ImageButton>(R.id.btnListaTicket)
                        val panel = v.findViewById<LinearLayout>(R.id.pneBtnMesa)

                        if (m.getString("abierta") == "0") {
                            panel.visibility = View.GONE
                        } else {
                            panel.visibility = View.VISIBLE
                            inicializarBtnAux(btnC, btnCm, btnLs, m)
                        }
                        v.findViewById<Button>(R.id.btnMesa).apply {
                            id = i
                            isSingleLine = false
                            text = m.getString("Nombre")
                            textSize = 15f
                            tag = m
                            val strRgb = m.getString("RGB")
                            if (strRgb.contains(",")) {
                                val rgb = strRgb.trim().split(",")
                                setBackgroundColor(
                                    Color.rgb(
                                        rgb[0].trim().toInt(),
                                        rgb[1].trim().toInt(),
                                        rgb[2].trim().toInt()
                                    )
                                )
                            } else {
                                setBackgroundResource(com.valleapp.valletpvlib.R.drawable.bg_pink)
                            }
                            setOnClickListener {
                                try {
                                    val m1 = it.tag as JSONObject
                                    m1.put("Tarifa", zn?.getString("Tarifa"))
                                    val intent = Intent(cx, HacerComandas::class.java).apply {
                                        putExtra("op", "m")
                                        putExtra("cam", myServicio?.getCam().toString()) // Usar getCam() de myServicio
                                        putExtra("mesa", m1.toString())
                                    }
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error starting HacerComandas", e)
                                }
                            }
                        }
                        row.addView(v, rowParams)
                        if ((i + 1) % 3 == 0) {
                            row = TableRow(cx)
                            listaMesas?.addView(row, params)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error filling mesas", e)
        }
    }

    private fun inicializarBtnAux(btnC: ImageButton, btnCm: ImageButton, btnLs: ImageButton, m: JSONObject) {
        btnC.tag = m
        btnCm.tag = m
        btnLs.tag = m
        btnC.setOnClickListener {
            mostrarCuenta(it)
        }
        btnLs.setOnClickListener {
            clickMostrarPedidos(it)
        }
        btnCm.setOnClickListener {
            clickCambiarMesa(it)
        }
        btnCm.setOnLongClickListener {
            clickJuntarMesa(it)
            false
        }
    }

    private fun servirPedido(obj: JSONObject) {
        try {
            val p = ContentValues().apply {
                put("art", obj.toString())
                put("idz", zn?.getString("ID"))
            }
            myServicio?.agregarInstruccion(Instruccion(p, "$server/pedidos/servido"))
            val jobj = JSONArray()
            jobj.put(obj)
            dbCuenta?.artServido(jobj)
        } catch (e: Exception) {
            Log.e(TAG, "Error serving pedido", e)
        }
    }

    fun clickServido(v: View) {
        val obj = v.tag as JSONObject
        servirPedido(obj)
        selectReceptor(selectedReceptorId, showAllWaiters)
    }

    fun mostrarCuenta(v: View) {
        val intent = Intent(cx, Cuenta::class.java).apply {
            putExtra("mesa", v.tag.toString())
            putExtra("url", server)
        }
        startActivity(intent)
    }

    fun clickCambiarMesa(v: View) {
        val intent = Intent(cx, OpMesas::class.java).apply {
            putExtra("mesa", v.tag.toString())
            putExtra("op", "cambiar")
            putExtra("url", server)
        }
        startActivity(intent)
    }

    fun clickJuntarMesa(v: View) {
        val intent = Intent(cx, OpMesas::class.java).apply {
            putExtra("mesa", v.tag.toString())
            putExtra("op", "juntar")
            putExtra("url", server)
        }
        startActivity(intent)
    }

    fun clickMostrarPedidos(v: View) {
        val intent = Intent(cx, MostrarPedidos::class.java).apply {
            putExtra("mesa", v.tag.toString())
            putExtra("url", server)
        }
        startActivity(intent)
    }

    private fun cargarPreferencias() {
        val json = JSON() // Your JSON serialization/deserialization class
        try {
            // Attempt to deserialize the preferences file
            val pref = json.deserializar("preferencias.dat", this)
            uid = pref?.optString("uid", "") ?: "uid_default" // Default to "uid_default" if not found
          
            // If deserialization failed or returned null, exit the function
            if (pref == null) {
                zn = null // Ensure zn is null if preferences couldn't be loaded
                return
            }

            // Use optJSONObject to safely get the value associated with "zn" as a JSONObject.
            // This handles cases where "zn" is missing, null, or not a valid JSON object.
            val znStr = pref.optString("zn", "")
            if (znStr.isEmpty()) {
                zn = null // If "zn" is empty, set zn to null
                return
            }
            val zonaObject = JSONObject(znStr)
            zn = zonaObject


        } catch (e: Exception) {
            // Catch any other exceptions during the process (e.g., file reading errors)
            Log.e("MesasActivity", "Error loading preferences from file", e)
            zn = null // Ensure zn is null in case of any exception
        }
    }

    fun buscarPedidos(v: View) {
        val intent = Intent(cx, BuscarPedidos::class.java).apply {
            putExtra("url", server)
        }
        startActivity(intent)
    }

    // Declare the Activity Result Launcher
    private val autoriasActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        // This block is executed when the launched activity (Autorias) finishes
        if (result.resultCode == RESULT_OK) {
            // Handle the result here if needed
            val data: Intent? = result.data
            try {
                data?.getStringExtra("mensajes")?.let {
                    val p = JSONArray(it)
                    // Clear existing peticiones and add the new ones from the result
                    peticiones = JSONArray()
                    for (i in 0 until p.length()) {
                        peticiones.put(p.getJSONObject(i))
                    }
                }
            } catch (e: JSONException) {
                Log.e(TAG, "Error processing autorias result", e)
            }
            Log.d("MesasActivity", "Autorias activity finished with RESULT_OK. Peticiones updated.")
        } else {
            Log.d("MesasActivity", "Autorias activity finished with result code: ${result.resultCode}")
        }
        // Reset the flag when the activity returns
        isViewingMessages = false

        manejarAurotias()
    }

    fun mostrarAutorias(v: View?) {
        isViewingMessages = true
        val i = Intent(this, Autorias::class.java).apply { // Using 'this' for context
            putExtra("url", server)
            putExtra("peticiones", peticiones.toString())
            putExtra("uid", myServicio?.getUid()) // Usar getUid() de myServicio
        }
        autoriasActivityResultLauncher.launch(i)
    }

    fun mostrarSendMensajes(v: View) {
        try {
            // Verificar si el camarero tiene permiso de comandos por voz en permisos
            val permisos = cam?.optString("permisos", "") ?: ""
            val tieneComandosVoz = permisos.contains("comandos_voz")
            
            val intent = if (tieneComandosVoz) {
                // Si tiene permiso de comandos por voz, cargar ChatBotActivity
                Intent(cx, ChatBotActivity::class.java).apply {
                    putExtra("camarero", cam?.getString("ID"))
                    putExtra("url", server)
                    putExtra("uid", uid)
                }
            } else {
                // Si no tiene permiso, cargar SendMensajes
                Intent(cx, SendMensajes::class.java).apply {
                    putExtra("camarero", cam?.getString("ID"))
                    putExtra("url", server)
                    putExtra("uid", uid)
                }
            }
            
            startActivity(intent)
        } catch (e: JSONException) {
            Log.e(TAG, "Error showing send messages", e)
        }
    }


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mesas)

        val camstr = intent.getStringExtra("cam")
        cam = if (camstr != null) {
            try {
                JSONObject(camstr)
            } catch (e: JSONException) {
                Log.e(TAG, "Error parsing cam JSON", e)
                null
            }
        } else {
            null
        }
        val title = findViewById<TextView>(R.id.lblNombreCamarero)
        title.text = "${cam?.optString("nombre", "")} ${cam?.optString("apellidos", "")}"
        findViewById<View>(R.id.show_autorias).visibility = View.GONE

        pedidos = Pedidos(this)
        listaMesas = ListaMesas(this) // Assuming this is the Fragment you want to show
        
        // Ensure listaMesas and pedidos are not null before creating the adapter
        if (listaMesas == null || pedidos == null) {
            // Handle this error appropriately, maybe finish the activity or show a message
            Log.e("MesasActivity", "Failed to initialize listaMesas or pedidos")
            return
        }

        val adaptadorMesas = AdaptadorMesas(this, listaMesas!!, pedidos!!)
        val vpPager = findViewById<ViewPager2>(R.id.pager)
        vpPager.adapter = adaptadorMesas

        // Find the TabLayout in your layout
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)

        // Link the TabLayout to the ViewPager2
        TabLayoutMediator(tabLayout, vpPager) { tab, position ->
            tab.text = adaptadorMesas.getPageTitle(position)
        }.attach()

        // Verificar permisos y cambiar icono del botón de mensajes si tiene comandos_voz
        // Lo hacemos con un pequeño delay para asegurar que las vistas estén inicializadas
        val permisos = cam?.optString("permisos", "") ?: ""
        val tieneComandosVoz = permisos.contains("comandos_voz")
        
        Handler(Looper.getMainLooper()).postDelayed({
            listaMesas?.cambiarIconoMensajes(tieneComandosVoz)
        }, 100)




        // Manejo moderno del botón atrás
        // Create an OnBackPressedCallback instance
        val onBackPressedCallback = object : OnBackPressedCallback(true) { // 'true' means the callback is enabled initially
            override fun handleOnBackPressed() {
                // This is where your back press handling logic goes
                if (presBack >= 1) {
                    finish()
                } else {
                    customToast.showBottom("Pulsa otra vez para salir")
                    presBack++
                }
            }
        }
        // Add the callback to the dispatcher, associating it with this activity's lifecycle
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onDestroy() {
        unbindService(mConexion)
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onLongClick(view: View): Boolean {
        val json = JSON()
        try {
            val pref = json.deserializar("preferencias.dat", this) ?: JSONObject()
            val zonaActualStr = pref.optString("zn", "")

            val zonaActual = if (zonaActualStr.isNotEmpty()) {
                try {
                    JSONObject(zonaActualStr)
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }
            val zonaNueva = view.tag as JSONObject

           
            // Si no hay zona actual (null o empty), cargar la zona nueva
            if (zonaActual == null || zonaActual.length() == 0) {
                pref.put("zn", zonaNueva.toString())
                json.serializar("preferencias.dat", pref, cx)
                customToast.showTop("Zona asociada correctamente")
            } else {
                // Si ya hay una zona, comparar con la nueva
                val idZonaActual = zonaActual.optString("ID", "")
                val idZonaNueva = zonaNueva.optString("ID", "")


                if (idZonaActual == idZonaNueva) {
                    // Si es la misma zona, borrarla
                    pref.put("zn", "")
                    json.serializar("preferencias.dat", pref, cx)
                    customToast.showTop("Asociación de zona eliminada")
                } else {
                    // Si es una zona diferente, reemplazarla
                    pref.put("zn", zonaNueva.toString())
                    json.serializar("preferencias.dat", pref, cx)
                    customToast.showTop("Zona cambiada correctamente")

                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onLongClick", e)
        }
        return false
    }

    override fun onResume() {
        presBack = 0

        if (myServicio == null) {
            val intent = Intent(applicationContext, ServiceCOM::class.java)
            bindService(intent, mConexion, BIND_AUTO_CREATE)
        } else {
            isViewingMessages = false
        }

        cargarPreferencias()
        manejarAurotias()

        super.onResume()
    }

    override fun pedir(v: View) {
        try {
            val obj = v.tag as JSONObject
            val p = ContentValues().apply {
                put("idp", obj.getString("IDPedido"))
                put("id", obj.getString("IDArt"))
                put("Descripcion", obj.getString("Descripcion"))
            }
            myServicio?.agregarInstruccion(
                Instruccion(p, "${myServicio?.getServerUrl()}/impresion/reenviarlinea", handlerOperaciones, "reenviar") // Usar getServerUrl()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in pedir", e)
        }
    }

}