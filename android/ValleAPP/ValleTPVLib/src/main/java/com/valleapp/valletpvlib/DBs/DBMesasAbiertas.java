package com.valleapp.valletpvlib.DBs;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

public class DBMesasAbiertas extends DBMesas {

    public DBMesasAbiertas(Context context) {
        super(context);
    }

    @Override
    public JSONArray filter(String cWhere) {
        if (cWhere!=null && !cWhere.equals("")){
            cWhere += " and abierta=1";
        }else{
            cWhere = "abierta=1";
        }
        return super.filter(cWhere);
    }

    @Override
    public void rellenarTabla(JSONArray objs) {
        SQLiteDatabase sqlDb = getWritableDatabase();
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("abierta", 0);
            initialValues.put("num", "0");//Cerramos todas las mesas
            sqlDb.update("mesas", initialValues, null, null);
            for (int i=0; i < objs.length(); i ++) {
                JSONObject o = objs.getJSONObject(i);
                initialValues = new ContentValues();
                String id = o.getString("ID");
                initialValues.put("abierta", 1);
                initialValues.put("num", o.getString("num"));
                sqlDb.update("mesas", initialValues,"ID=?", new String[]{id});
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @SuppressLint("Range")
    @Override
    protected JSONObject cursorToJSON(Cursor res) {
        JSONObject obj = new JSONObject();
        try {
            int num = res.getInt(res.getColumnIndex("num"));
            obj.put("abierta", res.getString(res.getColumnIndex("abierta")));
            obj.put("ID", res.getString(res.getColumnIndex("ID")));
            obj.put("num", num);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public void inicializar() {}

    @Override
    public void rm(JSONObject o) {

    }

    @Override
    public void insert(JSONObject o) {  }

    @Override
    public void update(JSONObject o) {
        try{
            String id;
            if (o.has("ID")){
                id = o.getString("ID");
            }else{
                id = o.getString("id");
            }
            SQLiteDatabase dbsql = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("abierta", o.getString("abierta"));
            values.put("num", o.getInt("num"));
            dbsql.update("mesas", values, "ID=?", new String[]{id});
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
