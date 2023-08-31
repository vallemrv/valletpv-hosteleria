package com.valleapp.valletpvlib.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Query
import org.json.JSONObject

@Entity(tableName = "teclas")
data class Tecla(
    var nombre: String = "",
    var p1: Double = 0.0,
    var p2: Double = 0.0,
    var incremento: Double = 0.0,
    var orden: Int = -1,
    var familia: Int = -1,
    var tag: String = "",
    var descripcionR: String = "",
    var descripcionT: String = "",
    var seccion: Int = -1,
    var parent: Int = -1,
    var color: String = "#FFC0CB",  // Color rosado por defecto
    var nombreFam: String = "",
    var seccionNombre: String = "",
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

    private fun loadJson(json: JSONObject) {
        id = json.getLong("id")
        nombre = json.getString("nombre")
        p1 = json.getDouble("p1")
        p2 = json.getDouble("p2")
        incremento = json.getDouble("incremento")
        orden = json.getInt("orden")
        familia = json.getInt("familia")
        tag = json.getString("tag")
        descripcionR = json.getString("descripcionR")
        descripcionT = json.getString("descripcionT")
        seccion = json.getInt("seccion")
        parent = json.getInt("parent")
        color = json.getString("color")
        nombreFam = json.getString("nombreFam")
        seccionNombre = json.getString("seccionNombre")
        child = json.getInt("child")
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

    @Query("SELECT * FROM teclas WHERE nombre LIKE '%' || :strBus || '%' OR tag LIKE '%' || :strBus || '%' OR descripcionR LIKE '%' || :strBus || '%' OR descripcionT LIKE '%' || :strBus || '%'")
    fun getByBusqueda(strBus: String): List<Tecla>

    @Query("DELETE FROM teclas WHERE id = :id")
    override fun deleteById(id: Long)


}
