package com.valleapp.valletpv.dlg

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.widget.SwitchCompat
import android.widget.ImageButton
import org.json.JSONObject
import com.valleapp.valletpv.R
import android.content.ContentValues
import com.valleapp.valletpvlib.comunicacion.HTTPRequest

class DlgEditExistencias(
    context: Context,
    private val server: String,
    private val obj: JSONObject
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_existencias)

        // Configurar el texto y estado del switch
        val switch = findViewById<SwitchCompat>(R.id.linea_settings)
        switch.text = obj.getString("Nombre")
        switch.isChecked = obj.getInt("hay_existencias") == 1


        // Configurar el botón guardar
        val btnGuardar = findViewById<ImageButton>(R.id.btn_guardar)
        btnGuardar.setOnClickListener {
            val idTecla = obj.getInt("ID")
            val hayExistencias = if (switch.isChecked) 1 else 0
            val endpoint = if (hayExistencias == 1) "borrar" else "agregar"

            val params = ContentValues().apply {
                put("IDTecla", idTecla)
            }

            HTTPRequest("$server/articulos/$endpoint", params, "", null)
            dismiss()
        }
    }
}