package com.valleapp.valletpvlib.ui.theme

import androidx.compose.ui.graphics.Color

fun hexToComposeColor(hex: String): Color {
    return Color(android.graphics.Color.parseColor(hex))
}
