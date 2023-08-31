package com.valleapp.valletpvlib.tools;


data class Instrucciones(
    val endPoint: String,
    val params: Map<String, String>? = null,
)