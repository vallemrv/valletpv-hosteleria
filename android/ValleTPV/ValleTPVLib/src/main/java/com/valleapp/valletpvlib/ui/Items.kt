package com.valleapp.valletpvlib.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valleapp.valletpvlib.db.BaseEntity
import com.valleapp.valletpvlib.ui.theme.ColorTheme
import com.valleapp.valletpvlib.ui.theme.Styles


@Composable
fun ListItem(item: BaseEntity, onItemClick: (BaseEntity) -> Unit) {
    Text(
        text = item.toString(),
        style = Styles.TextListas,
        modifier = Modifier
            .clickable { onItemClick(item) }
            .fillMaxWidth()
            .padding(8.dp)
            .height(70.dp)
            .background(ColorTheme.BgListas)
            .padding(start = 16.dp, top = 16.dp)
    )
}
