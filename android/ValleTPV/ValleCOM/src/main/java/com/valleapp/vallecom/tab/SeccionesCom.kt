package com.valleapp.vallecom.tab


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.valleapp.vallecom.interfaces.ITeclados
import com.valleapp.valletpv.R
import org.json.JSONArray
import org.json.JSONException
import com.valleapp.valletpvlib.tools.getDrawable


class SeccionesCom(
    private val botonera: ITeclados,
    private val secciones: JSONArray
) : Fragment() {

    private val seccionesCom = arrayOf(
        R.id.seccion_uno, R.id.seccion_dos, R.id.seccion_tres,
        R.id.seccion_cuatro, R.id.seccion_cinco, R.id.seccion_seis
    )

    internal lateinit var panel: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        botonera.rellenarBotonera()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.teclados, container, false)
        panel = view.findViewById(R.id.pneArt)
        for (i in seccionesCom.indices) {
            try {
                

                // Tu código original, modificado:
                val sec = secciones.getJSONObject(i)
                val button = view.findViewById<ImageButton>(seccionesCom[i])
                val drawableName = sec.getString("icono") // Por ejemplo, "icono_mesa"
                button.setImageDrawable(getDrawable(requireContext(), drawableName))
                button.tag = sec.getString("nombre")
                button.setOnLongClickListener {
                    botonera.asociarBotonera(it)
                    true
                }


            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return view
    }

    fun getPanel(): View {
        return panel
    }
}
