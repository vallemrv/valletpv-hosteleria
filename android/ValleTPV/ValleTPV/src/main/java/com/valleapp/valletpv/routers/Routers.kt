package com.valleapp.valletpv.routers

sealed class Routers (val route: String) {
    object Arqueo: Routers("arqueo")
    object PaseCamareros : Routers("pasecamareros")
    object Preferencias : Routers("preferencias")

}