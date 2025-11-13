package com.valleapp.valletpv.adaptadoresDatos

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.valleapp.valletpv.R
import org.json.JSONObject

class AdaptadorSepararTicket(
    context: Context,
    private val values: List<JSONObject>,
    private val separados: Boolean
) : ArrayAdapter<JSONObject>(context, R.layout.item_separado, values) {

    private val TAG = "AdaptadorSepararTicket"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val view: View

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.item_separado, parent, false)
            viewHolder = ViewHolder(
                view.findViewById(R.id.lblCan),
                view.findViewById(R.id.lblNombre)
            )
            view.tag = viewHolder // Set ViewHolder only once here

        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        try {
            if (position >= values.size) {
                return view
            }

            val art = values[position]
            val can = art.optInt("Can", 0)
            val canCobro = art.optInt("CanCobro", 0)
            val cantidadFinal = if (separados) canCobro else can - canCobro

            viewHolder.lblCan.text = cantidadFinal.toString()
            viewHolder.nombre.text = art.optString("Descripcion", "")

        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener los datos del artículo en position $position: ${e.message}", e)
        }

        return view
    }

    private class ViewHolder(
        val lblCan: TextView,
        val nombre: TextView
    )
}