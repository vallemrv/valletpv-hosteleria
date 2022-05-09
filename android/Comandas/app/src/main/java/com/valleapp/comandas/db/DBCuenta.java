package com.valleapp.comandas.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by valle on 13/10/14.
 */
public class DBCuenta extends DBBase {


    public DBCuenta(Context context) {
        super(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS cuenta " +
                "(ID INTEGER PRIMARY KEY, Estado TEXT, " +
                "Descripcion TEXT, descripcion_t TEXT, Precio DOUBLE, IDPedido INTEGER, " +
                "IDMesa INTEGER, flag TEXT default ''," +
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
    public void rellenarTabla(JSONArray datos){
        // Gets the data repository in write mode
         SQLiteDatabase db = this.getWritableDatabase();
        try{
           db.execSQL("DELETE FROM cuenta");
        }catch (SQLiteException e){
            Log.i("ValleTPV", "Creando base de datos para pedidos");
            this.onCreate(db);
        }
        insertarRegistros(db, datos);
        db.close();
    }

    @Override
    public JSONArray filter(String cWhere) {
        JSONArray lista = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String strWhere = "";
            if (cWhere != null){
                strWhere = " WHERE "+ cWhere;
            }

            Cursor res = db.rawQuery("SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total" +
                    " FROM cuenta " + strWhere +
                    " GROUP BY  IDArt, Descripcion, Precio, Estado ORDER BY ID DESC", null);
            res.moveToFirst();
            while (!res.isAfterLast()) {
                lista.put(cargarRegistro(res));
                res.moveToNext();
            }
            res.close();

            db.close();
        }catch (SQLiteException e){
            db.close();
            db = this.getWritableDatabase();
            this.onCreate(db);
        }
        return lista;
    }



    public void actualizarMesa(JSONArray datos, String IDMesa){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            db.execSQL("DELETE FROM cuenta WHERE IDMesa=" + IDMesa);
        }catch (SQLiteException e){
            this.onCreate(db);
        }

        insertarRegistros(db, datos);
        db.close();
    }

    public List<JSONObject> getAll(String id)
    {
        return filterList("IDMesa ="+id+" AND (estado = 'N' or estado ='P')" );
    }

    public Double getTotal(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        double s = 0.0;
        try {
            Cursor cursor = db.rawQuery("SELECT SUM(Precio) AS TotalTicket " +
                    "FROM cuenta WHERE IDMesa=" + id, null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
                s = cursor.getDouble(0);
            }
            cursor.close();

        } catch (SQLiteException e) {
            db.close();
            db = this.getWritableDatabase();
            this.onCreate(db);
        }

        db.close();
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
        db.close();
    }


    public List<JSONObject> filterList(String cWhere) {
        List<JSONObject> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String strWhere = "";
            if (cWhere != null){
                strWhere = " WHERE "+ cWhere;
            }

            Cursor res = db.rawQuery("SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total" +
                    " FROM cuenta " + strWhere +
                    " GROUP BY  IDArt, Descripcion, Precio, Estado ORDER BY ID DESC", null);
            res.moveToFirst();
            while (!res.isAfterLast()) {
                 lista.add(cargarRegistro(res));
                 res.moveToNext();
            }
            res.close();

            db.close();
        }catch (SQLiteException e){
            e.printStackTrace();
            db.close();
            db = this.getWritableDatabase();
            this.onCreate(db);
        }
        return lista;
    }

    @SuppressLint("Range")
    JSONObject cargarRegistro(Cursor res){

        JSONObject obj = new JSONObject();
        try{
            obj.put("ID", res.getString(res.getColumnIndex("ID")));
            obj.put("Descripcion", res.getString(res.getColumnIndex("Descripcion")));
            obj.put("descripcion_t", res.getString(res.getColumnIndex("descripcion_t")));
            obj.put("Can", res.getString(res.getColumnIndex("Can")));
            obj.put("CanCobro", 0);
            obj.put("Precio", res.getString(res.getColumnIndex("Precio")));
            obj.put("Total", res.getString(res.getColumnIndex("Total")));
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

    void insertarRegistros(SQLiteDatabase db, JSONArray datos) {
        // Insert the new row, returning the primary key value of the new row
        for (int i= 0 ; i<datos.length();i++) {
            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            try {
                String id = datos.getJSONObject(i).getString("ID");
                db.delete("cuenta","ID = ?", new String[]{id});
                int idMesa = datos.getJSONObject(i).getInt("IDMesa");
                values.put("ID", id);
                values.put("IDArt", datos.getJSONObject(i).getInt("IDArt"));
                values.put("Descripcion", datos.getJSONObject(i).getString("Descripcion"));
                values.put("descripcion_t", datos.getJSONObject(i).getString("descripcion_t"));
                values.put("Precio", datos.getJSONObject(i).getDouble("Precio"));
                values.put("IDMesa", idMesa);
                values.put("Estado", datos.getJSONObject(i).getString("Estado"));
                values.put("nomMesa", datos.getJSONObject(i).getString("nomMesa"));
                values.put("IDZona", datos.getJSONObject(i).getString("IDZona"));
                values.put("servido", datos.getJSONObject(i).getString("servido"));
                values.put("IDPedido", datos.getJSONObject(i).getString("IDPedido"));
                db.insert("cuenta", null, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void atualizarDatos(JSONArray jsonArray) {
        SQLiteDatabase db = getWritableDatabase();
        insertarRegistros(db, jsonArray);
    }

    public JSONArray filterByPedidos(String cWhere) {
        String strWhere = "";
        if (cWhere != null){
            strWhere = " WHERE "+cWhere;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery("SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total FROM cuenta " + strWhere +
                " GROUP BY  IDArt, Descripcion, Precio, Estado, IDPedido ORDER BY ID DESC", null);
        JSONArray ls = new JSONArray();
        res.moveToFirst();
        while (!res.isAfterLast()){
            ls.put(cargarRegistro(res));
            res.moveToNext();
        }

        db.close();
        return  ls;
    }

    public void moverLinea(JSONObject m, JSONObject linea) {
        try {
            SQLiteDatabase db = getWritableDatabase();

            boolean abierta = m.getString("abierta").equals("1");
            String idm = m.getString("ID");

            String idz = m.getString("IDZona");
            String nomMesa = m.getString("Nombre");
            String idlinea = linea.getString("ID");

            //camibar la linea de mesa.
            ContentValues p = new ContentValues();
            p.put("IDMesa", idm);
            p.put("IDZona", idz);
            p.put("nomMesa", nomMesa);
            db.update("cuenta", p, "ID = ?", new String[]{idlinea});

            if (!abierta){
                p = new ContentValues();
                p.put("abierta", "1");
                db.update("mesas", p, "ID = ?", new String[]{idm});
            }


            //Ver si hay que cerrar la mesas
            String idm2 = linea.getString("IDMesa");
            Cursor c = db.rawQuery("Select count(*) from cuenta where IDMesa = ?",
                    new String[]{idm2});

            c.moveToFirst();
            int count= c.getInt(0);
            if (count <= 0){
                p = new ContentValues();
                p.put("abierta", "0");
                db.update("mesas", p, "ID = ?", new String[]{idm2});
            }



        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void artServido(JSONObject obj) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues p = new ContentValues();
            p.put("servido", 1);
            db.update("cuenta", p, "IDArt = ? AND Descripcion = ? AND Precio = ? AND IDPedido = ? ",
                    new String[]{obj.getString("IDArt"), obj.getString("Descripcion"),
                            obj.getString("Precio"), obj.getString("IDPedido")});
        }catch (Exception e){
           e.printStackTrace();
        }
        db.close();
    }

    @SuppressLint("Range")
    public ArrayList<JSONObject> getPedidosChoices(String idMesa) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery("SELECT *, COUNT(ID) AS Can, SUM(PRECIO) AS Total FROM cuenta  WHERE estado ='P' AND IDMesa=" + idMesa +
                " GROUP BY  IDArt, Descripcion, Precio, Estado, IDPedido ORDER BY IDPedido", null);
        ArrayList<JSONObject>  ls = new ArrayList<JSONObject> ();
        res.moveToFirst();
        int max = 3;
        int count = 0;
        int id_aux = -1;
        String subtilte = "";
        JSONObject o = null;
        try {
        while (!res.isAfterLast()){

                int idPedido = res.getInt(res.getColumnIndex("IDPedido"));
                String can = res.getString(res.getColumnIndex("Can"));
                String nombre =  res.getString(res.getColumnIndex("Descripcion"));
                if (id_aux != idPedido) {
                    id_aux = idPedido;
                    if (o != null && subtilte!= ""){
                        subtilte += ", etc..";
                        o.put("subtitle", subtilte);
                    }
                    o = new JSONObject();
                    o.put("IDPedido", id_aux);
                    ls.add(o);
                    count = 0;
                    subtilte = "";
                }
                if (count < max){
                    if (subtilte != "") subtilte += ", ";
                    subtilte += can+ " "+ nombre;
                    count++;
                }
                res.moveToNext();

            }
            if (o != null && subtilte!= ""){
                subtilte += ", etc..";
                o.put("subtitle", subtilte);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        res.close();
        db.close();
        return  ls;
    }
}

