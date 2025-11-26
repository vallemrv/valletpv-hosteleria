package com.valleapp.vallecom.activitys

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context

import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.*

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.valleapp.vallecom.adaptadores.AdaptadorComanda
import com.valleapp.vallecom.adaptadores.AdaptadorPedidos
import com.valleapp.vallecom.interfaces.IComanda
import com.valleapp.vallecom.interfaces.INota
import com.valleapp.vallecom.interfaces.ITeclados
import com.valleapp.vallecom.tab.Comanda
import com.valleapp.vallecom.tab.SeccionesCom
import com.valleapp.vallecom.utilidades.ActivityBase
import com.valleapp.vallecom.utilidades.Nota
import com.valleapp.vallecom.utilidades.ServiceCOM
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.db.DBCuenta
import com.valleapp.valletpvlib.db.DBMesas
import com.valleapp.valletpvlib.db.DBSecciones
import com.valleapp.valletpvlib.db.DBTeclas
import com.valleapp.valletpvlib.tools.Instruccion
import com.valleapp.valletpvlib.tools.JSON
import com.valleapp.valletpvlib.R as LibR

import org.json.JSONException
import org.json.JSONObject
import java.util.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HacerComandas : ActivityBase(), INota, IComanda, ITeclados {

    private var comanda: Comanda? = null
    private var seccionesCom: SeccionesCom? = null


    private lateinit var dbMesas: DBMesas
    private lateinit var dbTeclas: DBTeclas
    private lateinit var dbSecciones: DBSecciones
    private lateinit var dbCuenta: DBCuenta

    private lateinit var mesa: JSONObject

    private var can = 1
    private var tarifa = 1

    private var sec: JSONObject? = null
    private lateinit var artSel: JSONObject
    private lateinit var nota: Nota

    private lateinit var cantidad: TextView
    private lateinit var infPedido: TextView

    private var pause = false

    private lateinit var sugerenciaLauncher: ActivityResultLauncher<Intent>
    private lateinit var buscarArticuloLauncher: ActivityResultLauncher<Intent>
    private lateinit var refillLauncher: ActivityResultLauncher<Intent>

    private val mConexion = object : ServiceConnection {
        @SuppressLint("SetTextI18n")
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            myServicio = (iBinder as ServiceCOM.MyBinder).getService()
            myServicio?.let {
                dbMesas = it.getDb("mesas") as DBMesas
                dbSecciones = it.getDb("seccionescom") as DBSecciones
                dbTeclas = it.getDb("teclas") as DBTeclas
                dbCuenta = it.getDb("lineaspedido") as DBCuenta
                comanda = Comanda(this@HacerComandas)

                seccionesCom = SeccionesCom(this@HacerComandas, dbSecciones.getAll())
                val aComanda = AdaptadorComanda(this@HacerComandas, comanda!!, seccionesCom!!)
                server = it.getServerUrl()
                cargarPreferencias()

                val vpPager = findViewById<ViewPager2>(R.id.pager) // Cambiar a ViewPager2
                val title = findViewById<TextView>(R.id.lblNombreCamarero)
                
                vpPager.adapter = aComanda

                val tableLayout = findViewById<TabLayout>(R.id.tab_layout)
                TabLayoutMediator(tableLayout, vpPager) { tab, position ->
                    tab.text = aComanda.getPageTitle(position)
                }.attach()

                try {
                    val nombre = "${cam?.getString("nombre")} ${cam?.getString("apellidos")}"
                    title.text = "$nombre -- ${mesa.getString("Nombre")}"
                } catch (e: JSONException) {
                    Log.e("HacerComandas", "Error al obtener datos del camarero o mesa", e)
                }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            myServicio = null
        }
    }

    override fun cargarNota() {
        nota = Nota(mesa, this, this)
        rellenarComanda()
    }

    override fun agregarLinea(v: View) {
        val art = v.tag as JSONObject
        nota.addArt(art, can)
    }

    override fun borrarLinea(v: View) {
        val art = v.tag as JSONObject
        nota.rmArt(art)
    }

    override fun getContexto(): Context {
        return cx
    }

    private fun cargarPreferencias() {
        val json = JSON()
        try {
            val pref = json.deserializar("preferencias.dat", this)
            sec = pref?.isNull("sec")?.let {
                if (!it) {
                    dbSecciones.filter("Nombre = '${pref.getString("sec")}'").getJSONObject(0)
                } else {
                    dbSecciones.getAll().optJSONObject(0)
                }
            }
        } catch (e: Exception) {
            Log.e("HacerComandas", "Error al cargar preferencias", e)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun rellenarComanda() {
        try {
            val num = nota.getNum()
            val lPedidos = if (num > 0) {
                infPedido.text = "$num articulos"
                nota.getLineas()
            } else {
                infPedido.text = "0 articulos"
                emptyList()
            }
            comanda?.apply {
               getListaPedidos()?.adapter = AdaptadorPedidos(this@HacerComandas , lPedidos) // Convierte AdaptadorPedidos a un adaptador compatible con ListView
               setCantidad(nota.getNum().toString())
            }
            can = 1
            cantidad.text = can.toString()
        } catch (e: Exception) {
            Log.e("HacerComandas", "Error al rellenar comanda", e)
        }
    }

    override fun rellenarBotonera() {
        try {
            if (sec != null) {
                val lsart = dbTeclas.getAll(sec!!.getString("ID"), tarifa)
                val ll = seccionesCom!!.panel as LinearLayout
                ll.removeAllViews()

                if (lsart.length() > 0) {
                    val rowParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply { weight = 1f } // Cambiado a 1f para que sea Float
                    val colParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        weight = 1f // Cambiado a 1f para que sea Float
                        setMargins(5, 5, 5, 5)
                    }

                    var row = LinearLayout(cx).apply { orientation = LinearLayout.HORIZONTAL }
                    ll.addView(row, rowParams)

                    for (i in 0 until lsart.length()) {
                        val a = lsart.getJSONObject(i)
                        val inflater = cx.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val v = inflater.inflate(LibR.layout.btn_art, ll, false) // Pasar 'll' como parent y 'false' para no adjuntar

                        val btn = v.findViewById<Button>(LibR.id.boton_art)

                        a.getInt("hay_existencias").let { hayExistencias ->
                            btn.isEnabled = hayExistencias == 1
                            if (hayExistencias == 1) {
                                v.findViewById<ImageView>(LibR.id.ic_no_hay_existencias).visibility = View.GONE
                            }else{
                                v.findViewById<ImageView>(LibR.id.ic_no_hay_existencias).visibility = View.VISIBLE
                            }
                        }

                        if (a.getString("RGB").isNotEmpty()) {
                            val rgb = a.getString("RGB").split(",")
                            btn.setBackgroundColor(Color.rgb(rgb[0].toInt(), rgb[1].toInt(), rgb[2].toInt()))
                        } else {
                            btn.setBackgroundResource(LibR.drawable.bg_pink)
                        }

                        btn.id = i
                        btn.isSingleLine = false
                        btn.text = a.getString("Nombre").trim()
                        btn.tag = JSONObject(a.toString())

                        btn.setOnClickListener {
                            findViewById<View>(R.id.btn_reffil).visibility = View.GONE
                            val art = it.tag as JSONObject
                            pedirArt(art)
                        }

                        row.addView(v, colParams)

                        if (i < lsart.length() - 1 && (i + 1) % 3 == 0) {
                            row = LinearLayout(cx).apply { orientation = LinearLayout.HORIZONTAL }
                            ll.addView(row, rowParams)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("HacerComandas", "Error al rellenar botonera", e)
        }
    }

    private fun rellenarSub() {
        try {
            val lsart = dbTeclas.getAllSub(artSel.getString("ID"))
            val ll = seccionesCom!!.panel as LinearLayout
            ll.removeAllViews()
            val length = lsart.length()
            if (length > 0) {
                val metrics = resources.displayMetrics
                val w = if (length > 9) LinearLayout.LayoutParams.MATCH_PARENT else (metrics.density * 100).toInt()
                val ww = if (length > 9) 1 else 0

                val rowParamsSub = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, w
                ).apply { weight = ww.toFloat() } // Convertido a Float
                val colParamsSub = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 1f // Cambiado a 1f para que sea Float
                    setMargins(5, 5, 5, 5)
                }

                var row = LinearLayout(cx).apply { orientation = LinearLayout.HORIZONTAL }
                ll.addView(row, rowParamsSub)

                for (i in 0 until lsart.length()) {
                    val m = lsart.getJSONObject(i)
                    val inflater = cx.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val v = inflater.inflate(LibR.layout.btn_art, ll, false) // Pasar 'll' como parent y 'false' para no adjuntar
                    val btn = v.findViewById<Button>(LibR.id.boton_art)

                    btn.id = i
                    btn.tag = m
                    btn.isSingleLine = false
                    btn.text = m.getString("Nombre")


                    m.getInt("hay_existencias").let { hayExistencias ->
                        btn.isEnabled = hayExistencias == 1
                        if (hayExistencias == 1) {
                            v.findViewById<ImageView>(LibR.id.ic_no_hay_existencias).visibility = View.GONE
                        }else{
                            v.findViewById<ImageView>(LibR.id.ic_no_hay_existencias).visibility = View.VISIBLE
                        }
                    }

                    if (m.getString("RGB").isNotEmpty()) {
                        val rgb = m.getString("RGB").split(",")
                        btn.setBackgroundColor(Color.rgb(rgb[0].toInt(), rgb[1].toInt(), rgb[2].toInt()))
                    } else {
                        btn.setBackgroundResource(LibR.drawable.bg_pink)
                    }
                    btn.setOnClickListener {
                        val obj = it.tag as JSONObject
                        pedirArt(obj)
                        if (obj.getString("tipo") == "SP") {
                            rellenarBotonera()
                        }
                    }

                    row.addView(v, colParamsSub)

                    if (i < lsart.length() - 1 && (i + 1) % 3 == 0) {
                        row = LinearLayout(cx).apply { orientation = LinearLayout.HORIZONTAL }
                        ll.addView(row, rowParamsSub)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("HacerComandas", "Error al rellenar sub", e)
        }
    }

    

    private fun pedirArt(art: JSONObject) {
        try {
            val aux = JSONObject(art.toString()).apply {
                val descripcionOriginal = getString("descripcion_r")
                put("Descripcion", descripcionOriginal)
            }
            if (art.getString("tipo") == "SP") {
                customToast.showBottom(art.getString("Nombre"))
                nota.addArt(aux, can)
            } else {
                artSel = aux
                rellenarSub()
            }
        } catch (e: JSONException) {
            Log.e("HacerComandas", "Error al procesar artículo", e)
        }
    }

    fun clickMenu(v: View) {
        findViewById<View>(R.id.btn_reffil).visibility = View.GONE
        try {
            sec = dbSecciones.filter("Nombre = '${v.tag}'").getJSONObject(0)
            rellenarBotonera()
        } catch (e: JSONException) {
            Log.e("HacerComandas", "Error al hacer clic en el menú", e)
        }
    }

    @SuppressLint("SetTextI18n")
    fun clickCan(v: View) {
        can = v.tag.toString().toInt()
        cantidad.text = can.toString()
    }

    fun clickEnviarComanda(v: View) {
        try {
            val p = ContentValues()
            val nl = nota.getLineas()
            val idm = mesa.getString("ID")
            val idc = cam?.getString("ID")
            p.put("idm", idm)
            p.put("pedido", nl.toString())
            p.put("idc", idc)
            p.put("uid_device", UUID.randomUUID().toString())
            println("Enviando comanda: $p")
            myServicio?.let {
                nota.eliminarComanda()
                it.agregarInstruccion(Instruccion(p, "$server/comandas/pedir"))

                dbMesas.abrirMesa(mesa.getString("ID"))
                finish()
            }
        } catch (e: Exception) {
            Log.e("HacerComandas", "Error al enviar comanda", e)
        }
    }



    fun clickSugerencia(v: View) {
        pause = true
        val art = v.tag as JSONObject
        val intent = Intent(cx, Sugerencias::class.java).apply {
            putExtra("url", server)
            putExtra("art", nota.getArt(art))
        }
        sugerenciaLauncher.launch(intent)
    }

    fun clickBuscarArticulo(v: View) {
        pause = true
        val intent = Intent(cx, BuscadorTeclas::class.java).apply {
            putExtra("Tarifa", tarifa.toString())
        }
        buscarArticuloLauncher.launch(intent)
    }

    fun onRefill(v: View) {
        try {
            pause = true
            val intent = Intent(cx, Refill::class.java).apply {
                putExtra("id_mesa", mesa.getString("ID"))
            }
            refillLauncher.launch(intent)
        } catch (e: JSONException) {
            Log.e("HacerComandas", "Error al hacer refill", e)
        }
    }

    override fun asociarBotonera(v: View) {
        val json = JSON()
        try {
            val pref = json.deserializar("preferencias.dat", this)
            pref?.put("sec", v.tag.toString())
            json.serializar("preferencias.dat", pref, cx)
            customToast.showBottom("Asociacion realizada")
        } catch (e: Exception) {
            Log.e("HacerComandas", "Error al asociar botonera", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hacer_comanda)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Aquí puedes manejar el evento de retroceso
                finish() // O cualquier otra lógica que necesites
            }
        })

        cantidad = findViewById(R.id.lblCantidad)
        infPedido = findViewById(R.id.lblPedido)


        try {
            server = intent.extras?.getString("url") ?: ""
            cam = JSONObject(intent.extras?.getString("cam") ?: "")
            mesa = JSONObject(intent.extras?.getString("mesa") ?: "")
            tarifa = mesa.getInt("Tarifa")
        } catch (e: Exception) {
            Log.e("HacerComandas", "Error al obtener datos del intent", e)
        }

        sugerenciaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val sug = result.data?.getStringExtra("sug") ?: ""
                val incremento = result.data?.getDoubleExtra("incremento", 0.0) ?: 0.0
                nota.addSug(sug, incremento)
            }
        }

        buscarArticuloLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val art = JSONObject(result.data?.getStringExtra("art") ?: "")
                    nota.addArt(art, can)
                } catch (e: JSONException) {
                    Log.e("HacerComandas", "Error al obtener artículo del buscador", e)
                }
            }
        }

        refillLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val idPedido = result.data?.getStringExtra("IDPedido") ?: ""
                val lPedidos = dbCuenta.filterGroup("estado = 'P' AND IDPedido = $idPedido")
                for (i in 0 until lPedidos.length()) {
                    try {
                        val art = lPedidos.getJSONObject(i)
                        nota.addArt(art, art.getInt("Can"))
                    } catch (e: JSONException) {
                        Log.e("HacerComandas", "Error al agregar artículo del refill", e)
                    }
                }
            }
        }



    }

    override fun onResume() {
        super.onResume()
        if (myServicio == null) {
            val intent = Intent(baseContext, ServiceCOM::class.java).apply {
                putExtra("server", server)
            }
            bindService(intent, mConexion, BIND_AUTO_CREATE)
        } else {
            cargarPreferencias()
        }
        cargarNota()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            100 if resultCode == RESULT_OK -> {
                try {
                    val art = JSONObject(data?.getStringExtra("art") ?: "")
                    nota.addArt(art, can)
                } catch (e: JSONException) {
                    Log.e("HacerComandas", "Error al obtener artículo del buscador", e)
                }
            }
            200 if resultCode == RESULT_OK -> {
                val sug = data?.getStringExtra("sug") ?: ""
                nota.addSug(sug)
            }
            300 if resultCode == RESULT_OK -> {
                val idPedido = data?.getStringExtra("IDPedido") ?: ""
                val lPedidos = dbCuenta.filter("estado = 'P' AND IDPedido = $idPedido")
                for (i in 0 until lPedidos.length()) {
                    try {
                        val art = lPedidos.getJSONObject(i)
                        nota.addArt(art, art.getInt("Can"))
                    } catch (e: JSONException) {
                        Log.e("HacerComandas", "Error al agregar artículo del refill", e)
                    }
                }
            }
        }
        pause = false
    }

    override fun onDestroy() {
        try {
            myServicio?.let { unbindService(mConexion) }
        } catch (e: Exception) {
            Log.e("HacerComandas", "Error al destruir actividad", e)
        }
        super.onDestroy()
    }



    override fun onPause() {
        super.onPause()
    }
}