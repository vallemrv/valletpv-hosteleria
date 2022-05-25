package com.valleapp.valleCOM.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by valle on 13/10/14.
 */
public class DbTbUpdates extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "valletpv";


    public String tb_name = "sync";


    public DbTbUpdates(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        onCreate(this.getWritableDatabase());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+ tb_name +" (nombre TEXT PRIMARY KEY, last TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + tb_name);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }



    public void rellenarTabla(JSONArray tb){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        try{
          db.execSQL("DELETE FROM "+ tb_name);
        }catch (SQLiteException e){
            this.onCreate(db);
        }
       // Insert the new row, returning the primary key value of the new row
        for (int i= 0 ; i < tb.length(); i++){
            // Create a new map of values, where column names are the keys
            try {
                 ContentValues values = new ContentValues();
                 values.put("nombre", tb.getJSONObject(i).getInt("nombre"));
                 values.put("last", tb.getJSONObject(i).getString("last"));
                 db.insert(tb_name, null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    @SuppressLint("Range")
    public JSONArray getAll()
    {
        JSONArray ls = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+ tb_name, null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            try{
                JSONObject obj = new JSONObject();
                obj.put("nombre", res.getString(res.getColumnIndex("nombre")));
                obj.put("last", res.getString(res.getColumnIndex("last")));
                ls.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            res.moveToNext();

        }

        return ls;
    }

    private boolean hayRegistros(String tb, SQLiteDatabase db){
        boolean hay = false;
       try {
            Cursor res = db.rawQuery("select count(*) from " + tb_name + " WHERE nombre = ? ", new String[]{tb});
            res.moveToFirst();
            int count = res.getInt(0);
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
        boolean isUp = true;
        try {

            String date = obj.getString("last");
            if (date.equals("")) return true;
            String tb = obj.getString("nombre");
            SQLiteDatabase db = this.getReadableDatabase();
            isUp = !hayRegistros(tb, db);

            if (!isUp) {
                Cursor res = db.rawQuery("select count(*) from " + tb_name + " WHERE nombre = ? AND last < ?", new String[]{tb, date});
                res.moveToFirst();
                int count = res.getInt(0);
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

    public void setLast(String tb, String last) {
        SQLiteDatabase db;
        try{
            db = getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put("last", last);
            db.update(tb_name, v, "nombre = ? ", new String[]{tb});
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
