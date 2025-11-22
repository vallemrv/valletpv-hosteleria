package com.valleapp.vallecash.ui.fondocaja

import android.content.ContentValues
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import org.json.JSONObject
import java.util.Locale


class FondoCajaViewModel : ViewModel() {

    val fondoDeCaja = MutableLiveData(0.0)
    val totalAlmacenes = MutableLiveData(0.0)
    val totalRecicladores = MutableLiveData(0.0)
    val cambioReal = MutableLiveData(0.0)
    val totalAdmitido = MutableLiveData(0.0)
    val totalUltimoStacker = MutableLiveData(0.0)
    val totalRetiradoStacker = MutableLiveData(0.0)
    private val ocupado = MutableLiveData(false)
    val message = MutableLiveData("")

    fun getTotales(server: String, uid: String) {
        val url = "$server/arqueo/getcambio"
        val handler = Handler(Looper.getMainLooper()) {
            val res = it.data.getString("RESPONSE")
            if (res != null) {
                val obj = JSONObject(res)
                // Recoge los datos del servidor
                fondoDeCaja.postValue(obj.getDouble("cambio"))
                totalUltimoStacker.postValue(obj.getDouble("stacke"))
                cambioReal.postValue(obj.getDouble("cambio_real"))
            }
            ocupado.postValue(false)
            true
        }
        ocupado.postValue(true)
        // Realizar la solicitud HTTP
        val p = ContentValues()
        p.put("uid", uid)
        HTTPRequest(url, p, "cambio", handler)
    }

    fun updateTotales(server: String, uid: String) {
        val ultimoStacker = totalUltimoStacker.value ?: 0.0
        val totalAlmacenesValor = totalAlmacenes.value ?: 0.0
        val cambioRealValor = cambioReal.value ?: 0.0
        val totalAdmitidoValor = totalAdmitido.value ?: 0.0
        val totalRetiradoStackerValor = totalRetiradoStacker.value ?: 0.0
        val nuevoStacker = ultimoStacker - totalRetiradoStackerValor
        val nuevoCambioReal = cambioRealValor + totalAdmitidoValor

        val p = ContentValues()
        p.put("cambio", String.format(Locale.UK, "%.2f", fondoDeCaja.value))
        p.put("stacke", String.format(Locale.UK, "%.2f", nuevoStacker))
        p.put("cambio_real", String.format(Locale.UK, "%.2f", nuevoCambioReal))
        p.put("uid", uid)

        val url = "$server/arqueo/setcambio"
        val handler = Handler(Looper.getMainLooper()) {
            ocupado.postValue(false)
            val nuevo = getTotalRecicladores()
            totalAdmitido.postValue(0.0)
            totalRetiradoStacker.postValue(0.0)
            totalUltimoStacker.postValue(nuevoStacker)
            totalAlmacenes.postValue(totalAlmacenesValor-totalRetiradoStackerValor)
            cambioReal.postValue(nuevoCambioReal)
            totalRecicladores.postValue(nuevo)
            message.postValue("Totales actualizados correctamente.")
            true
        }
        ocupado.postValue(true)
        HTTPRequest(url, p, "cambio", handler)
    }

    fun getTotalRecicladores(): Double {
        val recicladores = totalRecicladores.value ?: 0.0
        val admintido = totalAdmitido.value ?: 0.0
        return recicladores + admintido
    }
}