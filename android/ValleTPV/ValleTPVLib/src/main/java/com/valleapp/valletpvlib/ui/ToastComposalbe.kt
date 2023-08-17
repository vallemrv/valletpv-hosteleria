package com.valleapp.valletpvlib.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ComposableToast(message: String, show: Boolean, onHide: () -> Unit) {
    if (show) {

        Surface(
            color = Color.Gray,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .alpha(0.9f)
                .wrapContentSize(Alignment.Center)  // Centrar el contenido en el Box
        ) {
            Text(
                text = message,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    // Usando LaunchedEffect para agregar un delay
    LaunchedEffect(key1 = message) {
        delay(2000)  // delay de 2 segundos
        onHide()
    }

}
