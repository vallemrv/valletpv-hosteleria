package com.valleapp.vallecom.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.valleapp.vallecom.interfaces.IBaseDatos;

import org.json.JSONArray;
import org.json.JSONException;
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

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS zonas");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public JSONArray getAll()
    {
       return filter(null);
    }


    @Override
    public void rellenarTabla(JSONArray objs) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.execSQL("DELETE FROM zonas");
        }catch (SQLiteException e){
            this.onCreate(db);
        }

        super.rellenarTabla(objs);
    }

    @Override
    @SuppressLint("Range")
    protected JSONObject cursorToJSON(Cursor res) {
        JSONObject o = new JSONObject();
        try {
            o.put("Nombre",res.getString(res.getColumnIndex("Nombre")));
            o.put("ID", res.getString(res.getColumnIndex("ID")));
            o.put("Tarifa", res.getString(res.getColumnIndex("Tarifa")));
            o.put("RGB", res.getString(res.getColumnIndex("RGB")));
        }catch (Exception e){
            e.printStackTrace();
        }
        return o;
    }

    @Override
    protected ContentValues caragarValues(JSONObject o) {
        ContentValues values = new ContentValues();
        try {
            values.put("ID", o.getInt("id"));
            values.put("Nombre", o.getString("nombre"));
            values.put("RGB", o.getString("rgb"));
            values.put("Tarifa", o.getString("tarifa"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return  values;
    }
}
