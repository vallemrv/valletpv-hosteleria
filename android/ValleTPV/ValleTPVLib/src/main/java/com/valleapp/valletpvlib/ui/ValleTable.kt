package com.valleapp.valletpvlib.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valleapp.valletpvlib.db.IBaseEntity
import com.valleapp.valletpvlib.db.InfoField


@Composable
fun BotonItem(obj: InfoField, onButtonClick: (InfoField) -> Unit) {
    ExtendedFloatingActionButton(
        onClick = {
           onButtonClick(obj)
        },
        modifier = Modifier
            .padding(8.dp)
            .height(150.dp),
        containerColor = obj.color

    ) {
        Text(
            text = obj.text,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(9.dp)
                .wrapContentSize(Alignment.Center),
            lineHeight = 40.sp,
            color = Color.Black,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
    }
}



//Creamos una tabla Grid compose para contener los botones de la calculadora
//parametros: filas, columnas, lista de botones y una funcion que se ejecuta al pulsar un boton
@Composable
fun ValleGrid(
    columns: Int,
    botones: List<IBaseEntity>,
    onButtonClick: (InfoField) -> Unit
) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxWidth(),
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(botones) { info ->
            BotonItem(obj = info.getInfoField(), onButtonClick = onButtonClick )
        }
    }
}

