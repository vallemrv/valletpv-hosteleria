package com.valleapp.valletpvlib.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.valleapp.valletpvlib.interfaces.IBaseDatos
import com.valleapp.valletpvlib.interfaces.IBaseSocket
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

abstract class DBBase(
    context: Context?,
    protected val tbName: String
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), IBaseDatos, IBaseSocket {


    companion object {
        const val DATABASE_VERSION = 3
        const val DATABASE_NAME = "valletpv"
    }

    abstract override fun onCreate(db: SQLiteDatabase)

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $tbName")
        onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $tbName")
        onCreate(db)
    }

    override fun inicializar() {
        onCreate(writableDatabase)
    }

    protected abstract fun cursorToJSON(res: Cursor): JSONObject
    protected abstract fun cargarValues(o: JSONObject): ContentValues

    override fun filter(cWhere: String?): JSONArray {
        val db = readableDatabase
        val whereClause = cWhere?.let { " WHERE $it" } ?: ""
        val res = db.rawQuery("SELECT * FROM $tbName $whereClause", null)
        val list = JSONArray()
        try {
            while (res.moveToNext()) {
                try {
                    list.put(cursorToJSON(res))
                } catch (e: Exception) {
                    Log.e("DBBase", e.toString())
                }
            }
        } finally {
            res.close()
        }
        return list
    }

    override fun rellenarTabla(objs: JSONArray) {
        val db = writableDatabase

        db.execSQL("DELETE FROM $tbName")
        for (i in 0 until objs.length()) {
            try {
                insert(objs.getJSONObject(i))
            } catch (e: JSONException) {
                Log.e("DBBase", e.toString())
            }
        }
    }

    override fun insert(o: JSONObject) {
        val db = writableDatabase
        try {
            synchronized(db) {

                val id = o.optString("ID", o.optString("id"))
                val values = cargarValues(o)

                if (tbName == "cuenta") {
                    db.delete(tbName, "estado='E' AND IDMesa = ?", arrayOf(values.getAsString("IDMesa")))
                }

                val count = count(db, "ID=$id")
                println("DBBase insert: $tbName, ID: $id, count: $count, values: $values")
                if (count == 0) {
                    db.insert(tbName, null, values)
                } else {
                    db.update(tbName, values, "ID=?", arrayOf(id))
                }
            }
        } catch (e: Exception) {
            Log.e("DBBase", e.toString())
        }
    }

    override fun update(o: JSONObject) {
        insert(o)
    }

    override fun rm(o: JSONObject) {
        val db = writableDatabase
        println("DBBase rm $tbName, : $o")
        try {
            synchronized(db) {
                val id = o.optString("ID", o.optString("id"))
                db.delete(tbName, "ID=?", arrayOf(id))
            }
        } catch (e: Exception) {
            Log.e("DBBase", e.toString())
        }
    }

    private fun count(db: SQLiteDatabase, cWhere: String?): Int {
        val whereClause = cWhere?.let { " WHERE $it" } ?: ""
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $tbName $whereClause", null)
        return try {
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        } finally {
            cursor.close()
        }
    }
}
