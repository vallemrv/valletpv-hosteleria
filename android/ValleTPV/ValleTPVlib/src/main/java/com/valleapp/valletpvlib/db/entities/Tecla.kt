package com.valleapp.valletpvlib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.valleapp.valletpvlib.interfaces.IBaseEntity
import org.json.JSONObject

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
) : IBaseEntity<Tecla> {

    override fun entityFromJson(obj: JSONObject): Tecla {
        return Tecla(
            ID = obj.getInt("ID"),
            Nombre = obj.getString("Nombre"),
            P1 = obj.getDouble("P1"),
            P2 = obj.getDouble("P2"),
            Precio = obj.getDouble("Precio"),
            RGB = obj.getString("RGB"),
            IDSeccion = obj.getInt("IDSeccion"),
            Tag = obj.optString("Tag"),
            Orden = obj.getInt("Orden"),
            IDSec2 = obj.optInt("IDSec2"),
            tipo = obj.optString("tipo"),
            descripcion_t = obj.optString("descripcion_t"),
            descripcion_r = obj.optString("descripcion_r")
        )
    }

    override fun jsonFromEntity(entity: Tecla): JSONObject {
        val json = JSONObject()
        json.put("ID", entity.ID)
        json.put("Nombre", entity.Nombre)
        json.put("P1", entity.P1)
        json.put("P2", entity.P2)
        json.put("Precio", entity.Precio)
        json.put("RGB", entity.RGB)
        json.put("IDSeccion", entity.IDSeccion)
        json.put("Tag", entity.Tag)
        json.put("Orden", entity.Orden)
        json.put("IDSec2", entity.IDSec2)
        json.put("tipo", entity.tipo)
        json.put("descripcion_t", entity.descripcion_t)
        json.put("descripcion_r", entity.descripcion_r)
        return json
    }
}