package com.valleapp.valletpvlib.interfaces

import android.content.ContentValues

interface IControladorAutorizaciones {
    fun pedirAutorizacion(params: ContentValues?)
    fun pedirAutorizacion(id: String?)
}