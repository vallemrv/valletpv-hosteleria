package com.valleapp.vallecom.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.valleapp.vallecom.interfaces.IBaseDatos;
import com.valleapp.vallecom.interfaces.IBaseSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public abstract class DBBase extends SQLiteOpenHelper implements IBaseDatos, IBaseSocket {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "valletpv";
    protected final String tb_name;

    public DBBase(@Nullable Context context, String tb_name) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.tb_name = tb_name;
    }

    public abstract void onCreate(SQLiteDatabase sqLiteDatabase);

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over

        onCreate(db);
    }

    @Override
    public void inicializar(){
        onCreate(getWritableDatabase());
    }

    protected abstract JSONObject cursorToJSON(Cursor res);
    protected abstract ContentValues caragarValues(JSONObject o);

    @Override
    public JSONArray filter(String cWhere) {
        SQLiteDatabase db = this.getReadableDatabase();
        String w = "";
        if (cWhere != null) {
            w = " WHERE " + cWhere;
        }
        Cursor res = db.rawQuery("select * from " + tb_name + " " + w, null);
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
    public void rellenarTabla(JSONArray objs) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM "+tb_name);
        for (int i= 0 ; i < objs.length(); i++){
            // Create a new map of values, where column names are the keys
            try {
                insert(objs.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void insert(JSONObject o) {
        SQLiteDatabase db = getWritableDatabase();
        try{
               String id;
                if (o.has("ID")){
                    id = o.getString("ID");
                }else{
                    id = o.getString("id");
                }
                ContentValues values = caragarValues(o);

                if(Objects.equals(tb_name, "cuenta")){
                    db.delete(tb_name, "estado='N' and IDMesa = ?",
                            new String[]{values.getAsString("IDMesa")});
                }

                int count = count(db, "ID=" + id);

                if (count == 0) {
                    db.insert(tb_name, null, values);
                } else {
                    db.update(tb_name, values, "ID=?", new String[]{id});
                }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void update(JSONObject o) {
        insert(o);
    }



    @Override
    public void rm(JSONObject o) {
        SQLiteDatabase db = getWritableDatabase();
        try{
            if(o.has("ID")) {
                db.delete(tb_name, "ID=?", new String[]{o.getString("ID")});
            }else {
                db.delete(tb_name, "ID=?", new String[]{o.getString("id")});
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    protected int count(SQLiteDatabase db, String cWhere){
        String w = "";
        if (cWhere != null){
            w = " WHERE "+cWhere;
        }
        @SuppressLint("Recycle") Cursor mCount= db.rawQuery("select count(*) from  "+ tb_name +" "+ w, null);
        mCount.moveToFirst();
        return  mCount.getInt(0);
    }

    public void showDatos(String cWhere){
        SQLiteDatabase db = getReadableDatabase();
        String w = "";
        if (cWhere != null){
            w = " WHERE "+cWhere;
        }
        @SuppressLint("Recycle") Cursor res= db.rawQuery("select * from  "+ tb_name +" "+ w, null);
        res.moveToFirst();
        while (!res.isAfterLast()){
            StringBuilder dta = new StringBuilder();
            for (int i=0; i< res.getColumnCount(); i++) {
                dta.append(res.getColumnName(i)).append("=").append(res.getString(i)).append(" - ");
            }
            Log.i("SHOWDATA", dta.toString());
            res.moveToNext();
        }

    }


}
