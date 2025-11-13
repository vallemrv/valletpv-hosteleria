package com.valleapp.vallecom.adaptadores

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.valletpv.R

import org.json.JSONObject

class AdaptadorMensajes(
    private val context: Context,
    private val values: List<JSONObject>
) : RecyclerView.Adapter<AdaptadorMensajes.MensajesViewHolder>() {

    inner class MensajesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tituloTextView: TextView = view.findViewById(R.id.txtTitulo)
        val itemLayout: RelativeLayout = view.findViewById(R.id.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajesViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.linea_simple, parent, false)
        return MensajesViewHolder(view)
    }

    override fun onBindViewHolder(holder: MensajesViewHolder, position: Int) {
        try {
            val item = values[position]
            holder.tituloTextView.text = item.getString("nombre")
            holder.itemLayout.tag = item.getString("ID")
        } catch (e: Exception) {
            Log.e("AdaptadorMensajes", "Error al enlazar datos: ${e.message}", e)
        }
    }

    override fun getItemCount(): Int {
        return values.size
    }

    fun toListAdapter(): android.widget.BaseAdapter {
        return object : android.widget.BaseAdapter() {
            override fun getCount(): Int = values.size

            override fun getItem(position: Int): JSONObject = values[position]

            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.linea_simple, parent, false)
                val tituloTextView: TextView = view.findViewById(R.id.txtTitulo)
                val itemLayout: RelativeLayout = view.findViewById(R.id.item)

                try {
                    val item = getItem(position)
                    tituloTextView.text = item.getString("nombre")
                    itemLayout.tag = item.getString("ID")
                } catch (e: Exception) {
                    Log.e("AdaptadorMensajes", "Error al enlazar datos: ${e.message}", e)
                }

                return view
            }
        }
    }
}
