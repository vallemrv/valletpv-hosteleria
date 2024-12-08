package com.valleapp.valletpvlib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teclas")
data class Tecla(
    @PrimaryKey val ID: Int,
    val Nombre: String,
    val P1: Double,
    val P2: Double,
    val Precio: Double,
    val RGB: String,
    val IDSeccion: Int,
    val Tag: String?,
    val Orden: Int,
    val IDSec2: Int?,
    val tipo: String?,
    val descripcion_t: String?,
    val descripcion_r: String?
)
