package com.valleapp.vallecom.activitys

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import com.getbase.floatingactionbutton.FloatingActionButton
import com.valleapp.vallecom.utilidades.ActivityBase
import com.valleapp.vallecom.utilidades.ServiceCOM
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.db.DBCamareros
import com.valleapp.valletpvlib.tools.Instruccion
import com.valleapp.valletpvlib.tools.JSON
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Camareros : ActivityBase() {

    private var lscam: ArrayList<JSONObject>? = null
    private var dbCamareros: DBCamareros? = null
    private var uid = ""


    private val mConexion = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            myServicio = (iBinder as ServiceCOM.MyBinder).getService()
            myServicio?.let {
                it.setExHandler("camareros", handlerHttp)
                dbCamareros = it.getDb("camareros") as DBCamareros
                myServicio?.setUid(uid) // Establecer UID en ServiceBase
                myServicio?.setServerUrl(server) // Establecer URL en ServiceBase
                mostrarListado()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            myServicio = null
        }
    }

    private val handlerHttp = Handler(Looper.getMainLooper()) { msg ->
        val op = msg.data.getString("op")
        if (op == "listado") {
            try {
                val res = msg.data.getString("RESPONSE")

                if (res  == null) {
                    customToast.showBottom("Error al cargar camareros")
                    return@Handler true
                }
               
                if (res.contains("\"success\": false")) {
                    val error = JSONObject(res).getJSONObject("errors")
                    customToast.showTop(error.getString("error"), Toast.LENGTH_LONG)
                    val ll = findViewById<TableLayout>(R.id.pneCamareros)
                    ll.removeAllViews()
                    return@Handler true

                }

                dbCamareros?.rellenarTabla(JSONArray(res))
                mostrarListado()
            } catch (e: JSONException) {
                Log.e("Camareros", "Error al cargar camareros $e", e)
            }
        } else {
            mostrarListado()
        }
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camareros)
        val btnReload = findViewById<FloatingActionButton>(R.id.btn_reload_camareros)
        btnReload.setOnClickListener { reloadCamareros() }
    }

    override fun onResume() {
        cargarPreferencias()
        if (myServicio == null) {
            val intent = Intent(applicationContext, ServiceCOM::class.java)
            intent.putExtra("url", server)
            intent.putExtra("uid", uid)
            startService(intent)
            bindService(intent, mConexion, BIND_AUTO_CREATE)
        }
        super.onResume()
    }

    override fun onDestroy() {
        unbindService(mConexion)
        val intent = Intent(cx, ServiceCOM::class.java)
        stopService(intent)
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    fun mostrarListado() {
        try {
            lscam = dbCamareros?.getAutorizados(true)
            val ll = findViewById<TableLayout>(R.id.pneCamareros)
            ll.removeAllViews()
            lscam?.let { list ->
                if (list.isNotEmpty()) {
                    val params = TableLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )

                    var row = TableRow(cx)
                    ll.addView(row, params)

                    var i = 0
                    for (cam in list) {
                        val btnCamarero = Button(cx).apply {
                            setBackgroundResource(com.valleapp.valletpvlib.R.drawable.bg_pink)
                            width = 20
                            tag = cam
                            isSingleLine = false
                            textSize = 15f
                            text = "${cam.getString("nombre")}\n${cam.getString("apellidos")}"
                        }

                        btnCamarero.setOnClickListener { view ->
                            try {
                                val camSeleccionado = view.tag as JSONObject
                                val camPass = camSeleccionado.getString("pass_field") ?: ""
                                if (camPass.isEmpty() || camPass == "null") {
                                    val createPass = Dialog(cx).apply {
                                        setTitle("Crear una contraseña")
                                        setContentView(R.layout.dialog_create_pass)
                                    }
                                    val pass = createPass.findViewById<TextView>(R.id.create_pass_text)
                                    val passRep = createPass.findViewById<TextView>(R.id.repetir_pass_text)
                                    createPass.findViewById<View>(R.id.btnCrearPass).setOnClickListener {
                                        try {
                                            if (passRep.text.toString() != pass.text.toString()) {
                                                customToast.showBottom("Las contraseñas no coinciden.", Toast.LENGTH_SHORT)
                                            } else {
                                                val p = ContentValues().apply {
                                                    put("cam", camSeleccionado.toString())
                                                    put("password", pass.text.toString())
                                                }
                                                myServicio?.agregarInstruccion(
                                                    Instruccion(p, "${myServicio?.getServerUrl()}/camareros/crear_password") // Usar server encapsulado
                                                )
                                                createPass.dismiss()
                                                entrarEnMesas(camSeleccionado.toString())
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    createPass.show()
                                } else {
                                    val enterPass = Dialog(cx).apply {
                                        setContentView(R.layout.dialog_enter_pass)
                                        setTitle("Contraseña")
                                    }
                                    val pass = enterPass.findViewById<TextView>(R.id.enter_pass_text)
                                    enterPass.findViewById<View>(R.id.enter_pass_boton).setOnClickListener {
                                        try {
                                            if (pass.text.toString() == camPass) {
                                                entrarEnMesas(camSeleccionado.toString())
                                            } else {
                                                customToast.showBottom("Usuario no autorizado.", Toast.LENGTH_SHORT)
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        enterPass.dismiss()
                                    }
                                    enterPass.show()
                                }
                            } catch (e: JSONException) {
                                Log.e("Camareros", "Error al acceder al camarero seleccionado: $e")
                            }
                        }

                        val metrics = resources.displayMetrics
                        val rowParams = TableRow.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            (metrics.density * 100).toInt()
                        ).apply {
                            setMargins(5, 5, 5, 5)
                        }
                        row.addView(btnCamarero, rowParams)

                        if ((i + 1) % 3 == 0) {
                            row = TableRow(cx)
                            ll.addView(row, params)
                        }
                        i++
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Camareros", "Error al mostrar el listado de camareros: $e")
        }
    }

    fun reloadCamareros() {
        customToast.showCenter(
            "Refrescando camareros",
            Toast.LENGTH_SHORT
        )
        comprobarCamareros()
    }

    private fun comprobarCamareros() {
        myServicio?.comprobarCamareros(handlerHttp) // Obtener URL desde ServiceBase
    }

    private fun entrarEnMesas(cam: String) {
        val intent = Intent(cx, Mesas::class.java).apply {
            putExtra("cam", cam)
        }
        startActivity(intent)
    }

    private fun cargarPreferencias() {
        val json = JSON()
        try {
            val pref = json.deserializar("preferencias.dat", this)
            if (pref == null) {
                val intent = Intent(this, Preferencias::class.java)
                startActivity(intent)
            } else {
                uid = pref.optString("uid", "")
                server = pref.optString("URL", "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}