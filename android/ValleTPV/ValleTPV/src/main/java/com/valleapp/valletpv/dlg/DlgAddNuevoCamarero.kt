package com.valleapp.valletpv.dlg

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.tu.paquete.CustomToast
import com.valleapp.valletpv.R
import com.valleapp.valletpv.tools.ServiceTPV
import com.valleapp.valletpvlib.db.DBCamareros

/**
 * Created by valle on 19/10/14.
 */
class DlgAddNuevoCamarero(context: Context, myService: ServiceTPV?) : Dialog(context) {

    private var servicio: ServiceTPV? = null
    var cx: Context? = null

    init {
        this.servicio = myService
        cx = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_nuevo_camarero)

        val nombre = findViewById<TextView>(R.id.txt_add_cam_nombre)
                val apellidos = findViewById<TextView>(R.id.txt_add_cam_apellido)
                val ok = findViewById<Button>(R.id.btn_add_cam_aceptar)
                val s = findViewById<Button>(R.id.btn_add_cam_salir)

                val customToast = CustomToast(cx!!) // Inicializamos CustomToast aquí

        s.setOnClickListener { cancel() }

        ok.setOnClickListener {
            try {
                val n = nombre.text.toString()
                val a = apellidos.text.toString()
                if (n.isEmpty() && a.isEmpty()) {
                    customToast.showBottom("Datos del camarero incorrectos", Toast.LENGTH_LONG)
                } else {
                    servicio!!.addCamNuevo(n, a)
                    val db = servicio!!.getDb("camareros") as DBCamareros
                    db.addCamNuevo(n, a)
                    customToast.showBottom("Camarero agregado con exito", Toast.LENGTH_LONG)
                    servicio!!.getExHandler("camareros")?.sendEmptyMessage(0)
                    cancel()
                }
            } catch (e: Exception) {
                Log.e("ERROR", e.toString())
            }
        }
    }
}