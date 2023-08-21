package com.valleapp.valletpvlib.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valleapp.valletpvlib.db.IBaseEntity
import com.valleapp.valletpvlib.db.InfoField

enum class TipoBoton{
    SIMPLE, MESA
}

//Creamos una tabla Grid compose para contener los botones de la calculadora
//parametros: filas, columnas, lista de botones y una funcion que se ejecuta al pulsar un boton
@Composable
fun ValleGrid(
    columns: Int,
    botones: List<IBaseEntity>,
    tipo: TipoBoton,
    onButtonClick: (InfoField, String?) -> Unit
) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxWidth(),
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(botones) { info ->
            when(tipo){
                TipoBoton.SIMPLE -> BotonSimple(obj = info.getInfoField(), onButtonClick = onButtonClick )
                TipoBoton.MESA -> BotonMesa(obj = info.getInfoField(), onButtonClick = onButtonClick)
            }
        }
    }
}

