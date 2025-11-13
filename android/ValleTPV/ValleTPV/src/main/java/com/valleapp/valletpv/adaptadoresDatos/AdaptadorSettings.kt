package com.valleapp.valletpv.adaptadoresDatos

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SwitchCompat
import com.valleapp.valletpv.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class AdaptadorSettings(
    private val cx: Context,
    private val values: List<JSONObject>
) : ArrayAdapter<JSONObject>(cx, R.layout.item_settings, values) {

    private val lista = JSONArray()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            val inflater = LayoutInflater.from(cx)
            rowView = inflater.inflate(R.layout.item_settings, parent, false)
            viewHolder = ViewHolder(rowView.findViewById(R.id.linea_settings))
            rowView.tag = viewHolder
        } else {
            rowView = convertView
            viewHolder = rowView.tag as ViewHolder
        }

        val art = values[position]

        // Verificar si el objeto ya está en `lista` antes de añadirlo
        if (!jsonArrayContains(lista, art)) {
            lista.put(art)
        }

        // 🔹 Desactivar temporalmente el listener para evitar llamadas innecesarias
        viewHolder.linea.setOnCheckedChangeListener(null)

        // 🔹 Actualizar el estado del SwitchCompat
        viewHolder.linea.isChecked = art.optBoolean("Activo", false)
        viewHolder.linea.text = art.optString("Nombre", "Sin Nombre")
        viewHolder.linea.tag = art

        // 🔹 Volver a activar el listener solo después de haber actualizado los valores
        viewHolder.linea.setOnCheckedChangeListener { buttonView, isChecked ->
            try {
                val obj = buttonView.tag as? JSONObject
                obj?.put("Activo", isChecked)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        return rowView
    }

    fun getLista(): JSONArray {
        return lista
    }

    private fun jsonArrayContains(array: JSONArray, obj: JSONObject): Boolean {
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            if (item.optInt("ID") == obj.optInt("ID")) { // Comparando por ID
                return true
            }
        }
        return false
    }

    private class ViewHolder(val linea: SwitchCompat)
}
