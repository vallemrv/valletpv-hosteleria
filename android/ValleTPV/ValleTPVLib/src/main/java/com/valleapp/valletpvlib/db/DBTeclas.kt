package com.valleapp.valletpvlib.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Query
import org.json.JSONObject

@Entity(tableName = "teclas")
data class Tecla(
    var nombre: String = "",
    var p1: Float = 0f,
    var p2: Float = 0f,
    var incremento: Float = 0f,
    var orden: Int = 0,
    var familiaId: Int? = null,
    var tag: String = "",
    var descripcion_r: String? = null,
    var descripcion_t: String? = null,
    var seccionId: Int? = null,
    var parentId: Int? = null,
    var color: String = "#FFC0CB",  // Color rosado por defecto
    var nombreFam: String? = null,
    var seccion_nombre: String? = null,
    var child: Int = 0
) : BaseEntity() {


    override fun executeAccion(json: JSONObject, dao: IBaseDao<out BaseEntity>, op: String) {
        loadJson(json)
        val tb = dao as TeclasDao
        when (op) {
            "INS" -> tb.insert(this)
            "UP" -> tb.update(this)
        }
    }

    fun loadJson(json: JSONObject) {
        nombre = json.optString("nombre", nombre)
        p1 = json.optDouble("p1", p1.toDouble()).toFloat()
        p2 = json.optDouble("p2", p2.toDouble()).toFloat()
        incremento = json.optDouble("incremento", incremento.toDouble()).toFloat()
        orden = json.optInt("orden", orden)
        familiaId = json.optInt("familiaId", (familiaId ?: JSONObject.NULL) as Int).takeIf { it != JSONObject.NULL }
        tag = json.optString("tag", tag)
        descripcion_r = descripcion_r?.let { json.optString("descripcion_r", it) }
        descripcion_t = descripcion_t?.let { json.optString("descripcion_t", it) }
        seccionId = json.optInt("seccionId", (seccionId ?: JSONObject.NULL) as Int).takeIf { it != JSONObject.NULL }
        parentId = json.optInt("parentId", (parentId ?: JSONObject.NULL) as Int).takeIf { it != JSONObject.NULL }
        color = json.optString("color", color)
        nombreFam = nombreFam?.let { json.optString("nombreFam", it) }
        seccion_nombre = seccion_nombre?.let { json.optString("seccion_nombre", it) }
        child = json.optInt("child", child)
    }
}


@Dao
interface TeclasDao : IBaseDao<Tecla> {

    @Query("SELECT * FROM teclas")
    override fun getAll(): List<Tecla>

    @Query("SELECT * FROM teclas WHERE seccionId = :seccionId")
    fun getBySeccion(seccionId: Int): LiveData<List<Tecla>>

    @Query("SELECT * FROM teclas WHERE parentId = :parentId")
    fun getByParent(parentId: Int): LiveData<List<Tecla>>

    @Query("SELECT * FROM teclas WHERE parentId = :parentId")
    fun getChild(parentId: Int): LiveData<Tecla?>

    @Query("DELETE FROM teclas WHERE id = :id")
    override fun deleteById(id: Long)

}
