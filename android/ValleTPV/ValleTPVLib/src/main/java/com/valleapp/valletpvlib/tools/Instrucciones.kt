package com.valleapp.valletpvlib.tools;

data class Mensaje(
    var mensaje: String,
    var tipo: String,
    val data: Map<String, String>? = null,
)

data class Instrucciones(
    val endPoint: String,
    val mensaje: Mensaje? = null,
    val params: Map<String, String>? = null,
)