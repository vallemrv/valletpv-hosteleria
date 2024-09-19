package com.valleapp.vallecash.ui.dispensecoins

import android.content.ContentValues
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.valleapp.valletpvlib.comunicacion.HTTPRequest
import org.json.JSONObject
import java.util.Locale

class DenominationViewModel : ViewModel() {

    private val _denominations = MutableLiveData<List<Denomination>>()
    val denominations: LiveData<List<Denomination>> = _denominations

    private var _cambio = MutableLiveData<Double>(0.0)

    private var _camibo_real = MutableLiveData<Double>(0.0)

    private var _totalRecicladores = MutableLiveData<Double>(0.0)
    val totalRecicladores: LiveData<Double> = _totalRecicladores

    private var _totalAlmacenes = MutableLiveData<Double>(0.0)
    private var _esActualizado = MutableLiveData<Boolean>(false)

    private var _totalDispensado = MutableLiveData<Double>(0.0)
    val totalDispensado: LiveData<Double> = _totalDispensado

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> get() = _updateResult

    private val _ocupado = MutableLiveData<Boolean>()
    val ocupado: LiveData<Boolean> get() = _ocupado

    // LiveData para las cantidades, usando el nombre de la denominación como clave
    private val _amounts = MutableLiveData<MutableMap<String, Denomination>>() // Cambié a String

    init {
        _amounts.value = mutableMapOf()
    }

    fun getTotales(server: String) {
        val url = "$server/arqueos/getcambio"
        val handler = Handler(Looper.getMainLooper()) {
            val res = it.data.getString("RESPONSE")
            if (res != null) {
                val obj = JSONObject(res)
                // Recoge los datos del servidor
                _cambio = MutableLiveData<Double>(obj.getDouble("cambio"))
                _totalAlmacenes = MutableLiveData<Double>( obj.getDouble("stacke"))
                _camibo_real = MutableLiveData<Double>(obj.getDouble("cambio_real"))
                _esActualizado.postValue(true)
            }
            _ocupado.postValue(false)
            true // Esto indica que el mensaje ha sido procesado
        }
        _ocupado.postValue(true)
        // Realizar la solicitud HTTP
        HTTPRequest(url, ContentValues(), "cambio", handler)

    }

    fun updateTotales(server: String){

        val almacenes = _totalAlmacenes.value ?: 0.0
        val cambio_real = _camibo_real.value ?: 0.0
        val totalDispensado = _totalDispensado.value ?: 0.0
        val nuevo_cambio_real = cambio_real - totalDispensado

        val p = ContentValues()

        p.put("cambio",String.format(Locale.US,"%.2f", _cambio.value) )
        p.put("stacke", String.format(Locale.US,"%.2f", almacenes))
        p.put("cambio_real", String.format(Locale.US,"%.2f", nuevo_cambio_real))

        val url = "$server/arqueos/setcambio"
        val handler = Handler(Looper.getMainLooper()) {
            val res = it.data.getString("RESPONSE")
            if (res != null) {
                // Recoge los datos del servidor
                if ("success".equals(res)) {
                   _updateResult.postValue(true)
                } else {
                   println("Error al actualizar el efectivo en el servidor.")
                }
            }
            _ocupado.postValue(false)
            _totalDispensado.value = 0.0
            true // Esto indica que el mensaje ha sido procesado
        }
        _ocupado.postValue(true)
        // Realizar la solicitud HTTP
        HTTPRequest(url, p, "cambio", handler)
    }



    // Método para actualizar las cantidades
    fun updateAmount(denomination: Denomination, amount: Int) {
        val currentAmounts = _amounts.value ?: mutableMapOf() // Obtener el mapa actual o crear uno nuevo
        if (amount == 0) {
            currentAmounts.remove(denomination.name) // Si la cantidad es 0, eliminar la denominación
        }else {
            currentAmounts[denomination.name] = denomination

        }

        var total = 0.0 // Variable para acumular el total
        currentAmounts.entries.forEach { (key, value) ->
            val aux = key.toDoubleOrNull() // Convierte la clave a Double
            if (aux != null) {
                total += aux * value.dispenseAmount
            } else {
                Log.e("Error", "No se pudo convertir $key a Double")
            }
        }

        // Asignar el total calculado a _totalDispensado
        _totalDispensado.value = total
        _amounts.value = currentAmounts // Actualizar el LiveData
    }

    // Método para procesar el JSON
    fun processResponseY(jsonResponse: String) {
         if (jsonResponse.startsWith("#ER")) {
            return
         }
        val denominationsList = mutableListOf<Denomination>()
        var total = 0.0
        _totalDispensado.value = 0.0

        // Eliminar los caracteres no necesarios del JSON
        val cleanedResponse = jsonResponse.substringAfter("#WR:LEVEL#").substringBefore("#")

        // Partes del JSON separadas por ";"
        val parts = cleanedResponse.split(";")

        // Monedas y billetes
        val monedas = parts[0] // Monedas en formato "2:9,5:57,10:57,..."
        val billetes = parts[1] // Billetes en formato "500:18,1000:33,2000:4,..."

        // Procesar billetes primero y ordenarlos de mayor a menor
        val sortedBilletes = billetes.split(",").map {
            val (denomination, quantity) = it.split(":")
            val valueInCents = denomination.toInt()
            val quantityValue = quantity.toInt()
            Pair(valueInCents, quantityValue)
        }.sortedByDescending { it.first } // Ordenar de mayor a menor

        sortedBilletes.forEach { (valueInCents, quantityValue) ->
            // Solo incluir billetes de 500, 1000, 2000 sin decimales
            if (valueInCents == 500 || valueInCents == 1000 || valueInCents == 2000) {
                val denominationDescription = "${valueInCents / 100}"
                total += valueInCents * quantityValue
                denominationsList.add(Denomination(denominationDescription, quantityValue, 0))
            }
        }

        // Luego procesar las monedas y ordenarlas de mayor a menor
        val sortedMonedas = monedas.split(",").map {
            val (denomination, quantity) = it.split(":")
            val valueInCents = denomination.toInt()
            val quantityValue = quantity.toInt()
            Pair(valueInCents, quantityValue)
        }.sortedByDescending { it.first } // Ordenar de mayor a menor por el valor de la denominación

        sortedMonedas.forEach { (valueInCents, quantityValue) ->
            // Quitar los decimales para monedas de 1 €, 2 €, y 20 €
            val denominationDescription = when (valueInCents) {
                100, 200 -> "${valueInCents / 100}" // 1€, 2€,  sin decimales
                else -> String.format(Locale.US, "%.2f", valueInCents / 100.0) // Otras monedas con dos decimales
            }
            total += valueInCents * quantityValue
            // Añadir a la lista de denominaciones
            denominationsList.add(Denomination(denominationDescription, quantityValue, 0))
        }

        // Actualizar las denominaciones con la lista procesada
        _denominations.value = denominationsList
        _totalRecicladores.value = total / 100
    }


    // Método para generar el comando de dispensación según especificaciones dadas
    fun generateDispenseCommand(stackBilletes: Int, pantallaFrente: Int, verPantallaPago: Int): String {
        val amounts = _amounts.value ?: return ""  // Retorna cadena vacía si no hay cantidades seleccionadas

        val stringBuilder = StringBuilder("#U#")
        val monedasString = StringBuilder("")
        val billetesString = StringBuilder("")

        var hasBilletes = false  // Para verificar si hay al menos una cantidad mayor que cero
        var hasMonedas = false

        amounts.entries.forEach { (key, value) ->

            if (value.dispenseAmount > 0) {
                val cents = (key.toDouble() * 100).toInt()  // Convertir a céntimos
                if (cents < 500) {  // Monedas de menos de 200 céntimos
                    // Añadimos nombre de la denomincion y cantidad separada por dos puntos.
                    if (hasMonedas) {
                        monedasString.append(",")
                    }
                    monedasString.append("$cents:${value.dispenseAmount}")
                    hasMonedas = true

                } else {
                    // Añadir las denominaciones de billetes con sus cantidades
                    if (hasBilletes) {
                        billetesString.append(",")
                    }
                    billetesString.append("$cents:${value.dispenseAmount}")
                    hasBilletes = true
                }
            }
            value.dispenseAmount = 0  // Resetear la cantidad despachada

        }

        if (!hasMonedas && !hasBilletes) {
            return ""  // Retorna cadena vacía si no hay cantidades a dispensar
        }

        // Añadir los parámetros finales para el comando correctamente formateados
        stringBuilder.append("$monedasString;$billetesString")
        stringBuilder.append("#$stackBilletes#$pantallaFrente#$verPantallaPago#")

        return stringBuilder.toString()
    }

    fun setTotalDispensao(total: Double) {
       _totalDispensado.value = total
    }

    fun setOcupado(ocupado: Boolean) {
        _ocupado.value = ocupado
    }

}
