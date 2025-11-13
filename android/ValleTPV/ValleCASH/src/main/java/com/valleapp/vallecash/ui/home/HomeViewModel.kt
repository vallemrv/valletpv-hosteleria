package com.valleapp.vallecash.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    // MutableLiveData para el total dispensado (solo modificable desde el ViewModel)
    private val _totalDispensado = MutableLiveData<Double>()

    // LiveData para observar el total dispensado desde la vista
    val totalRecicladores: LiveData<Double> get() = _totalDispensado

    // MutableLiveData para el total del almacén (solo modificable desde el ViewModel)
    private val _totalAlmacen = MutableLiveData<Double>()

    // LiveData para observar el total del almacén desde la vista
    val totalAlmacen: LiveData<Double> get() = _totalAlmacen

    // Función para actualizar el total dispensado
    fun setTotalRecicladores(value: Double) {
        _totalDispensado.value = value
    }

    // Función para actualizar el total del almacén
    fun setTotalAlmacen(value: Double) {
        _totalAlmacen.value = value
    }
}