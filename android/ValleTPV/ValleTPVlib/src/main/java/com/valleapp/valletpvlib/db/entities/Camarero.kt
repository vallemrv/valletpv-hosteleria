package com.valleapp.valletpvlib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "camareros")
data class Camarero(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val apellidos: String,
    val activo: Boolean,
    val passField: String,
    val autorizado: Boolean,
    val permisos: String
)