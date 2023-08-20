package com.valleapp.valletpv.routers

sealed class Routers (val route: String) {
    object PaseCamareros : Routers("pasecamareros")
    object Preferencias : Routers("preferencias")

}