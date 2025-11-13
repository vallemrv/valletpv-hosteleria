package com.valleapp.valletpv.dlg

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import com.valleapp.valletpv.R
import com.valleapp.valletpv.adaptadoresDatos.AdaptadorCamMensajes
import com.valleapp.valletpv.interfaces.IControlMensajes
import org.json.JSONObject

class DlgMensajes(
    context: Context,
    private val controlador: IControlMensajes
) : Dialog(context), IControlMensajes {

    private val cx: Context = context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_mensaje)
        val btn: ImageButton = findViewById(R.id.btn_salir_mensajes)
        btn.setOnClickListener { cancel() }
        setTitle("Enviar mensajes")
    }

    override fun sendMensaje(IDRecptor: String, mensaje: String) {
        val t: TextView = findViewById(R.id.txt_mensaje)
        if (t.text.toString().isNotEmpty()) {
            controlador.sendMensaje(IDRecptor, t.text.toString())
            cancel()
        }
    }

    fun mostrarReceptores(lista: List<JSONObject>) {
        val l: ListView = findViewById(R.id.lista_camareros_notificables)
        l.adapter = AdaptadorCamMensajes(cx, lista, this)
    }
}
