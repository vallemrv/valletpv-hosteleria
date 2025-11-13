package com.valleapp.vallecom.adaptadores

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.valletpv.R
import org.json.JSONObject

class AdaptadorRefill(
    private val context: Context,
    private val values: List<JSONObject>
) : RecyclerView.Adapter<AdaptadorRefill.RefillViewHolder>() {

    inner class RefillViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tituloTextView: TextView = view.findViewById(R.id.txtTitulo)
        val subtituloTextView: TextView = view.findViewById(R.id.labelSubTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RefillViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.linea_title_subtiltle, parent, false)
        return RefillViewHolder(view)
    }

    override fun onBindViewHolder(holder: RefillViewHolder, position: Int) {
        try {
            val item = values[position]
            holder.tituloTextView.text = buildString {
                append("Pedido ")
                append(position + 1)
            }
            holder.subtituloTextView.text = item.getString("subtitle")
        } catch (e: Exception) {
            Log.e("AdaptadorRefill", "Error al enlazar datos: ${e.message}", e)
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
                val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.linea_title_subtiltle, parent, false)
                val tituloTextView: TextView = view.findViewById(R.id.txtTitulo)
                val subtituloTextView: TextView = view.findViewById(R.id.labelSubTitle)

                try {
                    val item = getItem(position)
                    tituloTextView.text = buildString {
                        append("Pedido ")
                        append(position + 1)
                    }
                    subtituloTextView.text = item.getString("subtitle")
                } catch (e: Exception) {
                    Log.e("AdaptadorRefill", "Error al enlazar datos: ${e.message}", e)
                }

                return view
            }
        }
    }
}
