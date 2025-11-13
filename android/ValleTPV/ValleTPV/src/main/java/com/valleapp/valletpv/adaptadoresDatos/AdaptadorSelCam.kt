package com.valleapp.valletpv.adaptadoresDatos

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.valleapp.valletpv.R
import org.json.JSONObject

class AdaptadorSelCam(
    context: Context,
    private val values: List<JSONObject>
) : ArrayAdapter<JSONObject>(context, R.layout.item_simple, values) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_simple, parent, false)

        try {
            val obj = values[position]
            val nombre = rowView.findViewById<TextView>(R.id.texto_linea)
            nombre.text = "${obj.getString("nombre")} ${obj.getString("apellidos")}"
            rowView.tag = obj // Guardamos el JSONObject directamente en el tag
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return rowView
    }
}
