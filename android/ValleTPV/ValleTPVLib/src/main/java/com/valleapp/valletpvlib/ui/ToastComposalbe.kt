package com.valleapp.valletpvlib.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ToastComposable(
    message: String, show: Boolean,
    timeout: Long,
    fontSize: TextUnit = 20.sp,
    onHide: () -> Unit = {}
) {

    if (show) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Surface(
                color = Color.Gray,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .alpha(0.9f)

            ) {
                Text(
                    text = message,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    fontSize = fontSize,
                )
            }
        }

        // Usando LaunchedEffect para agregar un delay
        LaunchedEffect(key1 = show) {
            delay(timeout)
            onHide()

        }
    }
}
