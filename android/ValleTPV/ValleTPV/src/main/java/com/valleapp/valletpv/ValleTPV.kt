package com.valleapp.valletpv

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tu.paquete.CustomToast
import com.valleapp.valletpv.adaptadoresDatos.AdaptadorSelCam
import com.valleapp.valletpv.dlg.DlgAddNuevoCamarero
import com.valleapp.valletpv.tools.ServiceTPV
import com.valleapp.valletpv.tpvcremoto.SocketManager
import com.valleapp.valletpvlib.db.DBCamareros
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONException
import org.json.JSONObject

class ValleTPV : AppCompatActivity() {

    private var conectado: Boolean = false
    private val cx = this
    private var server = ""

    private var myServicio: ServiceTPV? = null

    private var socketManager: SocketManager? = null

    private lateinit var dbCamareros: DBCamareros
    private lateinit var lstNoAutorizados: ListView
    private lateinit var lstAutorizados: ListView

    private var urlCashlogy = ""
    private var usarCashlogy = false
    private var usarTPVPC = false
    private var ipTPVPC = ""
    private val customToast = CustomToast(this)

    private val handleHttp = Handler(Looper.getMainLooper()) { msg ->
        val bundle = msg.data
       
        if (bundle.containsKey("CashlogyMsg")) {
            val toastMessage = bundle.getString("CashlogyMsg")
            if (toastMessage != null) {
                customToast.showBottom(toastMessage, Toast.LENGTH_LONG)
            }
        } else {
            rellenarListas()
        }
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_valletpv)

        val salirBtn: ImageButton = findViewById(R.id.salir)
        lstAutorizados = findViewById(R.id.lstautorizados)
        lstNoAutorizados = findViewById(R.id.lstnoautorizados)

        salirBtn.setOnClickListener { finish() }

        val aceptarBtn: ImageButton = findViewById(R.id.aceptar)
        aceptarBtn.setOnClickListener {
            val intent = Intent(applicationContext, Camareros::class.java)
            intent.putExtra("server", server)
            startActivity(intent)
        }

        val prefBtn: ImageButton = findViewById(R.id.btn_aceptar_preferencias)
        prefBtn.setOnClickListener {
            val intent = Intent(applicationContext, PreferenciasTPV::class.java)
            startActivity(intent)
        }

        val arqueoBtn: ImageButton = findViewById(R.id.btn_arqueo_caja)
        arqueoBtn.setOnClickListener {
            val intent = Intent(applicationContext, Arqueo::class.java)
            startActivity(intent)
        }

        lstNoAutorizados.setOnItemClickListener { _, view, _, _ ->
                val obj = view.tag as JSONObject
            try {
                dbCamareros.setAutorizado(obj.getInt("ID"), true)
                obj.put("autorizado", "1")
                myServicio?.autorizarCam(obj)
            } catch (e: JSONException) {
                Log.e("VALLETPV_ERR", e.toString())
            }
            rellenarListas()
        }

        findViewById<ImageButton>(R.id.btn_add_nuevo_camarero).setOnClickListener {
            val dlg = DlgAddNuevoCamarero(cx, myServicio)
            dlg.show()
        }

        lstAutorizados.setOnItemClickListener { _, view, _, _ ->
                val obj = view.tag as JSONObject
            try {
                obj.put("autorizado", "0")
                dbCamareros.setAutorizado(obj.getInt("ID"), false)
                myServicio?.autorizarCam(obj)
            } catch (e: JSONException) {
                Log.e("VALLETPV_ERR", e.toString())
            }
            rellenarListas()
        }
        cargarPreferencias()

        if (!usarTPVPC) return

        val address = ipTPVPC.split(":")
        if (address.size != 2) return
        val ip = address[0]
        val port = address[1].toInt()

        socketManager = SocketManager(ip, port)

        // Iniciar la conexión al servidor TPVPC
        socketManager!!.iniciarConexionSocket(
            onSuccess = {
                // Iniciar el pinpad
                socketManager!!.iniciarPinpad()
            },
            onError = { errorMsg ->
                runOnUiThread {
                    customToast.showBottom( "Error de conexión: $errorMsg", Toast.LENGTH_SHORT)
                }
            },
            onRespuesta = { bundle ->
                manejarRespuesta(bundle)
            }
        )

    }

    private fun rellenarListas() {
        lstAutorizados.adapter = AdaptadorSelCam(cx, dbCamareros.getAutorizados(true))
        lstNoAutorizados.adapter = AdaptadorSelCam(cx, dbCamareros.getAutorizados(false))
    }

    override fun onResume() {
        super.onResume()
        cargarPreferencias()

        myServicio?.let { rellenarListas() }
        if (server.isNotEmpty()) {
            val intent = Intent(applicationContext, ServiceTPV::class.java).apply {
                putExtra("url", server)
                putExtra("url_cashlogy", urlCashlogy)
                putExtra("usar_cashlogy", usarCashlogy)
                putExtra("usar_tpvpc", usarTPVPC)
                putExtra("ip_tpvpc", ipTPVPC)
            }
            startService(intent)
            bindService(intent, mConexion, BIND_AUTO_CREATE)
        }
    }


    // Función para procesar las respuestas recibidas desde SocketManager
    private fun manejarRespuesta(bundle: Bundle) {
        val estado = bundle.getString("estado")
        println("estado: $estado")
        when (estado) {

            "fallo" -> {
                conectado = false
                socketManager?.cerrarConexion()
                runOnUiThread {
                    customToast.showBottom("Pinpad error de conexión, reinicie la aplicacion", Toast.LENGTH_SHORT)
                }
            }
            "error" -> {
                conectado = true
                socketManager?.cerrarConexion()
                runOnUiThread {
                    customToast.showBottom( "Error en el pinpad. reinicadado...", Toast.LENGTH_SHORT)
                }
            }
            "iniciando" -> {
                conectado = false
                runOnUiThread {
                    customToast.showBottom("El pinpad se esta iniciando.....", Toast.LENGTH_SHORT)
                }
            }
            "iniciado" -> {
                conectado = true
                socketManager?.cerrarConexion()
                runOnUiThread {
                    customToast.showBottom("El pinpad está iniciado correctamente.", Toast.LENGTH_SHORT)
                }
            }
        }
    }

    override fun onDestroy() {
        unbindService(mConexion)
        socketManager?.cerrarConexion()

        Intent(cx, ServiceTPV::class.java).also {
            stopService(it)
        }
        super.onDestroy()
    }

    private fun cargarPreferencias() {
        val json = JSON()
        try {
            val pref = json.deserializar("preferencias.dat", this)
            if (pref == null) {
                // Si no hay preferencias guardadas, redirigir a la actividad de preferencias
                val intent = Intent(this, PreferenciasTPV::class.java)
                startActivity(intent)
            } else {
                // Cargar URL del servidor
                server = pref.getString("URL")

                // Cargar preferencias de Cashlogy
                urlCashlogy = pref.optString("URL_Cashlogy", "")
                usarCashlogy = pref.optBoolean("usaCashlogy", false)

                // Cargar preferencias de TPVPC
                usarTPVPC = pref.optBoolean("usaTPVPC", false)
                ipTPVPC = pref.optString("IP_TPVPC", "")
            }
        } catch (e: Exception) {
            Log.e("VALLETPV_ERR", e.toString())
        }
    }

    private val mConexion: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            myServicio = (iBinder as ServiceTPV.MyBinder).service
            dbCamareros = myServicio?.getDb("camareros") as DBCamareros
            myServicio?.executeCaslogy(usarCashlogy, urlCashlogy)
            myServicio?.setExHandler("camareros", handleHttp)
            rellenarListas()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            myServicio = null
        }
    }




}
