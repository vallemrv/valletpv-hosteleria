package com.valleapp.valletpv.adaptadoresDatos

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.valleapp.valletpv.interfaces.IControladorCuenta
import com.valleapp.valletpv.R
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class AdaptadorTicket(
    private val context: Context,
    private val values: ArrayList<JSONObject>,
    private val controlador: IControladorCuenta
) : ArrayAdapter<JSONObject>(context, R.layout.item_art, values), View.OnClickListener {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val rowView: View

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            rowView = inflater.inflate(R.layout.item_art, parent, false)

            viewHolder = ViewHolder(
                rowView.findViewById(R.id.lblCan),
                rowView.findViewById(R.id.lblNombre),
                rowView.findViewById(R.id.lblPrecio),
                rowView.findViewById(R.id.lblTotal),
                rowView.findViewById(R.id.btn_borrar)
            )

            rowView.tag = viewHolder
        } else {
            rowView = convertView
            viewHolder = rowView.tag as ViewHolder
        }

        val item = values[position]

        try {
            viewHolder.can.text = item.getString("Can")
            viewHolder.nombre.text = item.getString("descripcion_t")
            viewHolder.p.text = String.format(Locale.getDefault(),"%01.2f €", item.getDouble("Precio"))
            viewHolder.t.text = String.format(Locale.getDefault(),"%01.2f €", item.getDouble("Total"))

            viewHolder.rm.tag = item
            viewHolder.rm.setOnClickListener(this)
        } catch (e: JSONException) {
            Log.e("AdaptadorTicket", e.message ?: "Error al procesar JSON")
        }

        return rowView
    }

    override fun onClick(view: View) {
        try {
            val art = view.tag as JSONObject
            val estado = art.getString("Estado")
            if (estado == "N") controlador.borrarArticulo(art)
            else controlador.clickMostrarBorrar(art)
        } catch (e: JSONException) {
            Log.e("AdaptadorTicket", e.message ?: "Error en onClick")
        }
    }

    // ViewHolder para mejorar el rendimiento
    private class ViewHolder(
        val can: TextView,
        val nombre: TextView,
        val p: TextView,
        val t: TextView,
        val rm: ImageButton
    )
}
