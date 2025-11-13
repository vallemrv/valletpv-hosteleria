package com.valleapp.valletpvlib.tools

import android.content.ContentValues
import android.os.Handler
import java.util.UUID

class Instruccion(
    val params: ContentValues,
    val url: String,
    val handler: Handler? = null, // Handler es opcional
    val op: String = ""
) {
    init {
        params.put("uid_device", UUID.randomUUID().toString())
    }

}
