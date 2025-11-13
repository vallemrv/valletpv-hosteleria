package com.valleapp.vallecom.activitys

import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ListView
import android.widget.TextView
import com.valleapp.vallecom.adaptadores.AdaptadorMensajes
import com.valleapp.vallecom.utilidades.ActivityBase
import com.valleapp.vallecom.utilidades.ServiceCOM
import com.valleapp.valletpv.R
import com.valleapp.valletpvlib.db.DBReceptores
import com.valleapp.valletpvlib.tools.Instruccion
import org.json.JSONArray
import org.json.JSONObject

class SendMensajes : ActivityBase() {

    private var dbReceptores: DBReceptores? = null
    private var camarero: String? = null

    private val mConexion: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            myServicio = (iBinder as ServiceCOM.MyBinder).getService()
            if (myServicio != null) {
                try {
                    dbReceptores = myServicio!!.getDb("receptores") as DBReceptores
                    server = myServicio!!.getServerUrl()
                    mostrarLista()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            myServicio = null
        }
    }

    fun onClickItemSimple(v: View) {
        try {
            val t = findViewById<TextView>(R.id.txt_mensaje)
            if (t.text.toString().trim { it <= ' ' } != "") {
                val p = ContentValues()
                val o = JSONObject()
                val array = JSONArray()
                array.put(o)
                o.put("camarero", camarero)
                o.put("mensaje", t.text.toString())
                o.put("receptor", v.tag.toString())
                p.put("rows", array.toString())
                p.put("tb", "historialmensajes")
                myServicio!!.agregarInstruccion(Instruccion(p, "$server/sync/update_from_devices"))
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun mostrarLista() {
        try {
            val ls = findViewById<ListView>(R.id.lista_receptores)
            val lista = dbReceptores!!.getAll()
            val adaptador = AdaptadorMensajes(this, lista)
            ls.adapter = adaptador.toListAdapter() // Convierte AdaptadorMensajes a un adaptador compatible con ListView
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensajes)
        camarero = intent.extras!!.getString("camarero")
    }

    override fun onResume() {
        super.onResume()
        if (myServicio == null) {
            val intent = Intent(applicationContext, ServiceCOM::class.java)
            bindService(intent, mConexion, BIND_AUTO_CREATE)
        } else {
            mostrarLista()
        }
    }

    override fun onDestroy() {
        unbindService(mConexion)
        super.onDestroy()
    }
}
