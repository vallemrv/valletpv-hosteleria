package com.valleapp.valletpv.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.valleapp.valletpv.interfaces.IBaseDatos;
import com.valleapp.valletpv.interfaces.IBaseSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by valle on 13/10/14.
 */
public class DBCuenta extends SQLiteOpenHelper  implements IBaseDatos, IBaseSocket {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "valletpv";

    public DBCuenta(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE IF NOT EXISTS cuenta " +
                "(ID INTEGER PRIMARY KEY, Estado TEXT, " +
                "Descripcion TEXT, descripcion_t TEXT, Precio DOUBLE, IDPedido INTEGER, " +
                "IDMesa INTEGER," +
                "IDArt INTEGER," +
                "nomMesa TEXT, IDZona TEXT," +
                "servido INTEGER )");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE  IF EXISTS cuenta");
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    @Override
    public JSONArray filter(String cWhere) {

        String strWhere = "";
        if (cWhere != null){
            strWhere = " WHERE "+ cWhere;
        }

        JSONArray lista  = execSql("SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total" +
                " FROM cuenta " + strWhere +
                " GROUP BY  IDArt,  Descripcion, Precio, Estado ORDER BY ID DESC");


        return lista;
    }

    public List<JSONObject> filterList(String cWhere) {
        List<JSONObject> lista = new ArrayList<>();
        try {
            String strWhere = "";
            if (cWhere != null){
                strWhere = " WHERE "+ cWhere;
            }
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res = db.rawQuery("SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total" +
                    " FROM cuenta " + strWhere +
                    " GROUP BY  IDArt, Descripcion, Precio, Estado ORDER BY ID DESC", null);
            res.moveToFirst();
            while (!res.isAfterLast()) {
                lista.add(cargarRegistro(res));
                res.moveToNext();
            }

        }catch (SQLiteException e){
            e.printStackTrace();
        }
        return lista;
    }


    private ContentValues caragarValues(JSONObject o){
        ContentValues values = new ContentValues();
        try {
            values.put("ID", o.getString("ID"));
            values.put("IDArt", o.getInt("IDArt"));
            values.put("Descripcion", o.getString("Descripcion"));
            values.put("descripcion_t", o.getString("descripcion_t"));
            values.put("Precio", o.getDouble("Precio"));
            values.put("IDMesa", o.getString("IDMesa"));
            values.put("IDZona", o.getString("IDZona"));
            values.put("nomMesa", o.getString("nomMesa"));
            values.put("IDPedido", o.getString("IDPedido"));
            values.put("Estado", o.getString("Estado"));
            values.put("servido", o.getString("servido"));
        }catch (Exception e){
            Log.d("CUENTA-CARGARVALUES",  e.getMessage());
        }
        return values;
    }

    public List<JSONObject> getAll(String id) {
        return filterList("IDMesa ="+id+" AND (estado = 'N' or estado ='P')" );
    }

    public double getTotal(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        double s = 0.0;
        try {
            Cursor cursor = db.rawQuery("SELECT SUM(Precio) AS TotalTicket " +
                    "FROM cuenta WHERE IDMesa=" + id+ " AND (estado = 'N' or estado ='P')", null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
                s = cursor.getDouble(0);
            }

        } catch (SQLiteException e) {
            e.printStackTrace();
        }


        return s;
    }

    public void cambiarCuenta(String id, String id1) {
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            ContentValues values = new ContentValues();
            values.put("IDMesa", id1);
            db.update("cuenta", values, "IDMesa="+id, null);
        }catch (SQLiteException e){
            e.printStackTrace();
        }

    }

    @SuppressLint("Range")
    JSONObject cargarRegistro(Cursor res){

        JSONObject obj = new JSONObject();
        try{
            obj.put("ID", res.getString(res.getColumnIndex("ID")));
            obj.put("Descripcion", res.getString(res.getColumnIndex("Descripcion")));
            obj.put("descripcion_t", res.getString(res.getColumnIndex("descripcion_t")));
            int index_can = res.getColumnIndex("Can");
            if (index_can >=  0) {
                obj.put("Can", res.getString(res.getColumnIndex("Can")));
                obj.put("CanCobro", 0);
                obj.put("Total", res.getString(res.getColumnIndex("Total")));
            }
            obj.put("Precio", res.getString(res.getColumnIndex("Precio")));
            obj.put("IDArt", res.getString(res.getColumnIndex("IDArt")));
            obj.put("Estado", res.getString(res.getColumnIndex("Estado")));
            obj.put("nomMesa", res.getString(res.getColumnIndex("nomMesa")));
            obj.put("IDPedido", res.getString(res.getColumnIndex("IDPedido")));
            obj.put("servido", res.getString(res.getColumnIndex("servido")));
            obj.put("IDZona", res.getString(res.getColumnIndex("IDZona")));
            obj.put("IDMesa", res.getString(res.getColumnIndex("IDMesa")));
        }catch (Exception e){
            e.printStackTrace();
        }
        return obj;
    }

    public JSONArray execSql(String sql) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery(sql, null);
        JSONArray ls = new JSONArray();
        res.moveToFirst();
        while (!res.isAfterLast()){
            ls.put(cargarRegistro(res));
            res.moveToNext();
        }

        return  ls;
    }

    private int count(SQLiteDatabase db, String cWhere){
        String w = "";
        if (cWhere != null){
            w = " WHERE "+cWhere;
        }
        Cursor mCount= db.rawQuery("select count(*) from cuenta "+ w, null);
        mCount.moveToFirst();
        return  mCount.getInt(0);
    }

    public void showDatos(String cWhere){
        SQLiteDatabase db = getReadableDatabase();
        String w = "";
        if (cWhere != null){
            w = " WHERE "+cWhere;
        }
        Cursor res= db.rawQuery("select * from cuenta "+ w, null);
        res.moveToFirst();
        while (!res.isAfterLast()){
            String dta = "";
            for (int i=0; i< res.getColumnCount(); i++) {
                dta += res.getColumnName(i)+ "="+ res.getString(i) + " - ";
            }
            Log.i("SHOWDATA", dta);
            res.moveToNext();
        }

    }

    @Override
    public void insert(JSONObject o) {
        SQLiteDatabase db = getWritableDatabase();
        try{
            String id = o.getString("ID");
            int count = count(db, "ID="+id);
            ContentValues values = caragarValues(o);
            synchronized (db) {
                if (count == 0) {
                    db.insert("cuenta", null, values);
                } else {
                    db.update("cuenta", values, "ID=?", new String[]{id});
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void update(JSONObject o) {
        SQLiteDatabase db = getWritableDatabase();
        try{
            String id = o.getString("ID");
            ContentValues values = caragarValues(o);
            db.update("cuenta", values, "ID=?", new String[]{id});
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rm(JSONObject o) {
        SQLiteDatabase db = getWritableDatabase();
        try{
            db.delete("cuenta", "ID=?", new String[]{o.getString("ID")});
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void rellenarTabla(JSONArray datos){
        // Gets the data repository in write mode
       SQLiteDatabase db = this.getWritableDatabase();
        try{
            db.execSQL("DELETE FROM cuenta");
            for(int i= 0; i < datos.length(); i++){
                insert(datos.getJSONObject(i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public void inicializar() {
        SQLiteDatabase db = this.getWritableDatabase();
        this.onCreate(db);
    }

    public void replaceMesa(JSONArray datos, String IDMesa){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            db.execSQL("DELETE FROM cuenta WHERE IDMesa=" + IDMesa);
            for(int i= 0; i < datos.length(); i++){
                insert(datos.getJSONObject(i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void addArt(int IDMesa,  JSONObject art){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
         // Insert the new row, returning the primary key value of the new row
        try {
            int can = art.getInt("Can");
            for(int i=0;i<can;i++) {
                ContentValues values = new ContentValues();
                values.put("IDArt", art.getInt("ID"));
                values.put("Descripcion", art.getString("Descripcion"));
                values.put("descripcion_t", art.getString("descripcion_t"));
                values.put("Precio", art.getDouble("Precio"));
                values.put("IDMesa", IDMesa);
                values.put("Estado", "N");
                db.insert("cuenta", null, values);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void eliminar(String IDMesa, JSONArray lsart){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            for (int i = 0; i < lsart.length(); i++){
                JSONObject art = lsart.getJSONObject(i);
                String sql = "DELETE FROM cuenta WHERE ID IN (SELECT ID FROM cuenta WHERE IDMesa="
                               + IDMesa + " AND IDArt=" + art.getString("IDArt")
                                        + " AND Descripcion='" + art.getString("Descripcion")+"'"
                                        + " AND Precio=" + art.getString("Precio")
                                        + " AND Estado='" + art.getString("Estado") + "' LIMIT "+art.getString("Can")+")";
                db.execSQL(sql);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void eliminar(String IDMesa){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
           db.execSQL("DELETE FROM cuenta WHERE IDMesa=" + IDMesa);
        }catch (SQLiteException e) {
            e.printStackTrace();
        }

    }

    public void aparcar(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            db.execSQL("UPDATE cuenta SET Estado='P' WHERE IDMesa="+id);
        }catch (SQLiteException e){
            e.printStackTrace();
        }

    }

    @SuppressLint("Range")
    public JSONArray getNuevos(String id) {

        JSONArray lista = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor res = db.rawQuery("SELECT *, COUNT(ID) AS Can FROM cuenta WHERE IDMesa=" + id +" AND Estado = 'N'"+
                    " Group by IDArt, Descripcion, Precio, Estado", null);
            res.moveToFirst();
            while (!res.isAfterLast()) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("Descripcion", res.getString(res.getColumnIndex("Descripcion")));
                    obj.put("descripcion_t", res.getString(res.getColumnIndex("descripcion_t")));
                    obj.put("Can", res.getString(res.getColumnIndex("Can")));
                    obj.put("Precio", res.getString(res.getColumnIndex("Precio")));
                    obj.put("IDArt", res.getString(res.getColumnIndex("IDArt")));
                    lista.put(obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                res.moveToNext();

            }

        }catch (SQLiteException e){
           e.printStackTrace();
        }

        return lista;
    }



}

