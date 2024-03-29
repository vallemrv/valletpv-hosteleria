package com.valleapp.valletpvlib.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun BaseDialog(modifier: Modifier = Modifier, showDialog:Boolean, content: @Composable () -> Unit) {
   if (showDialog) {
       Box(
           modifier = Modifier.fillMaxSize(),
           contentAlignment = Alignment.Center
       ) {
           Box(
               modifier = Modifier
                   .fillMaxSize()
                   .background(Color.run { Black.copy(alpha = 0.5f) })
                   .clickable { /* Haz algo aquí para cerrar el diálogo si es necesario */ }
           )
           Card(
               elevation = CardDefaults.elevatedCardElevation(8.dp),
               modifier = modifier
           ) {
               content()
           }
       }
   }
}