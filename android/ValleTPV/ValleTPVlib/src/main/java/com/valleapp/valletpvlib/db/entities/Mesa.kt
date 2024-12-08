package com.valleapp.valletpvlib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mesas")
data class Mesa(
    @PrimaryKey val ID: Int,
    val Nombre: String,
    val RGB: String,
    val abierta: Boolean,
    val IDZona: Int,
    val num: Int,
    val Orden: Int
)
