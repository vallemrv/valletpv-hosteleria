package com.valleapp.valletpvlib.ui.theme
import androidx.compose.ui.graphics.Color

//paleta de colores


object ColorTheme {
    val Background = Color(0xFFCDC4DA)
    val Primary = Color(0xFFD0B8D5)
    val BotonesAccion = Color(0xFFEBD0EC)
    val TextTitulos = Color.Black
    val BgListas = Color(0xFFEBD0EC)
    val Secundary = Color(0xFFC5CAE9)
    fun hexToComposeColor(hex: String?): Color {
        if (hex == null) return Primary
        else  return Color(android.graphics.Color.parseColor(hex))
    }
}

