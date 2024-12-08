package com.valleapp.valletpvlib.db.interfaces

import androidx.room.*
import com.valleapp.valletpvlib.db.entities.MesaEntity

@Dao
interface MesaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mesa: MesaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mesas: List<MesaEntity>)

    @Update
    suspend fun update(mesa: MesaEntity)

    @Query("DELETE FROM mesas WHERE ID = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM mesas WHERE IDZona = :idZona ORDER BY Orden DESC")
    suspend fun getAllByZone(idZona: Int): List<MesaEntity>

    @Query("SELECT * FROM mesas WHERE IDZona = :idZona AND ID != :idMesa ORDER BY Orden DESC")
    suspend fun getAllExceptOne(idZona: Int, idMesa: Int): List<MesaEntity>

    @Query("UPDATE mesas SET abierta = 1, num = 0 WHERE ID = :idMesa")
    suspend fun abrirMesa(idMesa: Int)

    @Query("UPDATE mesas SET abierta = 0, num = 0 WHERE ID = :idMesa")
    suspend fun cerrarMesa(idMesa: Int)

    @Query("UPDATE mesas SET num = 1 WHERE ID = :idMesa")
    suspend fun marcarRojo(idMesa: Int)
}
