package com.valleapp.valletpvlib.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object Styles {
    val TextListas = TextStyle(
        fontSize = 30.sp,
        letterSpacing = 0.sp,

    )
    val TextTitulos = TextStyle(
        fontSize = 40.sp,
        letterSpacing = 0.sp,
        color = ColorTheme.TextTitulos,
        fontWeight = FontWeight.Bold
    )
    val TextSubTitulos = TextStyle(
        fontSize = 32.sp,
        letterSpacing = 0.sp,
        color = ColorTheme.TextTitulos,
        fontWeight = FontWeight.Bold
    )
    val TextBotones = TextStyle(
        fontSize = 40.sp,
        letterSpacing = 0.sp
    )
}
