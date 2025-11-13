package com.valleapp.valletpv.adaptadoresDatos

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.valleapp.valletpv.R
import com.valleapp.valletpv.interfaces.IControlMensajes
import org.json.JSONObject

class AdaptadorCamMensajes(
    context: Context,
    private val objects: List<JSONObject>,
    private val controlador: IControlMensajes
) : ArrayAdapter<JSONObject>(context, R.layout.lista_camareos_para_notificaciones, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val view: View

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.item_camarero_notificable, parent, false)

            viewHolder = ViewHolder(
                view.findViewById(R.id.txt_nombre_camarero_notificaciones),
                view.findViewById(R.id.btn_send_cam_autorizado)
            )

            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        try {
            val obj = objects[position]
            val nombre = obj.optString("nombre", "").trim()
            val apellidos = obj.optString("apellidos", "").trim()

            // Si nombre o apellidos están vacíos, se muestra "Para todos"
            viewHolder.nombre.text = if (nombre.isNotEmpty()) {
                "$nombre $apellidos"
            } else {
                "Nombre vacio"
            }
            viewHolder.boton.tag = obj.getString("ID")
            viewHolder.boton.setOnClickListener {
                controlador.sendMensaje(it.tag.toString(), obj.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return view
    }

    private class ViewHolder(
        val nombre: TextView,
        val boton: ImageView
    )
}
