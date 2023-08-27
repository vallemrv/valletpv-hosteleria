package com.valleapp.valletpvlib.db

data class LineaCuenta(
    var cantidad: Int = 0,
    var descripcion: String = "",
    var descripcion_r: String = "",
    var precio: Double = 0.0,
    var total: Double = 0.0,
) : BaseEntity() {

    override fun toString(): String {
        return "$cantidad x $descripcion"
    }

}