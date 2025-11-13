package com.valleapp.valletpvlib.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject

class DBSugerencias(context: Context) : DBBase(context, "sugerencias") {


    override fun onCreate(db: SQLiteDatabase) {
         db.execSQL("CREATE TABLE IF NOT EXISTS $tbName (ID TEXT PRIMARY KEY, IDTecla TEXT, sugerencia TEXT, Incremento DECIMAL(10,2));")
    }

    @SuppressLint("Range")
    override fun cursorToJSON(res: Cursor): JSONObject {
        val obj = JSONObject()
        try {
            obj.put("ID", res.getString(res.getColumnIndex("ID")))
            obj.put("sugerencia", res.getString(res.getColumnIndex("sugerencia")))
            obj.put("tecla", res.getString(res.getColumnIndex("IDTecla")))
            obj.put("incremento", res.getDouble(res.getColumnIndex("Incremento")))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return obj
    }



    override fun cargarValues(o: JSONObject): ContentValues  {
        val values = ContentValues()
        try {
            values.put("ID", o.getInt("id"))
            values.put("IDTecla", o.getString("tecla"))
            values.put("sugerencia", o.getString("sugerencia"))
            values.put("Incremento", o.getDouble("incremento"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return values
    }

    fun getAll(): JSONArray {
        return filter(null)
    }

    override fun filter(cWhere: String?): JSONArray {
        val array = JSONArray()
        val db = readableDatabase
        val selectQuery = if (cWhere != null) {
            "SELECT * FROM $tbName WHERE $cWhere ORDER BY Incremento DESC"
        } else {
            "SELECT * FROM $tbName ORDER BY Incremento DESC"
        }

        val cursor = db.rawQuery(selectQuery, null)
        cursor.use {
            if (cursor.moveToFirst()) {
                do {
                    array.put(cursorToJSON(cursor))
                } while (cursor.moveToNext())
            }
        }
        return array
    }
}
