package com.valleapp.valletpv.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.valleapp.valletpv.interfaces.IBaseDatos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by valle on 13/10/14.
 */
public class DBSecciones extends DBBase {

    public DBSecciones(Context context) {
        super(context, "secciones");
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS secciones (ID INTEGER PRIMARY KEY, Nombre TEXT,Orden INTEGER, RGB TEXT)");
    }

    public JSONArray getAll()
    {
        return filter(null);
    }

    @Override
    public JSONArray filter(String cWhere) {
        SQLiteDatabase db = this.getReadableDatabase();
        String w = "";
        if (cWhere != null) {
            w = " WHERE " + cWhere;
        }
        Cursor res = db.rawQuery("select * from " + tb_name + " " + w +" ORDER BY orden DESC", null);
        res.moveToFirst();
        JSONArray list = new JSONArray();
        while (!res.isAfterLast()) {
            try {
                list.put(cursorToJSON(res));
            } catch (Exception e) {
                e.printStackTrace();
            }
            res.moveToNext();
        }
        return  list;
    }

    @Override
    public void inicializar() {
        SQLiteDatabase db = this.getWritableDatabase();
        this.onCreate(db);
    }

    @SuppressLint("Range")
    @Override
    protected JSONObject cursorToJSON(Cursor res) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("Nombre", res.getString(res.getColumnIndex("Nombre")));
            obj.put("ID", res.getString(res.getColumnIndex("ID")));
            obj.put("RGB", res.getString(res.getColumnIndex("RGB")));
        }catch (Exception e){
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    protected ContentValues caragarValues(JSONObject o) {
        ContentValues values = new ContentValues();
        try {
            values.put("ID", o.getInt("id"));
            values.put("Nombre", o.getString("nombre"));
            values.put("RGB", o.getString("rgb"));
            values.put("Orden", o.getString("orden"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return  values;
    }
}
