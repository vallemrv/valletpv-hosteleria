package com.valleapp.valletpvlib.routers

sealed class RoutersBase (val route: String) {
    object Camareros: RoutersBase("camareros")
    object Mesas: RoutersBase("mesas/{camId}")
    object Cuenta: RoutersBase("cuenta/{camId}/{mesaId}")
}