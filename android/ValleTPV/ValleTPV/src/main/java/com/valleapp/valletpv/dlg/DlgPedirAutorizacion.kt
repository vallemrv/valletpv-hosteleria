package com.valleapp.valletpv.dlg

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ListView
import com.valleapp.valletpv.R
import com.valleapp.valletpv.adaptadoresDatos.AdaptadorCamNotificaciones
import com.valleapp.valletpv.interfaces.IAutoFinish
import com.valleapp.valletpvlib.interfaces.IBaseDatos
import com.valleapp.valletpvlib.db.DBCamareros
import com.valleapp.valletpv.interfaces.IControladorAutorizaciones
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask

class DlgPedirAutorizacion(
    context: Context,
    private val controladorAutofinish: IAutoFinish,
    dbCamareros: IBaseDatos,
    private val controladorAutorizaciones: IControladorAutorizaciones,
    private val params: JSONObject,
    private val accion: String
) : Dialog(context), IControladorAutorizaciones {

    private val dbCamareros: DBCamareros = dbCamareros as DBCamareros

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_camareos_para_notificaciones)
        setTitle("Confirmar autorizacion")
        val l: ListView = findViewById(R.id.lista_camareros_notificables)
        val ad = AdaptadorCamNotificaciones(
            context,
            R.layout.item_camarero_notificable,
            dbCamareros.getConPermiso(accion),
            this
        )
        l.adapter = ad
        val btn: ImageButton = findViewById(R.id.btn_salir_notificaciones_camareros)
        btn.setOnClickListener { cancel() }
    }

    override fun onStop() {
        super.onStop()
        controladorAutofinish.setEstadoAutoFinish(true, false)
    }

    override fun onStart() {
        super.onStart()
        val t = Timer()
        t.schedule(object : TimerTask() {
            override fun run() {
                controladorAutofinish.setEstadoAutoFinish(true, true)
            }
        }, 1000)
    }

    override fun pedirAutorizacion(id: String) {
        try {
            val p = ContentValues()
            p.put("idautorizado", id)
            p.put("instrucciones", params.toString())
            p.put("accion", accion)
            controladorAutorizaciones.pedirAutorizacion(p)
            cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun pedirAutorizacion(params: ContentValues) {
        Log.e("AUTORIZACION", params.toString())
    }
}
