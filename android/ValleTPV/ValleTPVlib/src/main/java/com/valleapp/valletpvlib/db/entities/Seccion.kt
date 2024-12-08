package com.valleapp.valletpvlib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "secciones")
data class Seccion(
    @PrimaryKey val ID: Int,
    val Nombre: String,
    val Orden: Int,
    val RGB: String
)