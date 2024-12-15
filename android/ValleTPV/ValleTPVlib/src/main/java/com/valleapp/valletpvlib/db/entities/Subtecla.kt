package com.valleapp.valletpvlib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.valleapp.valletpvlib.interfaces.IBaseEntity
import org.json.JSONObject

@Entity(tableName = "subteclas")
data class Subtecla(
    @PrimaryKey val ID: Int,
    val Nombre: String,
    val Incremento: Double,
    val IDTecla: Int,
    val descripcion_t: String?,
    val descripcion_r: String?
) : IBaseEntity<Subtecla> {

    override fun emtityFromJson(obj: JSONObject): Subtecla {
        return Subtecla(
            ID = obj.getInt("ID"),
            Nombre = obj.getString("Nombre"),
            Incremento = obj.getDouble("Incremento"),
            IDTecla = obj.getInt("IDTecla"),
            descripcion_t = obj.optString("descripcion_t"),
            descripcion_r = obj.optString("descripcion_r")
        )
    }

    override fun jsonFromEmtity(entity: Subtecla): JSONObject {
        val json = JSONObject()
        json.put("ID", entity.ID)
        json.put("Nombre", entity.Nombre)
        json.put("Incremento", entity.Incremento)
        json.put("IDTecla", entity.IDTecla)
        json.put("descripcion_t", entity.descripcion_t)
        json.put("descripcion_r", entity.descripcion_r)
        return json
    }
}