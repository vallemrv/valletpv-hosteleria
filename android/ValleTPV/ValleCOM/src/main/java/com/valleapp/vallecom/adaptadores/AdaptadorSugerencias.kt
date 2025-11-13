package com.valleapp.vallecom.adaptadores

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.valleapp.valletpv.R
import com.valleapp.vallecom.activitys.Sugerencias
import org.json.JSONException
import org.json.JSONObject
import androidx.core.graphics.toColorInt

class AdaptadorSugerencias(
    private val context: Context,
    private val values: List<JSONObject>
) : BaseAdapter() {

    override fun getCount(): Int {
        return values.size
    }

    override fun getItem(position: Int): JSONObject {
        return values[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_sugerencia, parent, false)
        val tituloTextView: TextView = view.findViewById(R.id.txtTitulo)
        val precioTextView: TextView = view.findViewById(R.id.txtPrecio)
        val itemLayout: LinearLayout = view.findViewById(R.id.item)

        try {
            val item = getItem(position)
            val sugerencia = item.getString("sugerencia")
            val incremento = item.getDouble("incremento")

            tituloTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25f)
            tituloTextView.text = sugerencia

            // Mostrar precio si incremento > 0
            if (incremento > 0) {
                precioTextView.visibility = View.VISIBLE
                "+%.2f€".format(incremento).also { precioTextView.text = it }
                precioTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25f)
                // Cambiar color cuando hay incremento
                itemLayout.setBackgroundColor("#E8F5E8".toColorInt())
                tituloTextView.setTextColor("#2E7D32".toColorInt())
            } else {
                precioTextView.visibility = View.GONE
                itemLayout.setBackgroundColor(Color.TRANSPARENT)
                tituloTextView.setTextColor(Color.BLACK)
            }

            itemLayout.tag = item

            // Agregar onClick listener
            itemLayout.setOnClickListener {
                if (context is Sugerencias) {
                    context.onClickItemSimple(it)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return view
    }
}
