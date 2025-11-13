package com.valleapp.vallecom.adaptadores

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.valletpv.R
import org.json.JSONException
import org.json.JSONObject

class AdaptadorTicket(
    private val context: Context,
    private val values: List<JSONObject>
) : RecyclerView.Adapter<AdaptadorTicket.TicketViewHolder>() {

    inner class TicketViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cantidadTextView: TextView = view.findViewById(R.id.lblCan)
        val descripcionTextView: TextView = view.findViewById(R.id.lblDescripcion)
        val precioTextView: TextView = view.findViewById(R.id.lblPrecio)
        val totalTextView: TextView = view.findViewById(R.id.lblTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.linea_art, parent, false)
        return TicketViewHolder(view)
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        try {
            val item = values[position]
            holder.cantidadTextView.text = item.getString("Can")
            holder.descripcionTextView.text = item.getString("descripcion_t")
            holder.precioTextView.text = String.format("%.2f €", item.getDouble("Precio"))
            holder.totalTextView.text = String.format("%.2f €", item.getDouble("Total"))
        } catch (e: JSONException) {
            Log.e("AdaptadorTicket", "Error al enlazar datos: ${e.message}", e)
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

            @SuppressLint("DefaultLocale")
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.linea_art, parent, false)
                val cantidadTextView: TextView = view.findViewById(R.id.lblCan)
                val descripcionTextView: TextView = view.findViewById(R.id.lblDescripcion)
                val precioTextView: TextView = view.findViewById(R.id.lblPrecio)
                val totalTextView: TextView = view.findViewById(R.id.lblTotal)

                try {
                    val item = getItem(position)
                    cantidadTextView.text = item.getString("Can")
                    descripcionTextView.text = item.getString("descripcion_t")
                    precioTextView.text = String.format("%.2f €", item.getDouble("Precio"))
                    totalTextView.text = String.format("%.2f €", item.getDouble("Total"))
                } catch (e: JSONException) {
                    Log.e("AdaptadorTicket", "Error al enlazar datos: ${e.message}", e)
                }

                return view
            }
        }
    }
}
