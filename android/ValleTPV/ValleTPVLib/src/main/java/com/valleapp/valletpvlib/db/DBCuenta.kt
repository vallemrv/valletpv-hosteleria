package com.valleapp.valletpvlib.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

/**
 * Created by valle on 13/10/14.
 */
abstract class DBCuenta(context: Context?) : DBBase(context, "cuenta") {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS cuenta " +
                    "(ID TEXT PRIMARY KEY, Estado TEXT, " +
                    "Descripcion TEXT, descripcion_t TEXT, " +
                    "Precio DOUBLE, IDPedido INTEGER, " +
                    "IDMesa INTEGER," +
                    "IDArt INTEGER," +
                    "nomMesa TEXT, IDZona TEXT," +
                    "servido INTEGER )"
        )
    }

    override fun rellenarTabla(objs: JSONArray?) {
        val db = writableDatabase
        db.execSQL("DELETE FROM $tb_name WHERE Estado != 'N'")
        if (objs != null) {
            for (i in 0 until objs.length()) {
                // Create a new map of values, where column names are the keys
                try {
                    insert(objs.getJSONObject(i))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun filterGroup(cWhere: String?): JSONArray {
        val lista = JSONArray()
        try {
            var strWhere = ""
            if (cWhere != null) {
                strWhere = " WHERE $cWhere"
            }
            val db = this.readableDatabase
            val res = db.rawQuery(
                "SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total" +
                        " FROM cuenta " + strWhere +
                        " GROUP BY  IDArt, Descripcion, Precio, Estado ORDER BY ID DESC", null
            )
            res.moveToFirst()
            while (!res.isAfterLast) {
                lista.put(cursorToJSON(res))
                res.moveToNext()
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return lista
    }

    fun filterList(cWhere: String?): List<JSONObject?> {
        val lista: MutableList<JSONObject?> = ArrayList()
        try {
            var strWhere = ""
            if (cWhere != null) {
                strWhere = " WHERE $cWhere"
            }
            val db = this.readableDatabase
            val res = db.rawQuery(
                "SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total" +
                        " FROM cuenta " + strWhere +
                        " GROUP BY  IDArt, Descripcion, Precio, Estado ORDER BY ID DESC", null
            )
            res.moveToFirst()
            while (!res.isAfterLast) {
                lista.add(cursorToJSON(res))
                res.moveToNext()
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return lista
    }

    override fun caragarValues(o: JSONObject?): ContentValues {
        val values = ContentValues()
        try {
            values.put("ID", o!!.getString("ID"))
            values.put("IDArt", o.getInt("IDArt"))
            values.put("Descripcion", o.getString("Descripcion"))
            values.put("descripcion_t", o.getString("descripcion_t"))
            values.put("Precio", o.getDouble("Precio"))
            values.put("IDMesa", o.getString("IDMesa"))
            values.put("IDZona", o.getString("IDZona"))
            values.put("nomMesa", o.getString("nomMesa"))
            values.put("IDPedido", o.getString("IDPedido"))
            values.put("Estado", o.getString("Estado"))
            values.put("servido", o.getString("servido"))
        } catch (e: Exception) {
            Log.d("CUENTA-CARGARVALUES", e.message!!)
        }
        return values
    }

    fun getAll(id: String): List<JSONObject?> {
        return filterList("IDMesa =$id AND (Estado = 'N' or Estado = 'P')")
    }

    fun getTotal(id: String): Double {
        val db = this.readableDatabase
        var s = 0.0
        try {
            @SuppressLint("Recycle") val cursor = db.rawQuery(
                "SELECT SUM(Precio) AS TotalTicket " +
                        "FROM cuenta WHERE IDMesa=" + id + " AND (Estado = 'N' or Estado = 'P')",
                null
            )
            cursor.moveToFirst()
            if (cursor.count > 0 && cursor.columnCount > 0) {
                s = cursor.getDouble(0)
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return s
    }

    fun cambiarCuenta(id: String, id1: String?) {
        val db = this.writableDatabase
        try {
            val values = ContentValues()
            values.put("IDMesa", id1)
            db.update("cuenta", values, "IDMesa=$id AND estado != 'N'", null)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("Range")
    override fun cursorToJSON(res: Cursor?): JSONObject? {
        val obj = JSONObject()
        try {
            obj.put("ID", res!!.getString(res.getColumnIndex("ID")))
            obj.put("Descripcion", res.getString(res.getColumnIndex("Descripcion")))
            obj.put("descripcion_t", res.getString(res.getColumnIndex("descripcion_t")))
            val index_can = res.getColumnIndex("Can")
            if (index_can >= 0) {
                obj.put("Can", res.getString(index_can))
            }
            val index_total = res.getColumnIndex("Total")
            if (index_total >= 0) {
                obj.put("Total", res.getString(index_total))
                obj.put("CanCobro", 0)
            }
            obj.put("Precio", res.getString(res.getColumnIndex("Precio")))
            obj.put("IDArt", res.getString(res.getColumnIndex("IDArt")))
            obj.put("Estado", res.getString(res.getColumnIndex("Estado")))
            obj.put("nomMesa", res.getString(res.getColumnIndex("nomMesa")))
            obj.put("IDPedido", res.getString(res.getColumnIndex("IDPedido")))
            obj.put("servido", res.getString(res.getColumnIndex("servido")))
            obj.put("IDZona", res.getString(res.getColumnIndex("IDZona")))
            obj.put("IDMesa", res.getString(res.getColumnIndex("IDMesa")))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return obj
    }

    fun replaceMesa(datos: JSONArray, IDMesa: String) {
        // Gets the data repository in write mode
        val db = this.writableDatabase
        try {
            db.execSQL("DELETE FROM cuenta WHERE IDMesa=$IDMesa AND Estado != 'N'")
            for (i in 0 until datos.length()) {
                insert(datos.getJSONObject(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addArt(IDMesa: Int, art: JSONObject) {
        // Gets the data repository in write mode
        val db = this.writableDatabase
        // Insert the new row, returning the primary key value of the new row
        try {
            val can = art.getInt("Can")
            for (i in 0 until can) {
                val values = ContentValues()
                values.put("ID", UUID.randomUUID().toString())
                values.put("IDArt", art.getInt("ID"))
                values.put("Descripcion", art.getString("Descripcion"))
                values.put("descripcion_t", art.getString("descripcion_t"))
                values.put("Precio", art.getDouble("Precio"))
                values.put("IDMesa", IDMesa)
                values.put("Estado", "N")
                db.insert("cuenta", null, values)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun eliminar(IDMesa: String, lsart: JSONArray) {
        val db = this.writableDatabase
        try {
            for (i in 0 until lsart.length()) {
                val art = lsart.getJSONObject(i)
                val sql = ("DELETE FROM cuenta WHERE ID IN (SELECT ID FROM cuenta WHERE IDMesa="
                        + IDMesa + " AND IDArt=" + art.getString("IDArt")
                        + " AND Descripcion='" + art.getString("Descripcion") + "'"
                        + " AND Precio=" + art.getString("Precio")
                        + " LIMIT " + art.getString("Can") + ")")
                db.execSQL(sql)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun eliminar(IDMesa: String) {
        val db = this.writableDatabase
        try {
            db.execSQL("DELETE FROM cuenta WHERE IDMesa=$IDMesa")
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("Range")
    fun getNuevos(id: String): JSONArray {
        val lista = JSONArray()
        val db = this.readableDatabase
        try {
            val res = db.rawQuery(
                "SELECT *, COUNT(ID) AS Can FROM cuenta WHERE IDMesa=" + id + " AND Estado = 'N'" +
                        " Group by IDArt, Descripcion, Precio, Estado", null
            )
            res.moveToFirst()
            while (!res.isAfterLast) {
                lista.put(cursorToJSON(res))
                res.moveToNext()
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return lista
    }
}