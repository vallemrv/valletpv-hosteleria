package com.valleapp.valletpv.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by valle on 13/10/14.
 */
public class DBZonas extends DBBase {

    public DBZonas(Context context) {
        super(context, "zonas");
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS  zonas (ID INTEGER PRIMARY KEY, Nombre TEXT, RGB TEXT, Tarifa INTEGER)");
    }

    public JSONArray getAll()
    {
       return filter(null);
    }

    @SuppressLint("Range")
    @Override
    protected JSONObject cursorToJSON(Cursor res) {
        JSONObject cam = new JSONObject();
        try {
            cam.put("Nombre", res.getString(res.getColumnIndex("Nombre")));
            cam.put("ID", res.getString(res.getColumnIndex("ID")));
            cam.put("Tarifa", res.getString(res.getColumnIndex("Tarifa")));
            cam.put("RGB", res.getString(res.getColumnIndex("RGB")));
        }catch (Exception e){
            e.printStackTrace();
        }
        return cam;
    }

    @Override
    protected ContentValues caragarValues(JSONObject o) {
        ContentValues values = new ContentValues();
        try {
            values.put("ID", o.getInt("id"));
            values.put("Nombre", o.getString("nombre"));
            values.put("RGB", o.getString("rgb"));
            values.put("Tarifa", o.getString("tarifa"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }
}
