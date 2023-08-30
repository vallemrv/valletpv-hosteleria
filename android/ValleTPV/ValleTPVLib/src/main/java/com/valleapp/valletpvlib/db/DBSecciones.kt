package com.valleapp.valletpvlib.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Query
import org.json.JSONObject

@Entity(tableName = "secciones")
data class Seccion(
    var nombre: String = "",
    var color: String = "#FFC0CB",
    var orden: Int = 0,
    // Puedes usar una String para representar la ruta al archivo del icono si estás usando una base de datos local.
    var url: String? = null,
): BaseEntity() {

    // Suponiendo que tienes una función similar para cargar desde un JSONObject
    private fun loadJson(json: JSONObject) {
        nombre = json.getString("nombre") ?: ""
        orden = json.getInt("orden")
        url = json.getString("url")  // Asumiendo que el servidor retorna una ruta o URL como String.
        id = json.getLong("id")
    }

    override fun executeAccion(json: JSONObject, dao: IBaseDao<out BaseEntity>, op: String) {
        loadJson(json)
        val tb = dao as SeccionesDao
        when (op) {
            "INS" -> tb.insert(this)
            "UP" -> tb.update(this)
        }
    }

    override fun toString(): String {
        return nombre
    }
}


@Dao
interface SeccionesDao: IBaseDao<Seccion> {

    @Query("DELETE FROM secciones WHERE ID = :id")
    override fun deleteById(id: Long)

    @Query("SELECT * FROM secciones")
    override fun getAll(): List<Seccion>

    @Query("SELECT * FROM secciones")
    fun getListaLive(): LiveData<List<Seccion>>
}
