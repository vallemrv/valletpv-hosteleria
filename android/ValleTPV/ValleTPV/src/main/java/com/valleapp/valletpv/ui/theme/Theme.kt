package com.valleapp.valletpv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ValleTheme(
    content: @Composable () -> Unit
) {
    val colors = DarkColorPalette

    MaterialTheme(
        colorScheme = colors,
        typography = ValleTypography,  // Aquí puedes definir tu tipografía personalizada si lo deseas
        shapes = ValleShapes,          // Aquí puedes definir formas personalizadas si lo deseas
        content = content
    )
}