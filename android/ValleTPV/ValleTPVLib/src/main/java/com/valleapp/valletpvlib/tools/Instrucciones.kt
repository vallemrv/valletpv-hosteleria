package com.valleapp.valletpvlib.tools;

import android.content.ContentValues
import android.os.Handler

data class Instrucciones(
    val params: ContentValues,
    val url: String,
    val handler: Handler? = null,
    val op: String,
)