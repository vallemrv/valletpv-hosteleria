package com.valleapp.valletpvlib.tools

data class Ticket(
    var id: Long = 0,
    var fecha: String = "",
    var hora: String = "",
    var entrega: Double = 0.0,
    var total: Double = 0.0,
    var nomMesa: String = "",
    var camarero: String = "",
)

data class LineasTicket(
    var teclaId: Long = 0,
    var descripcion: String = "",
    var cantidad: Int = 0,
    var precio: Double = 0.0,
    var total: Double = 0.0,
)