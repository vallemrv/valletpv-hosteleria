package com.valleapp.valletpvlib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.valleapp.valletpvlib.interfaces.IBaseEntity
import org.json.JSONObject

@Entity(tableName = "camareros")
data class Camarero(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val apellidos: String,
    val activo: Boolean,
    val passField: String,
    val autorizado: Boolean,
    val permisos: String
) : IBaseEntity<Camarero> {

    override fun emtityFromJson(obj: JSONObject): Camarero {
        return Camarero(
            id = obj.getLong("id"),
            nombre = obj.getString("nombre"),
            apellidos = obj.getString("apellidos"),
            activo = obj.getInt("activo") == 1,
            passField = obj.getString("pass"),
            autorizado = obj.getInt("autorizado") == 1,
            permisos = obj.getString("permisos")
        )
    }

    override fun jsonFromEmtity(entity: Camarero): JSONObject {
        val json = JSONObject()
        json.put("id", entity.id)
        json.put("nombre", entity.nombre)
        json.put("apellidos", entity.apellidos)
        json.put("activo", if (entity.activo) 1 else 0)
        json.put("pass", entity.passField)
        json.put("autorizado", if (entity.autorizado) 1 else 0)
        json.put("permisos", entity.permisos)
        return json
    }
}