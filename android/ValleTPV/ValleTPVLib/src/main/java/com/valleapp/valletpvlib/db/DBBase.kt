package com.valleapp.valletpvlib.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.valleapp.valletpvlib.interfaces.IBaseDatos
import com.valleapp.valletpvlib.interfaces.IBaseSocket
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

abstract class DBBase(context: Context?, val tb_name: String) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), IBaseDatos, IBaseSocket {
    abstract override fun onCreate(sqLiteDatabase: SQLiteDatabase)
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS $tb_name")
        onCreate(db)
    }

    override fun inicializar() {
        onCreate(writableDatabase)
    }

    protected abstract fun cursorToJSON(res: Cursor?): JSONObject?
    protected abstract fun caragarValues(o: JSONObject?): ContentValues
    override fun filter(cWhere: String): JSONArray {
        val db = this.readableDatabase
        val w = " WHERE $cWhere"
        val res = db.rawQuery("select * from $tb_name $w", null)
        res.moveToFirst()
        val list = JSONArray()
        while (!res.isAfterLast) {
            try {
                list.put(cursorToJSON(res))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            res.moveToNext()
        }
        return list
    }

    override fun rellenarTabla(objs: JSONArray) {
        val db = writableDatabase
        db.execSQL("DELETE FROM $tb_name")
        for (i in 0 until objs.length()) {
            // Create a new map of values, where column names are the keys
            try {
                insert(objs.getJSONObject(i))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    override fun insert(o: JSONObject) {
        val db = writableDatabase
        try {
            synchronized(db) {
                val id: String = if (o.has("ID")) {
                    o.getString("ID")
                } else {
                    o.getString("id")
                }
                val values = caragarValues(o)
                if (tb_name == "cuenta") {
                    db.delete(
                        tb_name,
                        "estado='N' and IDMesa = ?",
                        arrayOf(values.getAsString("IDMesa"))
                    )
                }
                val count = count(db, "ID=$id")
                if (count == 0) {
                    db.insert(tb_name, null, values)
                } else {
                    db.update(tb_name, values, "ID=?", arrayOf(id))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun update(o: JSONObject) {
        insert(o)
    }

    override fun rm(o: JSONObject) {
        val db = writableDatabase
        try {
            synchronized(db) {
                if (o.has("ID")) {
                    db.delete(tb_name, "ID=?", arrayOf(o.getString("ID")))
                } else {
                    db.delete(tb_name, "ID=?", arrayOf(o.getString("id")))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun count(db: SQLiteDatabase, cWhere: String?): Int {
        var w = ""
        if (cWhere != null) {
            w = " WHERE $cWhere"
        }
        @SuppressLint("Recycle") val mCount = db.rawQuery("select count(*) from  $tb_name $w", null)
        mCount.moveToFirst()
        return mCount.getInt(0)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "valletpv"
    }
}