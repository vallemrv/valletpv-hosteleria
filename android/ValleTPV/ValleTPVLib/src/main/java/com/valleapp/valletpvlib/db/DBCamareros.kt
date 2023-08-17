package com.valleapp.valletpvlib.db

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query


@Entity(tableName = "camareros")
data class Camarero(
    val nombre: String,
    val apellidos: String,
    val activo: Boolean,
    @ColumnInfo(name = "pass_field") val passField: String,
    val autorizado: Boolean,
    val permisos: String
): BaseEntity()
@Dao
interface CamareroDao: BaseDao<Camarero> {

    @Query("SELECT * FROM camareros ")
    fun getAll(): LiveData<List<Camarero>>

    @Query("UPDATE camareros SET autorizado = :autorizado WHERE ID = :id")
    fun setAutorizado(id: Long, autorizado: Boolean)

    @Query("SELECT * FROM camareros WHERE activo = true AND autorizado = :autorizado")
    fun getAutorizados(autorizado: Boolean): LiveData<List<Camarero>>

    @Query("SELECT * FROM camareros WHERE activo = 1 AND permisos LIKE :permiso")
    fun getConPermiso(permiso: String): LiveData<List<Camarero>>

    @Insert
    fun insertCamarero(camarero: Camarero)

}



