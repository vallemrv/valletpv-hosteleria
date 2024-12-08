package com.valleapp.valletpvlib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cuenta")
data class Cuenta(
    @PrimaryKey val ID: String,
    val Estado: String?,
    val Descripcion: String?,
    val descripcion_t: String?,
    val Precio: Double?,
    val IDPedido: Int?,
    val IDMesa: Int?,
    val IDArt: Int?,
    val nomMesa: String?,
    val IDZona: String?,
    val servido: Int?
)
