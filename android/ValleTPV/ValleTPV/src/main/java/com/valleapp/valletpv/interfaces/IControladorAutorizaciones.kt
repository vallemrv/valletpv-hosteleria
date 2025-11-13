package com.valleapp.valletpv.interfaces

import android.content.ContentValues

interface IControladorAutorizaciones {
    fun pedirAutorizacion(params: ContentValues)
    fun pedirAutorizacion(id: String)
}
