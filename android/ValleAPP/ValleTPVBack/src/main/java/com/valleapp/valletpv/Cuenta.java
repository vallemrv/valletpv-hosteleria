package com.valleapp.valletpv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.valleapp.valletpv.adaptadoresDatos.AdaptadorTicket;
import com.valleapp.valletpv.cashlogyActivitis.CobroCashlogyActivity;

import com.valleapp.valletpv.dlg.DlgCobrar;
import com.valleapp.valletpv.dlg.DlgPedirAutorizacion;
import com.valleapp.valletpv.dlg.DlgSepararTicket;
import com.valleapp.valletpv.dlg.DlgVarios;
import com.valleapp.valletpv.interfaces.IAutoFinish;
import com.valleapp.valletpv.interfaces.IControladorAutorizaciones;
import com.valleapp.valletpv.interfaces.IControladorCuenta;
import com.valleapp.valletpvlib.DBs.DBCamareros;
import com.valleapp.valletpvlib.DBs.DBCuenta;
import com.valleapp.valletpvlib.DBs.DBMesas;
import com.valleapp.valletpvlib.DBs.DBSecciones;
import com.valleapp.valletpvlib.DBs.DBSubTeclas;
import com.valleapp.valletpvlib.DBs.DBTeclas;
import com.valleapp.valletpvlib.tools.JSON;
import com.valleapp.valletpvlib.tools.ServicioCom;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class Cuenta extends Activity implements TextWatcher, IControladorCuenta, IControladorAutorizaciones, IAutoFinish {

    private String server = "";
    DBSecciones dbSecciones;
    DBTeclas dbTeclas;
    DBCuenta dbCuenta;
    DBMesas dbMesas;
    DBSubTeclas dbSubteclas;

    JSONObject cam = null;
    JSONObject mesa = null;
    JSONObject artSel = null;

    List<JSONObject> lineas = null;
    JSONArray lsartresul = null;

    Double totalMesa = 0.00;

    String tipo = "";
    String sec = "";
    int cantidad = 1;
    Boolean reset = true;
    Boolean stop = false;
    Timer timerAutoCancel = null;

    final long autoCancel = 10000;

    ServicioCom myServicio;

    final Context cx = this;

    private DlgCobrar dlgCobrar;
    private boolean can_refresh = true;

    private final ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                myServicio = ((ServicioCom.MyBinder) iBinder).getService();
                myServicio.setExHandler("lineaspedido", handlerHttp);
                myServicio.setExHandler("teclas", handlerSeccionesTeclas);
                myServicio.setExHandler("secciones", handlerSeccionesTeclas);
                dbCuenta = (DBCuenta) myServicio.getDb("lineaspedido");
                dbMesas = (DBMesas) myServicio.getDb("mesas");
                dbSecciones = (DBSecciones) myServicio.getDb("secciones");
                dbTeclas = (DBTeclas) myServicio.getDb("teclas");
                dbSubteclas = (DBSubTeclas) myServicio.getDb("subteclas");
                rellenarSecciones();
                rellenarTicket();


                if (tipo.equals("c")) {
                    Timer t = new Timer();
                    findViewById(R.id.loading).setVisibility(View.VISIBLE);
                    t.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handlerMostrarCobrar.sendEmptyMessage(0);
                        }
                    }, 1000);

                }
                myServicio.setMesaAbierta(mesa);
                get_cuenta();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler handlerMostrarCobrar = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                final String id_mesa = mesa.getString("ID");
                mostrarCobrar(dbCuenta.filterGroup("IDMesa=" + id_mesa), totalMesa);
                findViewById(R.id.loading).setVisibility(View.GONE);
            }catch (Exception e ){
                e.printStackTrace();
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler mostrarBusqueda= new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            rellenarArticulos(lsartresul);
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler handlerSeccionesTeclas= new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            rellenarSecciones();
        }
    };


    @SuppressLint("HandlerLeak")
    private final Handler handlerHttp = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                reset=true;
                String res = msg.getData().getString("RESPONSE");
                if (res != null) {
                    JSONArray datos = new JSONArray(res);
                    synchronized (dbCuenta) {
                       dbCuenta.replaceMesa(datos, mesa.getString("ID"));
                       rellenarTicket();
                   }
                }else{
                    if (can_refresh) rellenarTicket();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };



    //Inicializacion de estados y vista
    private void rellenarSecciones() {

        try{

            JSONArray lssec = dbSecciones.getAll();

            if(lssec.length()>0){

                LinearLayout ll = findViewById(R.id.pneSecciones);
                ll.removeAllViews();

                DisplayMetrics metrics = getResources().getDisplayMetrics();

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,Math.round(metrics.density * 100));

                params.setMargins(5,5,5,5);


                for (int i = 0; i < lssec.length(); i++) {
                    JSONObject  z =  lssec.getJSONObject(i);

                    if(sec.equals("") && i==0) sec =  z.getString("ID");

                    Button btn = new Button(cx);
                    btn.setId(z.getInt("ID"));
                    btn.setSingleLine(false);
                    btn.setText(z.getString("Nombre"));
                    btn.setTag(z.getString("ID"));
                    btn.setTextSize(16);
                    String rgb_str =  z.getString("RGB");
                    if (rgb_str.equals("")){
                        btn.setBackgroundResource(R.drawable.bg_pink);
                    }else {
                        String[] rgb = rgb_str.trim().split(",");
                         btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));
                    }
                    btn.setOnClickListener(view -> {
                        setEstadoAutoFinish(true, false);
                        sec =  view.getTag().toString();
                        try {
                            JSONArray  lsart = dbTeclas.getAll(sec, mesa.getInt("Tarifa"));
                            rellenarArticulos(lsart);
                            lsartresul = lsart;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });

                    btn.setOnLongClickListener(view -> {
                        asociarBotonera(view);
                        return false;
                    });
                    ll.addView(btn, params);
                }

                JSONArray lsart = dbTeclas.getAll(sec,mesa.getInt("Tarifa"));
                rellenarArticulos(lsart);
                lsartresul = lsart;


            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void cargarPreferencias() {
        JSON json = new JSON();
        try {
            JSONObject pref = json.deserializar("preferencias.dat", this);
            if(!pref.isNull("sec")) {
                sec = pref.getString("sec");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void get_cuenta(){
        if(myServicio!=null & mesa != null) {
            try {
                myServicio.get_cuenta(handlerHttp, mesa.getString("ID"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //Mostrar datos de articulos y ticket
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void rellenarArticulos(JSONArray lsart) {
        try {

            if(lsart.length()>0){

                TableLayout ll = findViewById(R.id.pneArt);
                ll.removeAllViews();

                DisplayMetrics metrics = getResources().getDisplayMetrics();

                TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,Math.round(metrics.density * 100));


                LinearLayout.LayoutParams rowparams = new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT);

                rowparams.setMargins(5,5,5,5);
                rowparams.weight = 1;

                LinearLayout row = new LinearLayout(cx);
                row.setOrientation(LinearLayout.HORIZONTAL);


                ll.addView(row, params);


                for (int i = 0; i < lsart.length(); i++) {

                    final JSONObject  m =  lsart.getJSONObject(i);

                    Button btn = new Button(cx);

                    btn.setId(i);
                    btn.setTag(m);


                    if (m.has("RGB")){
                        btn.setText(m.getString("Nombre")+"\n"+String.format("%01.2f €",m.getDouble("Precio")));
                        String[] rgb = m.getString("RGB").split(",");
                        btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));

                        btn.setOnClickListener(view -> {
                            try {
                                artSel = (JSONObject) view.getTag();
                                artSel = new JSONObject(artSel.toString());
                                artSel.put("Can", cantidad);
                                artSel.put("Descripcion", componerDescripcion(artSel, "descripcion_r"));
                                artSel.put("descripcion_t", componerDescripcion(artSel, "descripcion_t"));
                                if (artSel.getString("tipo").equals("SP")) {
                                    pedirArt(artSel);
                                }else{
                                    rellenarArticulos(dbSubteclas.getAll(artSel.getString("ID")));
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        });
                    }else{
                        final Double precio = this.artSel.getDouble("Precio")+ m.getDouble("Incremento");
                        btn.setText(m.getString("Nombre")+"\n"+String.format("%01.2f €",precio));

                        btn.setBackgroundResource(R.drawable.bg_pink);
                        btn.setOnClickListener(view -> {
                            try {
                                JSONObject sub = (JSONObject) view.getTag();
                                Intent it = getIntent();
                                String des = sub.getString("descripcion_r");
                                if (!des.equals("null") && !des.isEmpty()){
                                    artSel.put("Descripcion", des);
                                }else{
                                    String nom = artSel.getString("Descripcion");
                                    String subnom = sub.getString("Nombre");
                                    artSel.put("Descripcion", nom+" "+subnom);

                                }
                                des = sub.getString("descripcion_t");
                                if (!des.equals("null") && !des.isEmpty()){
                                    artSel.put("descripcion_t", des);
                                }else if(artSel.getString("descripcion_t").equals(artSel.getString("Nombre"))){
                                    String nom = artSel.getString("descripcion_t");
                                    String subnom = sub.getString("Nombre");
                                    artSel.put("descripcion_t", nom+" "+subnom);
                                }
                                artSel.put("Precio", precio);
                                it.putExtra("art", artSel.toString());
                                setResult(RESULT_OK, it);
                                pedirArt(artSel);
                                rellenarArticulos(lsartresul);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        });
                    }
                    row.addView(btn, rowparams);

                    if (((i+1) % 5) == 0) {
                        row = new LinearLayout(cx);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        row.setMinimumHeight(130);

                        ll.addView(row, params);
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private String componerDescripcion(JSONObject o, String descipcion){
        String aux = "";
        try {
            String des = o.getString(descipcion);
            if (!des.equals("null") && !des.isEmpty()) {
                aux = des;
            }else{
                aux = o.getString("Nombre");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return  aux;
    }



    @SuppressLint("DefaultLocale")
    private void rellenarTicket() {
        try {
            synchronized (dbCuenta) {
                resetCantidad();
                lineas = dbCuenta.getAll(mesa.getString("ID"));
                totalMesa = dbCuenta.getTotal(mesa.getString("ID"));

                if (dlgCobrar != null){
                    dlgCobrar.dismiss();
                    Timer t = new Timer();
                    findViewById(R.id.loading).setVisibility(View.VISIBLE);
                    t.schedule(new TimerTask() {
                        @Override
                        public void run() {
                           handlerMostrarCobrar.sendEmptyMessage(0);
                         }
                    }, 1000);
                }

                TextView l = findViewById(R.id.lblPrecio);
                ListView lst = findViewById(R.id.lstCamareros);
                l.setText(String.format("%01.2f €", totalMesa));
                lst.setAdapter(new AdaptadorTicket(cx, (ArrayList<JSONObject>) lineas, this));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //Dialogos
    public void mostrarVarios(View v) {
        setEstadoAutoFinish(true, true);
        DlgVarios dlg= new DlgVarios(this, this);
        dlg.show();
    }

    public void mostrarSeparados(View v) {
        // TODO Auto-generated method stub
        if( totalMesa > 0) {
            try {
                setEstadoAutoFinish(true, true);
                aparcar(mesa.getString("ID"), dbCuenta.getNuevos(mesa.getString("ID")));
                lineas = dbCuenta.getAll(mesa.getString("ID"));
                DlgSepararTicket dlg = new DlgSepararTicket(this,this);
                dlg.setTitle("Separar ticket " + mesa.getString("Nombre"));
                dlg.setLineasTicket(lineas);
                dlg.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //Acciones con el servidor
    public void preImprimir(View v){
        try{
            setEstadoAutoFinish(true, false);
            aparcar(mesa.getString("ID"), dbCuenta.getNuevos(mesa.getString("ID")));
            lineas = dbCuenta.getAll(mesa.getString("ID"));

            if(totalMesa>0) {
                ContentValues p = new ContentValues();
                p.put("idm", mesa.getString("ID"));
                if(myServicio!=null) myServicio.preImprimir(p);
                dbMesas.marcarRojo(mesa.getString("ID"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void abrirCajon(View v){
        setEstadoAutoFinish(true,false);
        DBCamareros dbCamareros = (DBCamareros) myServicio.getDb("camareros");
        assert dbCamareros != null;
        if(!dbCamareros.getConPermiso("abrir_cajon").isEmpty()) {
            try {
                JSONObject p = new JSONObject();
                p.put("idc", cam.getString("ID"));
                DlgPedirAutorizacion dlg = new DlgPedirAutorizacion(cx, this,
                        dbCamareros, this,
                        p, "abrir_cajon");
                dlg.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            if (myServicio != null) myServicio.abrirCajon();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) { // Este es el código que usamos en startActivityForResult
            if (resultCode == Activity.RESULT_OK) {
                // Cobro exitoso
                double totalIngresado = data.getDoubleExtra("totalIngresado", 0.0);

                // Recibir los datos adicionales
                double totalMesa = data.getDoubleExtra("totalMesa", 0.0);
                String lineasString = data.getStringExtra("lineas");

                // Convertir lineasString de vuelta a JSONArray si lo necesitas
                JSONArray lst = null;
                try {
                    lst = new JSONArray(lineasString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                cobrar(lst, totalMesa, totalIngresado);

                Toast.makeText(this, "Cobro realizado con éxito", Toast.LENGTH_LONG).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Cobro cancelado
                Toast.makeText(this, "Cobro cancelado", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void cobrarMesa(View v) {
        try {
            aparcar(mesa.getString("ID"), dbCuenta.getNuevos(mesa.getString("ID")));
            JSONArray l = dbCuenta.filterGroup("IDMesa=" + mesa.getString("ID"));
            mostrarCobrar(l, totalMesa);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void aparcar(String idm, JSONArray nuevos) throws JSONException {
        if(nuevos.length()>0) {
            ContentValues p = new ContentValues();
            p.put("idm", idm);
            p.put("idc", cam.getString("ID"));
            p.put("pedido", nuevos.toString());
            if (myServicio != null) {
                myServicio.nuevoPedido(p);
                dbMesas.abrirMesa(idm);
                can_refresh = false;
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        can_refresh = true;
                    }
                }, 2000);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void clickCantidad(View v){
        setEstadoAutoFinish(true, false);
        cantidad = Integer.parseInt(((Button) v).getText().toString());
        TextView lbl = findViewById(R.id.lblCantida);
        lbl.setText("Cantidad "+cantidad);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void pedirArt(JSONObject art) {
        try {
            setEstadoAutoFinish(true, false);
            dbCuenta.addArt(mesa.getInt("ID"), art);
            rellenarTicket();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);
        cargarPreferencias();
        EditText bus = findViewById(R.id.txtBuscar);
        bus.addTextChangedListener(this);
        try {
           server = Objects.requireNonNull(getIntent().getExtras()).getString("url");
           cam = new JSONObject(Objects.requireNonNull(getIntent().getExtras().getString("cam")));
           mesa = new JSONObject(Objects.requireNonNull(getIntent().getExtras().getString("mesa")));
           tipo = getIntent().getExtras().getString("op");
           TextView title = findViewById(R.id.txtTitulo);
           title.setText(cam.getString("nombre") +" "+
                   cam.getString("apellidos")+  " -- "+mesa.getString("Nombre"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {  }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        reset = true;
        if(charSequence.length()>0) {
            try {
                final String str = charSequence.toString();
                final String t =  mesa.getString("Tarifa");
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    lsartresul = dbTeclas.findLike(str, t);
                    mostrarBusqueda.sendEmptyMessage(1);
                 }).start();
             } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) { }

    @Override
    protected void onPause() {
        stop = true;
        try{
            String idm = mesa.getString("ID");
            aparcar(idm, dbCuenta.getNuevos(idm));
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(timerAutoCancel!=null) {
            timerAutoCancel.cancel();
            timerAutoCancel=null;
        }
        myServicio.setMesaAbierta(null);
        unbindService(mConexion);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        try {
             stop = false;
             if(timerAutoCancel==null) {
                 timerAutoCancel = new Timer();
                 timerAutoCancel.schedule(new TimerTask() {
                     @Override
                     public void run() {
                         if (!stop) {
                             if (!reset) {
                                 finish();
                             } else reset = false;
                         }
                     }
                 }, 5000, autoCancel);
             }

            Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
            intent.putExtra("url", server);
            bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
            findViewById(R.id.loading).setVisibility(View.GONE);

        } catch (Exception e) {
            e.printStackTrace();
        }
       super.onResume();
    }

    @Override
    public void setEstadoAutoFinish(boolean r, boolean s) {
        tipo = "m";
        reset = r;
        stop = s;
    }

    public void mostrarCobrar(JSONArray lsart, Double totalCobro) {
        if(totalCobro>0) {
            try {
                setEstadoAutoFinish(true, true);
                if (dlgCobrar != null) dlgCobrar.dismiss();
                dlgCobrar = new DlgCobrar(this, this, myServicio.usaCashlogy());
                dlgCobrar.setTitle("Cobrar " + mesa.getString("Nombre"));
                dlgCobrar.setDatos(lsart, totalCobro);
                dlgCobrar.setOnDismissListener(dialogInterface -> dlgCobrar = null);
                dlgCobrar.show();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void cobrarConCashlogy(JSONArray lsart, Double totalCobro) {
        // Crear un Intent para iniciar la Activity de Cashlogy
        Intent intent = new Intent(this, CobroCashlogyActivity.class);

        // Pasar los datos necesarios a la Activity de Cashlogy
        intent.putExtra("totalMesa", totalCobro);
        intent.putExtra("lineas", lsart.toString());

        // Iniciar la Activity y esperar un resultado (usando el código 1 para identificarla)
        startActivityForResult(intent, 1);
    }



    @Override
    public void cobrar(JSONArray lsart, Double totalCobro, Double entrega) {

        try {
            setEstadoAutoFinish(true, true);
            if (dlgCobrar != null){
                dlgCobrar.dismiss();
                dlgCobrar = null;
            }
            DBCamareros dbCamareros = (DBCamareros) myServicio.getDb("camareros");
            assert dbCamareros != null;
            if(!dbCamareros.getConPermiso("cobrar_ticket").isEmpty()) {
                JSONObject p = new JSONObject();
                p.put("idm", mesa.getString("ID"));
                p.put("idc", cam.getString("ID"));
                p.put("entrega", Double.toString(entrega));
                p.put("art", lsart.toString());
                DlgPedirAutorizacion dlg = new DlgPedirAutorizacion(cx, this,
                        dbCamareros, this,
                        p, "cobrar_ticket");
                dlg.show();
            }else{
                ContentValues p = new ContentValues();
                p.put("idm", mesa.getString("ID"));
                p.put("idc", cam.getString("ID"));
                p.put("entrega", Double.toString(entrega));
                p.put("art", lsart.toString());
                dbCuenta.eliminar(mesa.getString("ID"), lsart);
                if (myServicio != null) {
                    myServicio.cobrarCuenta(p);
                    if (totalCobro - totalMesa == 0) {
                        dbMesas.cerrarMesa(mesa.getString("ID"));
                        finish();
                    } else {
                        rellenarTicket();
                    }
                    sendMessageMesaCobrada(entrega, entrega - totalCobro);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @SuppressLint("SetTextI18n")
    @Override
    public void clickMostrarBorrar(final JSONObject obj) {

            setEstadoAutoFinish(true,true);

            final Dialog dlg = new Dialog(cx);
            dlg.setContentView(R.layout.borrar_art);

            dlg.setOnCancelListener(dialogInterface -> setEstadoAutoFinish(true,false));

            dlg.setTitle("Borrar articulos");
            final EditText motivo = dlg.findViewById(R.id.txtMotivo);
            final Button error =  dlg.findViewById(R.id.btnError);
            final Button simpa =  dlg.findViewById(R.id.btnSimpa);
            final Button inv = dlg.findViewById(R.id.btnInv);
            final ImageButton ok =  dlg.findViewById(R.id.btn_ok);
            final ImageButton edit =  dlg.findViewById(R.id.btnEdit);
            final ImageButton exit =  dlg.findViewById(R.id.btn_salir_monedas);

            final LinearLayout pneEdit =  dlg.findViewById(R.id.pneEditarMotivo);
            final TextView txtInfo = dlg.findViewById(R.id.txt_info_borrar);
            final JSONObject art;
            try {
                art = new JSONObject(obj.toString());
                int canArt = art.getInt("Can");
                if (cantidad > canArt) cantidad = canArt;
                art.put("Can", cantidad);
                txtInfo.setText("Borrar " + cantidad + " "+art.getString("descripcion_t"));
                resetCantidad();

                pneEdit.setVisibility(View.GONE);

                exit.setOnClickListener(view -> dlg.cancel());

                edit.setOnClickListener(view -> {
                    if(pneEdit.getVisibility() == View.VISIBLE)  pneEdit.setVisibility(View.GONE);
                    else pneEdit.setVisibility(View.VISIBLE);
                });

                ok.setOnClickListener(view -> {
                    if (motivo.getText().length() > 0) {
                        borrarLinea(art, motivo.getText().toString());
                        dlg.cancel();
                    }
                });

                error.setOnClickListener(view -> {
                    borrarLinea(art, error.getText().toString());
                    dlg.cancel();


                });

                simpa.setOnClickListener(view -> {
                    borrarLinea(art, simpa.getText().toString());
                    dlg.cancel();
                });

                inv.setOnClickListener(view -> {
                    borrarLinea(art, inv.getText().toString());
                    dlg.cancel();
               });

            }catch (Exception e){
                e.printStackTrace();
            }

            dlg.show();


    }

    @Override
    public void borrarArticulo(JSONObject art) throws JSONException {
         reset = true;
         art.put("Can",1);
         dbCuenta.eliminar(mesa.getString("ID"), new JSONArray().put(art));
         rellenarTicket();
    }

    @Override
    public void pedirAutorizacion(ContentValues params) {
        myServicio.pedirAutorizacion(params);
    }

    @Override
    public void pedirAutorizacion(String id) {

    }

    // Utilidades
    @SuppressLint("SetTextI18n")
    public void resetCantidad(){
        cantidad = 1;
        TextView lbl = findViewById(R.id.lblCantida);
        lbl.setText("Cantidad "+ cantidad);
    }

    private void asociarBotonera(View view) {
        reset = true;
        JSON json = new JSON();
        try {
            JSONObject pref = json.deserializar("preferencias.dat", this);
            pref.put("sec",Integer.toString(view.getId()));
            json.serializar("preferencias.dat", pref, cx);
            Toast toast= Toast.makeText(getApplicationContext(),
                    "Asocicion realizada", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 200);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void borrarLinea(final JSONObject art, String motivo){
        try{
            reset = true;
            if (myServicio != null){
                DBCamareros dbCamareros = (DBCamareros) myServicio.getDb("camareros");
                assert dbCamareros != null;
                if(!dbCamareros.getConPermiso("borrar_linea").isEmpty()) {
                    JSONObject p = new JSONObject();
                    p.put("idm",  mesa.getString("ID"));
                    p.put("Precio", art.getString("Precio"));
                    p.put("idArt", art.getString("IDArt"));
                    p.put("can", art.getString("Can"));
                    p.put("idc", cam.getString("ID"));
                    p.put("motivo", motivo);
                    p.put("Estado", art.getString("Estado"));
                    p.put("Descripcion", art.getString("Descripcion"));
                    DlgPedirAutorizacion dlg = new DlgPedirAutorizacion(cx, this,
                            dbCamareros, this,
                            p, "borrar_linea");
                    dlg.show();
                }else{
                    ContentValues p = new ContentValues();
                    p.put("idm",  mesa.getString("ID"));
                    p.put("Precio", art.getString("Precio"));
                    p.put("idArt", art.getString("IDArt"));
                    p.put("can", art.getString("Can"));
                    p.put("idc", cam.getString("ID"));
                    p.put("motivo", motivo);
                    p.put("Estado", art.getString("Estado"));
                    p.put("Descripcion", art.getString("Descripcion"));
                    JSONArray ls = new JSONArray();
                    ls.put(new JSONObject(art.toString()));
                    dbCuenta.eliminar(mesa.getString("ID"), ls);
                    myServicio.rmLinea(p);
                    rellenarTicket();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendMessageMesaCobrada(Double entrega, double cambio) {
        Handler handlerMesas = myServicio.getExHandler("camareros");
        Message msg = handlerMesas.obtainMessage();
        Bundle bundle = msg.getData();
        if (bundle == null) bundle = new Bundle();
        bundle.putString("op", "show_info_cobro");
        bundle.putDouble("entrega", entrega );
        bundle.putDouble("cambio", cambio);
        msg.setData(bundle);
        handlerMesas.sendMessage(msg);
    }



}