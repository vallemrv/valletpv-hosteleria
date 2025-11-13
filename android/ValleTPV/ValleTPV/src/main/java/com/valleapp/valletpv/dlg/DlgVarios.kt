package com.valleapp.valletpv.dlg

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.valleapp.valletpv.R
import com.valleapp.valletpv.interfaces.IControladorCuenta
import org.json.JSONException
import org.json.JSONObject

class DlgVarios(
    context: Context,
    private val controlador: IControladorCuenta
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.varios)
        this.setTitle("Varios ")

        val can: TextView = this.findViewById(R.id.txt_varios_can)
        val p: TextView = this.findViewById(R.id.txt_varios_precio)
        val nom: TextView = this.findViewById(R.id.txt_varios_nombre)
        val ok: ImageButton = this.findViewById(R.id.btn_guardar_preferencias)
        val s: ImageButton = this.findViewById(R.id.btn_varios_salir)

        can.text = ""
        p.text = ""
        nom.text = ""

        s.setOnClickListener { cancel() }

        ok.setOnClickListener {
            if (p.text.isNotEmpty()) {
                try {
                    val strCan = if (can.text.toString().isEmpty()) "1" else can.text.toString()
                    val art = JSONObject()
                    val nombre = if (nom.text.toString().isNotEmpty()) nom.text.toString() else "Varios"
                    art.put("ID", "0")
                    art.put("Precio", p.text.toString().replace(",", "."))
                    art.put("Can", strCan)
                    art.put("Descripcion", nombre)
                    art.put("descripcion_t", nombre)
                    controlador.pedirArt(art)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                cancel()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        controlador.setEstadoAutoFinish(true, false)
    }
}
