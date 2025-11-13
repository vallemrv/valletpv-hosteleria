package com.valleapp.valletpvlib.tools

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.round(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return (this * factor).roundToInt() / factor
}

fun  getDrawable( context: Context, drawableName: String): Drawable? {
    // Define este mapa una única vez en tu clase o como un objeto compañero (companion object)
    // Si los nombres vienen de una API, deberías tener un control sobre esos nombres para que coincidan con tus drawables.
    val iconDrawableMap = mapOf(
        "bar" to com.valleapp.valletpvlib.R.drawable.ic_bar,
        "bocadillo" to com.valleapp.valletpvlib.R.drawable.ic_bocadillo,
        "carne" to com.valleapp.valletpvlib.R.drawable.ic_carne,
        "cocktel" to com.valleapp.valletpvlib.R.drawable.ic_cocktel,
        "copa_con_limon" to com.valleapp.valletpvlib.R.drawable.ic_copa_con_limon,
        "copa_vino" to com.valleapp.valletpvlib.R.drawable.ic_copa_vino,
        "cubalibre" to com.valleapp.valletpvlib.R.drawable.ic_cubalibre,
        "donut" to com.valleapp.valletpvlib.R.drawable.ic_donut,
        "jarra_cerveza" to com.valleapp.valletpvlib.R.drawable.ic_jarra_cerveza,
        "llevar" to com.valleapp.valletpvlib.R.drawable.ic_llevar, // Agregado
        "magdalena" to com.valleapp.valletpvlib.R.drawable.ic_magdalena,
        "menu" to com.valleapp.valletpvlib.R.drawable.ic_menu, // Agregado
        "pescado" to com.valleapp.valletpvlib.R.drawable.ic_pescado,
        "pincho" to com.valleapp.valletpvlib.R.drawable.ic_pincho,
        "pizza" to com.valleapp.valletpvlib.R.drawable.ic_pizza, // Agregado
        "plato" to com.valleapp.valletpvlib.R.drawable.ic_plato,
        "plato_combinado" to com.valleapp.valletpvlib.R.drawable.ic_plato_combinado,
        "sopa" to com.valleapp.valletpvlib.R.drawable.ic_sopa, // Agregado
        "sopa_cuchara" to com.valleapp.valletpvlib.R.drawable.ic_sopa_cuchara, // Agregado
        "tapas" to com.valleapp.valletpvlib.R.drawable.ic_tapas, // Incluido si es necesario
        "tarta" to com.valleapp.valletpvlib.R.drawable.ic_tarta,
        "taza_cafe" to com.valleapp.valletpvlib.R.drawable.ic_taza_cafe,
        "aperitivo" to com.valleapp.valletpvlib.R.drawable.ic_aperitivo
    )

    // Buscar el ID del drawable en nuestro mapa
    val drawableResId = iconDrawableMap[drawableName]


    if (drawableResId != null) { // Si encontramos el ID en el mapa
        return ResourcesCompat.getDrawable(context.resources, drawableResId, null)
    } else {
        // Si el nombre no está en el mapa, usamos un icono por defecto o manejamos el error
        Log.e("SeccionesCom", "Drawable no mapeado o no encontrado para: $drawableName. Usando icono por defecto.")
        return ResourcesCompat.getDrawable(context.resources, com.valleapp.valletpvlib.R.drawable.ic_plato, null)
    }

}