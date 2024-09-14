package com.valleapp.vallecash.ui.changecambio

import android.content.ContentValues
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import org.json.JSONObject
import java.util.Locale

class ChangeCambioViewModel : ViewModel() {

    val cambio = MutableLiveData<Double>(0.0)
    val totalAlmacenes = MutableLiveData<Double>(0.0)
    val totalAlmacenesAnterior = MutableLiveData<Double>(0.0)
    val cambioReal = MutableLiveData<Double>(0.0)
    val totalAdmitido = MutableLiveData<Double>(0.0)
    val esActualizado = MutableLiveData<Boolean>(false)
    val updateResult = MutableLiveData<Boolean>(false)
    val ocupado = MutableLiveData<Boolean>(false)

    fun getTotales(server: String) {
        val url = "$server/arqueos/getcambio"
        val handler = Handler(Looper.getMainLooper()) {
            val res = it.data.getString("RESPONSE")
            if (res != null) {
                val obj = JSONObject(res)
                // Recoge los datos del servidor
                cambio.postValue(obj.getDouble("cambio"))
                totalAlmacenesAnterior.postValue(obj.getDouble("stacke"))
                cambioReal.postValue(obj.getDouble("cambio_real"))
                esActualizado.postValue(true)
            }
            ocupado.postValue(false)
            true
        }
        ocupado.postValue(true)
        // Realizar la solicitud HTTP
        HTTPRequest(url, ContentValues(), "cambio", handler)
    }

    fun updateTotales(server: String) {
        val almacenes = totalAlmacenes.value ?: 0.0
        val cambioRealValor = cambioReal.value ?: 0.0
        val totalAdmitidoValor = totalAdmitido.value ?: 0.0
        val nuevoCambioReal = cambioRealValor + totalAdmitidoValor

        val p = ContentValues()
        p.put("cambio", String.format(Locale.getDefault(), "%.2f", cambio.value))
        p.put("stacke", String.format(Locale.getDefault(), "%.2f", almacenes))
        p.put("cambio_real", String.format(Locale.getDefault(), "%.2f", nuevoCambioReal))

        val url = "$server/arqueos/setcambio"
        val handler = Handler(Looper.getMainLooper()) {
            val res = it.data.getString("RESPONSE")
            if (res != null) {
                if ("success" == res) {
                    updateResult.postValue(true)
                } else {
                    println("Error al actualizar el efectivo en el servidor.")
                }
            }
            ocupado.postValue(false)
            totalAdmitido.postValue(0.0)
            true
        }
        ocupado.postValue(true)
        // Realizar la solicitud HTTP
        HTTPRequest(url, p, "cambio", handler)
    }
}
