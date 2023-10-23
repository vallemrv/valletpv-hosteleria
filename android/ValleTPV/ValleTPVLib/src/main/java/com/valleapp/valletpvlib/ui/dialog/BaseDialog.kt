package com.valleapp.valletpvlib.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BaseDialog(modifier: Modifier = Modifier, content: @Composable () -> Unit) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.run { Black.copy(alpha = 0.5f) })
        )
        Card(
            elevation = CardDefaults.elevatedCardElevation(8.dp),
            modifier = modifier
        ) {
            content()
        }
    }

}