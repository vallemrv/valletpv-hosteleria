package com.valleapp.valletpvlib.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.valleapp.valletpvlib.db.BaseEntity
import com.valleapp.valletpvlib.db.IBaseEntity
import com.valleapp.valletpvlib.db.LineaCuenta
import com.valleapp.valletpvlib.ui.theme.ColorTheme
import com.valleapp.valletpvlib.ui.theme.Styles


@Composable
fun ValleList(
    title: String,
    color: Color = ColorTheme.Primary,
    items: LazyListScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = Styles.H2,
            modifier = Modifier
                .fillMaxWidth()
                .background(color)
                .padding(8.dp),
            textAlign = TextAlign.Center
        )

        LazyColumn{
            items()
        }
    }
}


@Composable
fun ListaSimple(title:String, list: List<BaseEntity>, onItemClick: (IBaseEntity) -> Unit) {
    ValleList(title = title) {
        items(list) { item ->
            ListItem(item = item, onItemClick = {
                    onItemClick(it)
            })
        }
    }
}




@Composable
fun ListaCuenta(title:String, list: List<LineaCuenta>, onBorrarClick: (LineaCuenta) -> Unit) {
    ValleList(title = title) {
        items(list) { item ->
            ListItemCuenta(item)
        }
    }
}

