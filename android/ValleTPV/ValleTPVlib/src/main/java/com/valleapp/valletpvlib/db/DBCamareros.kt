package com.valleapp.valletpvlib.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class DBCamareros(context: Context) : DBBase(context, "camareros") {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS camareros (
                ID INTEGER PRIMARY KEY, 
                nombre TEXT, 
                apellidos TEXT, 
                activo TEXT, 
                pass_field TEXT, 
                autorizado TEXT, 
                permisos TEXT)"""
        )
    }

    override fun inicializar() {
        val db = writableDatabase
        this.onCreate(db)
    }

    override fun cursorToJSON(res: Cursor): JSONObject {
        val cam = JSONObject()
        try {
            cam.put("nombre", res.getString(res.getColumnIndexOrThrow("nombre")))
            cam.put("apellidos", res.getString(res.getColumnIndexOrThrow("apellidos")))
            cam.put("ID", res.getString(res.getColumnIndexOrThrow("ID")))
            cam.put("pass_field", res.getString(res.getColumnIndexOrThrow("pass_field")))
            cam.put("activo", res.getString(res.getColumnIndexOrThrow("activo")))
            cam.put("autorizado", res.getString(res.getColumnIndexOrThrow("autorizado")))
            cam.put("permisos", res.getString(res.getColumnIndexOrThrow("permisos")))
        } catch (e: Exception) {
            Log.e("DB_CAMAREROS", e.toString())
        }
        return cam
    }

    override fun cargarValues(o: JSONObject): ContentValues {
        val v = ContentValues()
        try {
            val id = o.getString("id")
            v.put("id", id)
            v.put("activo", o.getString("activo"))
            v.put("pass_field", o.getString("pass_field"))
            v.put("autorizado", o.getString("autorizado"))
            v.put("nombre", o.getString("nombre"))
            v.put("apellidos", o.getString("apellidos"))
            v.put("permisos", o.getString("permisos"))
        } catch (e: Exception) {
            Log.e("DB_CAMAREROS", e.toString())
        }
        return v
    }

    fun getAll(): JSONArray {
        return filter("activo=1")
    }

    fun setAutorizado(id: Int, a: Boolean) {
        val autorizado = if (a) "1" else "0"
        val db = readableDatabase
        val cv = ContentValues().apply {
            put("autorizado", autorizado)
        }
        db.update("camareros", cv, "ID=?", arrayOf(id.toString()))
    }

    fun getAutorizados(a: Boolean): ArrayList<JSONObject> {
        val autorizado = if (a) "1" else "0"
        val db = readableDatabase
        val res = db.rawQuery(
            "SELECT * FROM camareros WHERE activo='1' AND autorizado = '$autorizado'",
            null
        )

        return cargarRegistros(res)
    }

    private fun cargarRegistros(res: Cursor): ArrayList<JSONObject> {
        val lscam = arrayListOf<JSONObject>()
        try {
            while (res.moveToNext()) {
                lscam.add(cursorToJSON(res))
            }
        } finally {
            res.close()
        }
        return lscam
    }

    fun getConPermiso(permiso: String): ArrayList<JSONObject> {
        val db = readableDatabase
        val res = db.rawQuery(
            "SELECT * FROM camareros WHERE activo='1' AND permisos LIKE '%$permiso%'",
            null
        )
        return cargarRegistros(res)
    }

    fun addCamNuevo(n: String, a: String) {
        val db = writableDatabase
        try {
            val v = ContentValues().apply {
                put("activo", 1)
                put("pass_field", "")
                put("autorizado", 1)
                put("nombre", n)
                put("apellidos", a)
                put("permisos", "")
            }
            db.insert("camareros", null, v)
        } catch (e: Exception) {
            Log.e("DB_CAMAREROS", e.toString())
        }
    }
}
