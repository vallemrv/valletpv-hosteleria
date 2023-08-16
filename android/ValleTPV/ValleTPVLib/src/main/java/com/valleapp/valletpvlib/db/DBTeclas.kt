package com.valleapp.valletpvlib.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by valle on 13/10/14.
 */
abstract class DBTeclas(context: Context?) : DBBase(context, "teclas") {
    private var tarifa = 0
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS  teclas (" +
                    "ID INTEGER PRIMARY KEY, Nombre TEXT, P1 DOUBLE, P2 DOUBLE, Precio DOUBLE," +
                    " RGB TEXT, IDSeccion INTEGER, Tag TEXT, Orden INTEGER, IDSec2 INTEGER, tipo TEXT, " +
                    " descripcion_t TEXT, descripcion_r TEXT )"
        )
    }

    private fun cargarRegistros(sql: String): JSONArray {
        val ls = JSONArray()
        val db = this.readableDatabase
        val res = db.rawQuery(sql, null)
        res.moveToFirst()
        while (!res.isAfterLast) {
            ls.put(cursorToJSON(res))
            res.moveToNext()
        }
        return ls
    }

    fun getAll(id: String, tarifa: Int): JSONArray {
        this.tarifa = tarifa
        return cargarRegistros("SELECT * FROM teclas WHERE IDSeccion=$id OR IDSec2=$id ORDER BY Orden DESC")
    }

    fun findLike(str: String, t: String): JSONArray {
        tarifa = t.toInt()
        return cargarRegistros("SELECT * FROM teclas WHERE Nombre LIKE '%$str%' OR Tag LIKE '%$str%' ORDER BY Orden DESC LIMIT 15 ")
    }

    override fun inicializar() {
        val db = this.writableDatabase
        onCreate(db)
    }

    @SuppressLint("Range")
    override fun cursorToJSON(res: Cursor?): JSONObject? {
        val obj = JSONObject()
        try {
            obj.put("Nombre", res!!.getString(res.getColumnIndex("Nombre")))
            obj.put("ID", res.getString(res.getColumnIndex("ID")))
            obj.put("RGB", res.getString(res.getColumnIndex("RGB")))
            obj.put("tipo", res.getString(res.getColumnIndex("tipo")))
            obj.put("descripcion_t", res.getString(res.getColumnIndex("descripcion_t")))
            obj.put("descripcion_r", res.getString(res.getColumnIndex("descripcion_r")))
            if (tarifa == 2) obj.put(
                "Precio",
                res.getString(res.getColumnIndex("P2"))
            ) else obj.put(
                "Precio", res.getString(
                    res.getColumnIndex("P1")
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return obj
    }

    override fun caragarValues(o: JSONObject?): ContentValues {
        val values = ContentValues()
        try {
            values.put("ID", o!!.getInt("id"))
            values.put("IDSeccion", o.getInt("IDSeccion"))
            values.put("Nombre", o.getString("nombre"))
            values.put("P1", o.getDouble("p1"))
            values.put("P2", o.getDouble("p2"))
            values.put("Precio", o.getDouble("Precio"))
            values.put("RGB", o.getString("RGB"))
            values.put("Tag", o.getString("tag"))
            values.put("IDSec2", o.getString("IDSec2"))
            values.put("Orden", o.getString("orden"))
            values.put("tipo", o.getString("tipo"))
            values.put("descripcion_t", o.getString("descripcion_t"))
            values.put("descripcion_r", o.getString("descripcion_r"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return values
    }
}