package com.valleapp.vallecom.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by valle on 13/10/14.
 */
public class DBTbUpdates extends DBBase {


    public DBTbUpdates(Context context) {
        super(context, "sync");

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+ tb_name +" (nombre TEXT PRIMARY KEY, last TEXT)");
    }


    @SuppressLint("Range")
    @Override
    protected JSONObject cursorToJSON(Cursor res) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("nombre", res.getString(res.getColumnIndex("nombre")));
            obj.put("last", res.getString(res.getColumnIndex("last")));
        }catch (Exception e){
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    protected ContentValues caragarValues(JSONObject o) {
        ContentValues values = new ContentValues();
        try {
            values.put("nombre", o.getString("nombre"));
            values.put("last", o.getString("last"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return  values;
    }


    public void rellenarTabla(JSONArray tb){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        try{
          db.execSQL("DELETE FROM "+ tb_name);
          super.rellenarTabla(tb);
        }catch (SQLiteException e){
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    public JSONArray getAll()
    {
        return filter(null);
    }

    private boolean hayRegistros(String tb, SQLiteDatabase db){
        boolean hay = false;
       try {
            int count = count(db, "nombre='"+tb+"'");
            hay = count > 0;
        }catch (Exception e){
            e.printStackTrace();
        }
        return hay;
    }

    public void vaciar(){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            db.execSQL("DELETE FROM "+tb_name);
        }catch (SQLiteException e){
            this.onCreate(db);
        }
    }


    public boolean is_updatable(JSONObject obj) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean isUp = true;
        try {
            String date = obj.getString("last");
            if (date.equals("")) return true;
            String tb = obj.getString("nombre");
            isUp = !hayRegistros(tb, db);
            if (!isUp) {
                int count = count(db, String.format("nombre='%s' and last < '%s'", tb, date));
                isUp = count > 0;
            }
        } catch (Exception e) {
             e.printStackTrace();
        }

        return  isUp;
    }

    public void upTabla(String tb, String last) {
        try {
            SQLiteDatabase db =  this.getWritableDatabase();
            boolean hay = hayRegistros(tb, db);
            ContentValues v = new ContentValues();
            v.put("nombre", tb);
            v.put("last", last);
            if (!hay) {
                 db.insert(tb_name, null, v);
            }else{
                db.update(tb_name, v, "nombre = ?",  new String[]{tb});
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
