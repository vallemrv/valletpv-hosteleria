package com.valleapp.valletpvlib.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject

class DBSecciones(context: Context) : DBBase(context, "secciones") {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS secciones (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "nombre TEXT, " +
                    "icono TEXT )"
        )
    }

    fun getAll(): JSONArray {
        return filter(null)
    }

    override fun filter(cWhere: String?): JSONArray {
        val db = readableDatabase
        val w = if (cWhere != null) " WHERE $cWhere" else ""
        val res = db.rawQuery("SELECT * FROM $tbName $w", null)
        res.moveToFirst()
        val list = JSONArray()
        try {
            while (!res.isAfterLast) {
                try {
                    list.put(cursorToJSON(res))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                res.moveToNext()
            }
        } finally {
            res.close()
        }
        return list
    }

    override fun inicializar() {
        val db = writableDatabase
        onCreate(db)
    }

    override fun cursorToJSON(res: Cursor): JSONObject {
        val obj = JSONObject()
        try {
            obj.put("nombre", res.getString(res.getColumnIndexOrThrow("nombre")))
            obj.put("ID", res.getString(res.getColumnIndexOrThrow("ID")))
            obj.put("icono", res.getString(res.getColumnIndexOrThrow("icono")))
         } catch (e: Exception) {
            e.printStackTrace()
        }
        return obj
    }



    override fun cargarValues(o: JSONObject): ContentValues {
        val values = ContentValues()
        try {
            values.put("ID", o.getInt("id"))
            values.put("nombre", o.getString("nombre"))
            values.put("icono", o.getString("icono"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return values
    }
}