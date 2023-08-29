package com.valleapp.valletpvlib.db

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
    var familia: Int? = null,
    var tag: String = "",
    var descripcion_r: String? = null,
    var descripcion_t: String? = null,
    var seccion: Int? = null,
    var parent: Int? = null,
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
        nombre = json.optString("nombre")
        p1 = json.optDouble("p1").toFloat()
        p2 = json.optDouble("p2").toFloat()
        incremento = json.optDouble("incremento").toFloat()
        orden = json.optInt("orden")
        familia = json.optInt("familia")
        tag = json.optString("tag", tag)
        descripcion_r = json.optString("descripcion_r")
        descripcion_t = json.optString("descripcion_t")
        seccion = json.optInt("seccion")
        parent = json.optInt("parent")
        color = json.optString("color", color)
        nombreFam = json.optString("nombreFam")
        seccion_nombre = json.optString("seccion_nombre")
        child = json.optInt("child")
    }

    override fun toString(): String {
        return nombre
    }
}


@Dao
interface TeclasDao : IBaseDao<Tecla> {

    @Query("SELECT * FROM teclas")
    override fun getAll(): List<Tecla>

    @Query("SELECT * FROM teclas WHERE seccion = :seccionId")
    fun getBySeccion(seccionId: Int): List<Tecla>

    @Query("SELECT * FROM teclas WHERE parent = :parentId")
    fun getByParent(parentId: Int): List<Tecla>

    @Query("SELECT * FROM teclas WHERE nombre LIKE '%' || :strBus || '%' OR tag LIKE '%' || :strBus || '%' OR descripcion_r LIKE '%' || :strBus || '%' OR descripcion_t LIKE '%' || :strBus || '%'")
    fun getByBusqueda(strBus: String): List<Tecla>

    @Query("DELETE FROM teclas WHERE id = :id")
    override fun deleteById(id: Long)


}
