package com.valleapp.vallecom.interfaces

import android.view.View

/**
 * Created by valle on 18/09/14.
 */
interface IComanda {
    fun cargarNota()
    fun agregarLinea(v: View)
    fun borrarLinea(v: View)
    fun getContexto(): android.content.Context
}
