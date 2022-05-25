package com.valleapp.vallecom.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.valleapp.vallecom.interfaces.IBaseDatos;
import com.valleapp.vallecom.interfaces.IBaseSocket;

import org.json.JSONArray;
import org.json.JSONObject;

public class DBBase extends SQLiteOpenHelper implements IBaseDatos, IBaseSocket {


    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "valletpv";

    public DBBase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public void inicializar(){
        onCreate(getWritableDatabase());
    }


    @Override
    public JSONArray filter(String cWhere) {
        return null;
    }

    @Override
    public void rellenarTabla(JSONArray objs) {

    }


    @Override
    public void rm(JSONObject o) {

    }

    @Override
    public void insert(JSONObject o) {

    }

    @Override
    public void update(JSONObject o) {

    }
}
