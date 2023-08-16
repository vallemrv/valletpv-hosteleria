package com.valleapp.valletpvlib.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject

abstract class DBCamareros(context: Context) : DBBase(context, "camareros") {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS camareros (
                    ID INTEGER PRIMARY KEY,
                    nombre TEXT, apellidos TEXT,
                    activo TEXT,
                    pass_field TEXT,
                    autorizado TEXT,
                    permisos TEXT
                )"""
        )
    }

    override fun inicializar() {
        val db = this.writableDatabase
        onCreate(db)
    }
    @SuppressLint("Range")
    override fun cursorToJSON(res: Cursor?): JSONObject? {
        val cam = JSONObject()
        try {
            if (res != null) {
                cam.put("nombre", res.getString(res.getColumnIndex("nombre")))
            }
            if (res != null) {
                cam.put("apellidos", res.getString(res.getColumnIndex("apellidos")))
            }
            if (res != null) {
                cam.put("ID", res.getString(res.getColumnIndex("ID")))
            }
            if (res != null) {
                cam.put("pass_field", res.getString(res.getColumnIndex("pass_field")))
            }
            if (res != null) {
                cam.put("autorizado", res.getString(res.getColumnIndex("autorizado")))
            }
            if (res != null) {
                cam.put("permisos", res.getString(res.getColumnIndex("permisos")))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cam
    }

    override fun caragarValues(o: JSONObject?): ContentValues {
        val v = ContentValues()
        try {
            val id = o?.getString("id")
            v.put("ID", id)
            if (o != null) {
                v.put("activo", o.getString("activo"))
            }
            if (o != null) {
                v.put("pass_field", o.getString("pass_field"))
            }
            if (o != null) {
                v.put("autorizado", o.getString("autorizado"))
            }
            if (o != null) {
                v.put("nombre", o.getString("nombre"))
            }
            if (o != null) {
                v.put("apellidos", o.getString("apellidos"))
            }
            if (o != null) {
                v.put("permisos", o.getString("permisos"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return v
    }



    fun getAll(): JSONArray {
        return filter("activo=1")
    }

    fun setAutorizado(id: Int, a: Boolean) {
        val autorizado = if (a) "1" else "0"
        val db = readableDatabase
        val cv = ContentValues()
        cv.put("autorizado", autorizado)
        db.update("camareros", cv, "ID=$id", null)
    }

    fun getAutorizados(a: Boolean): ArrayList<JSONObject> {
        val autorizado = if (a) "1" else "0"
        val db = readableDatabase
        val res = db.rawQuery("select * from camareros WHERE activo='1' and autorizado = '$autorizado'", null)
        return cargarRegistros(res)
    }

    @SuppressLint("Range")
    private fun cargarRegistros(res: Cursor): ArrayList<JSONObject> {
        val lscam = ArrayList<JSONObject>()
        res.moveToFirst()
        while (!res.isAfterLast) {
            cursorToJSON(res)?.let { lscam.add(it) }
            res.moveToNext()
        }
        return lscam
    }

    fun getConPermiso(permiso: String): ArrayList<JSONObject> {
        val db = readableDatabase
        val res = db.rawQuery("select * from camareros where activo='1' and permisos LIKE '%$permiso%'", null)
        return cargarRegistros(res)
    }

    fun addCamNuevo(n: String, a: String) {
        val db = writableDatabase
        try {
            val v = ContentValues()
            v.put("activo", 1)
            v.put("pass_field", "")
            v.put("autorizado", 1)
            v.put("nombre", n)
            v.put("apellidos", a)
            v.put("permisos", "")
            db.insert("camareros", null, v)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
