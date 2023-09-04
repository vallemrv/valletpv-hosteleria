package com.valleapp.valletpv.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ModelCobros : ViewModel() {
    var total: Double by mutableDoubleStateOf(0.0)
    var entregado: Double by mutableDoubleStateOf(0.0)
    var cambio: Double by mutableDoubleStateOf(0.0)
    var showMostrarInfo by mutableStateOf(false)

    fun mostrarInfo(total: Double, entregado: Double, cambio: Double) {
        showMostrarInfo = true
        this.entregado = entregado
        this.cambio = cambio
        this.total = total
    }
}
