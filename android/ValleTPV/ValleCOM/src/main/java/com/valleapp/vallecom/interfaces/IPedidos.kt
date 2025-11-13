package com.valleapp.vallecom.interfaces

import android.view.View
import org.json.JSONObject


/**
 * Created by valle on 29/10/14.
 */
interface IPedidos {
    fun pedir(v: View)
    fun rellenarReceptores()
    fun rellenarZonas()
}
