package com.valleapp.valletpvlib.db.interfaces

import androidx.room.Dao
import androidx.room.Query
import com.valleapp.valletpvlib.db.entities.Cuenta

@Dao
interface CuentaDao: BaseDao<Cuenta> {

    @Query("DELETE FROM cuenta WHERE Estado != 'N'")
    suspend fun deleteNonNewEntries()

    @Query("SELECT *, COUNT(ID) AS Can, SUM(Precio) AS Total " +
            "FROM cuenta WHERE :cWhere GROUP BY IDArt, Descripcion, Precio, Estado " +
            "ORDER BY ID DESC")
    suspend fun filterGroup(cWhere: String): List<Cuenta>

    @Query("SELECT * FROM cuenta WHERE IDMesa = :idMesa AND (Estado = 'N' OR Estado = 'P')")
    suspend fun getAllByMesa(idMesa: Int): List<Cuenta>

    @Query("SELECT SUM(Precio) AS TotalTicket FROM cuenta WHERE IDMesa = :idMesa AND (Estado = 'N' OR Estado = 'P')")
    suspend fun getTotalByMesa(idMesa: Int): Double

    @Query("UPDATE cuenta SET IDMesa = :newMesaId WHERE IDMesa = :oldMesaId AND Estado != 'N'")
    suspend fun cambiarCuenta(oldMesaId: Int, newMesaId: Int)

    @Query("DELETE FROM cuenta WHERE IDMesa = :idMesa")
    suspend fun deleteByMesa(idMesa: Int)


    @Query("DELETE FROM cuenta WHERE ID IN (SELECT ID FROM cuenta WHERE IDMesa = :idMesa AND IDArt = :idArt " +
            "AND Descripcion = :descripcion AND Precio = :precio LIMIT :limit)")
    suspend fun eliminarArt(idMesa: Int, idArt: Int, descripcion: String, precio: Double, limit: Int): Int

    @Query("SELECT *, COUNT(ID) AS Can FROM cuenta WHERE IDMesa = :idMesa AND Estado = 'N' " +
            "GROUP BY IDArt, Descripcion, Precio, Estado")
    suspend fun getNuevos(idMesa: Int): List<Cuenta>
}
