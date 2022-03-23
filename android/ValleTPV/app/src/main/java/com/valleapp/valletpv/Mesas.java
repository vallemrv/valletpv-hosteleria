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
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.valleapp.valletpv.Util.AdaptadorSettings;
import com.valleapp.valletpv.Util.ServicioCom;
import com.valleapp.valletpv.db.DbCuenta;
import com.valleapp.valletpv.db.DbMesas;
import com.valleapp.valletpv.db.DbZonas;
import com.valleapp.valletpv.dlg.DlgSelCamareros;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class Mesas extends Activity {

    final Context cx = this;
    String server = "";
    int presBack = 0;
    DbZonas dbZonas = new DbZonas(cx);
    DbMesas dbMesas = new DbMesas(cx);
    DbCuenta dbCuenta = new DbCuenta(cx);
    JSONObject cam = null;
    JSONObject zn = null;
    JSONArray lsTicket = null;
    JSONArray lsautorizados = null;
    JSONArray lsnoautorizados = null;


    Dialog dlgListadoTicket;
    String IDTicket = "";

    AdaptadorSettings adaptadorSettings;
    ListView lista_setting;
    ServicioCom myServicio;
    CountDownTimer info;
    Boolean run_info = false;

    @SuppressLint("HandlerLeak")
    private Handler controller_mesas = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            if (op == null) {
                RellenarMesas();
            } else if (op.equals("mensaje")){
                String res = msg.getData().getString("mensaje");
                Dialog dg = new Dialog(cx);
                dg.setContentView(R.layout.dialog_mensaje);
                TextView txt = dg.findViewById(R.id.texto_mensaje);
                txt.setTextSize(35);
                txt.setText(res);
                dg.setTitle("Atencion mensaje...");
                dg.show();
            } else if (op.equals("get_lista")) {
                try {
                    String res = msg.getData().getString("RESPONSE");

                    if (lista_setting != null) {
                        JSONArray lista = new JSONArray(res);
                        ArrayList<JSONObject> alista = new ArrayList<>();
                        for (int i = 0; i < lista.length(); i++) {
                            alista.add(lista.getJSONObject(i));
                        }
                        adaptadorSettings = new AdaptadorSettings(cx, alista);
                        lista_setting.setAdapter(adaptadorSettings);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (op.equals("cobrar")) {
                try {
                    String res = msg.getData().getString("RESPONSE").toString();
                    JSONObject obj = new JSONObject(res);
                    Double total = obj.getDouble("totalcobro");
                    Double entrega = obj.getDouble("entrega");
                    mostrar_info(total, entrega);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler controller_ticket = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String op = msg.getData().getString("op");
            String res = msg.getData().getString("RESPONSE");
            if(op.equals("lsticket")){
                try {
                    lsTicket = new JSONArray(res);
                    MostrarListado(lsTicket);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else if(op.equals("ticket")){
                MostrarTicket(res);
            }

        }

    };



    @SuppressLint("SetTextI18n")
    private void MostrarListado(JSONArray ls) {
      try{

          presBack = 0;

          if(ls.length()>0){

            LinearLayout ll =  dlgListadoTicket.findViewById(R.id.pneListados);

            ll.removeAllViews();

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            params.setMargins(5,5,5,5);

            for (int i = 0; i < ls.length(); i++) {
                JSONObject  z =  ls.getJSONObject(i);

                LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                View v = inflater.inflate(R.layout.linea_cabecera_ticket, null);

                TextView  n = v.findViewById(R.id.lblNumTicket);
                TextView  f = v.findViewById(R.id.lblHoraFecha);
                TextView  m = v.findViewById(R.id.lblNombre);
                TextView  e = v.findViewById(R.id.lblEntrega);
                TextView  t = v.findViewById(R.id.lblTotal);

                m.setText(z.getString("Mesa"));
                f.setText(z.getString("Fecha")+" - "+z.getString("Hora"));
                e.setText(z.getString("Entrega") + " €");
                t.setText(z.getString("Total")+ " €");
                n.setText(z.getString("ID"));

                ((ImageButton)v.findViewById(R.id.btnVerTicket)).setTag(z.getString("ID"));

                ll.addView(v, params);

            }
        }

      }catch (Exception e){
        e.printStackTrace();
      }

    }

    private  void MostrarTicket(String res){
        try{
            presBack = 0;

            JSONObject ticket = new JSONObject(res);
            JSONArray ls = ticket.getJSONArray("lineas");
            Double Total = ticket.getDouble("total");


            if(ls.length()>0){

                LinearLayout ll = dlgListadoTicket.findViewById(R.id.pneListados);

                ll.removeAllViews();

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(5,5,5,5);

                for (int i = 0; i < ls.length(); i++) {
                    JSONObject  z =  ls.getJSONObject(i);

                    LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);
                    View v = inflater.inflate(R.layout.linea_ticket, null);

                    TextView  c = v.findViewById(R.id.lblCan);
                    TextView  p = v.findViewById(R.id.lblPrecio);
                    TextView  n = v.findViewById(R.id.lblNombre);
                    TextView  t = v.findViewById(R.id.lblTotal);

                    c.setText(z.getString("Can"));
                    p.setText(String.format("%01.2f €",z.getDouble("Precio")));
                    n.setText(z.getString("Nombre"));
                    t.setText(String.format("%01.2f €",z.getDouble("Total")));

                    ll.addView(v, params);

                }

                LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                View v = inflater.inflate(R.layout.linea_total, null);
                TextView  t = (TextView)v.findViewById(R.id.lblTotalTicket);
                t.setText(String.format("%01.2f €",Total));

                ll.addView(v, params);

            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }



    private void RellenarZonas() {
        try {

             JSONArray lszonas = dbZonas.getAll();

             if(lszonas.length()>0){

                LinearLayout ll = findViewById(R.id.pneZonas);
                ll.removeAllViews();

                 DisplayMetrics metrics = getResources().getDisplayMetrics();


                 LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,Math.round(metrics.density * 100));

                params.setMargins(5,5,5,5);

                for (int i = 0; i < lszonas.length(); i++) {
                    JSONObject  z =  lszonas.getJSONObject(i);

                    if(zn==null && i==0) zn=z;

                    Button btn = new Button(cx);
                    btn.setId(i);
                    btn.setSingleLine(false);
                    btn.setText(z.getString("Nombre"));
                    btn.setTag(z);
                    String[] rgb = z.getString("RGB").trim().split(",");
                    btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));

                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                          zn = (JSONObject)view.getTag();
                          RellenarMesas();
                        }
                    });
                    ll.addView(btn, params);
                }

                RellenarMesas();

            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void RellenarMesas() {
        try {
            presBack = 0;

            JSONArray lsmesas = dbMesas.getAll(zn.getString("ID"));
            if(lsmesas.length()>0){

                TableLayout ll = (TableLayout)findViewById(R.id.pneMesas);
                ll.removeAllViews();

                TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT);

                DisplayMetrics metrics = getResources().getDisplayMetrics();


                TableRow.LayoutParams rowparams = new TableRow.LayoutParams(
                        Math.round(metrics.density * 160),
                        Math.round(metrics.density * 160));

                rowparams.setMargins(5,5,5,5);
                //rowparams.weight = 1;

                TableRow row = new TableRow(cx);
                ll.addView(row, params);

                for (int i = 0; i < lsmesas.length(); i++) {

                    JSONObject  m =  lsmesas.getJSONObject(i);
                    LayoutInflater inflater = (LayoutInflater)cx.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);

                    View v = inflater.inflate(R.layout.boton_mesa, null);

                    ImageButton btnCm = v.findViewById(R.id.btnCambiarMesa);
                    ImageButton btnC = v.findViewById(R.id.btnCobrar);
                    ImageButton btnRm = v.findViewById(R.id.btnBorrarMesa);
                    LinearLayout panel = v.findViewById(R.id.pneBtnMesa);

                    if(!m.getBoolean("abierta")) panel.setVisibility(View.GONE);
                    else inicializarBtnAux(btnC, btnCm, btnRm, m);
                    m.put("Tarifa",zn.getString("Tarifa"));

                    Button btn = (Button)v.findViewById(R.id.btnMesa);

                    btn.setId(i);
                    btn.setText(m.getString("Nombre"));

                    btn.setTag(m);

                    String[] rgb = m.getString("RGB").trim().split(",");
                    btn.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));

                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (info != null && run_info) info.cancel();
                            Intent intent = new Intent(cx, Cuenta.class);
                            JSONObject obj = (JSONObject)view.getTag();
                            intent.putExtra("op", "m");
                            intent.putExtra("url", server);
                            intent.putExtra("cam", cam.toString());
                            intent.putExtra("mesa", obj.toString());
                            startActivity(intent);
                        }
                    });
                    row.addView(v, rowparams);

                    if (((i+1) % 5) == 0) {
                        row = new TableRow(cx);
                        ll.addView(row, params);
                    }
                }
            }

        }catch (Exception e){
           e.printStackTrace();
        }

    }

    Toast msg = null;

    @SuppressLint("DefaultLocale")
    private void mostrar_info(final Double total, final Double entrega) {

        if (info != null && run_info){
            info.cancel();
            if (msg != null) msg.cancel();
        }
        info = new CountDownTimer(15000, 500) {
            public void onTick(long millisUntilFinished) {
                Double cambio = entrega - total;
                cambio = cambio <= 0.0 ? 0.0 : cambio;
                if (msg != null) msg.cancel();
                msg = Toast.makeText(cx, String.format("Cambio: %01.2f € ",
                         cambio), Toast.LENGTH_LONG);

                /*View toastView = msg.getView();
                TextView toastMessage =  toastView.findViewById(android.R.id.message);
                toastMessage.setTextSize(45);
                toastMessage.setPadding(5,5,5,5);
                toastMessage.setTextColor(Color.RED);*/
                msg.show();
            }

            public void onFinish() {
                run_info = false;
                if (msg != null)  msg.cancel();
            }
        };
        run_info = true;
        info.start();

    }

    public void clickReconect(View v){
        try{
            if(myServicio != null){
                myServicio.Reconectar();
            }
            Toast.makeText(getApplicationContext(),"Conectando con el servidor", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void clickAddCamareros(View v) {
        try {
            presBack = 0;
            final DlgSelCamareros sel_cam = new DlgSelCamareros(cx);
            sel_cam.setNoautorizados(lsnoautorizados);
            sel_cam.setAutorizados(lsautorizados);
            sel_cam.setTitle("Elegir camareros");
            sel_cam.show();
            sel_cam.get_btn_ok().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lsautorizados = sel_cam.getAutorizados();
                    lsnoautorizados = sel_cam.getNoautorizados();
                    sel_cam.cancel();
                    myServicio.set_lista_autorizados(controller_mesas, lsautorizados.toString(),
                            cam.toString());
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    private void inicializarBtnAux(ImageButton btnC, ImageButton btnCm, ImageButton btnRm,   final JSONObject m) {
          btnCm.setTag(m);btnC.setTag(m);btnRm.setTag(m);
          btnCm.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                clickJuntarMesa(view);
                return false;
            }
          });
    }

    public void clickCobrarMesa(View v){
        presBack = 0;
        JSONObject m = (JSONObject)v.getTag();
        Intent intent = new Intent(cx, Cuenta.class);
        intent.putExtra("op", "c");
        intent.putExtra("url", server);
        intent.putExtra("cam", cam.toString());
        intent.putExtra("mesa", m.toString());
        startActivity(intent);
    }

    public void clickAbrirCaja(View v){
        presBack = 0;
        if(myServicio!=null) myServicio.AbrirCajon();
    }

    public  void clickVerTicket(View v){
        presBack = 0;
        IDTicket =  v.getTag().toString();
        ((Button) dlgListadoTicket.findViewById(R.id.btnImprimir)).setVisibility(View.VISIBLE);
        ((Button) dlgListadoTicket.findViewById(R.id.btnListado)).setVisibility(View.VISIBLE);
        if(myServicio!=null){
            myServicio.getTicket(controller_ticket, IDTicket);
        }
    }

    public  void clickSettings(View v){
        presBack = 0;
        final Dialog settings = new Dialog(cx);
        settings.setTitle("Opciones impresión...");
        settings.setContentView(R.layout.dialog_settings);
        lista_setting = settings.findViewById(R.id.lista_settings);
        Button salir = settings.findViewById(R.id.boton_salir);
        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adaptadorSettings != null){
                    JSONArray lista = adaptadorSettings.lista;
                    if(myServicio!=null) myServicio.set_lista_settings(controller_mesas,
                            lista.toString());

                }
                settings.dismiss();
            }
        });
        if(myServicio!=null) myServicio.getLsSettings(controller_mesas);
        settings.show();

    }

    public void clickListaTicket(View v){
        presBack = 0;
        dlgListadoTicket = new Dialog(this);
        dlgListadoTicket.setContentView(R.layout.listado_ticket);
        dlgListadoTicket.setTitle("Lista de ticket");
        Window window = dlgListadoTicket.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        final Button imp = dlgListadoTicket.findViewById(R.id.btnImprimir);
        final Button salir = dlgListadoTicket.findViewById(R.id.btnSalir);
        final Button ls =  dlgListadoTicket.findViewById(R.id.btnListado);
        imp.setVisibility(View.GONE);
        ls.setVisibility(View.GONE);

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlgListadoTicket.cancel();
            }
        });

        ls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imp.setVisibility(View.GONE);
                ls.setVisibility(View.GONE);
                MostrarListado(lsTicket);
            }
        });
        imp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myServicio!=null) myServicio.imprimirTicket(IDTicket);
                dlgListadoTicket.cancel();
            }
        });
        if(myServicio!=null) myServicio.getLsTicket(controller_ticket);
        dlgListadoTicket.show();
    }

    public void clickCambiarMesa(View v){
        presBack = 0;

        Intent intent = new Intent(cx, OpMesas.class);
        intent.putExtra("url", server);
        intent.putExtra("mesa", ((JSONObject)v.getTag()).toString());
        intent.putExtra("op", "cambiar");
        startActivity(intent);
    }

    public void clickJuntarMesa(View v){
        presBack = 0;

        Intent intent = new Intent(cx, OpMesas.class);
        intent.putExtra("url", server);
        intent.putExtra("mesa", ((JSONObject)v.getTag()).toString());
        intent.putExtra("op", "juntar");
        startActivity(intent);
    }

    public void clickBorrarMesa(View v){
        presBack = 0;

        final JSONObject m = (JSONObject)v.getTag();
        final Dialog dlg = new Dialog(cx);
        dlg.setContentView(R.layout.borrar_art);
        dlg.setTitle("Borrar Mesa ");
        final EditText motivo = dlg.findViewById(R.id.txtMotivo);
        final Button error = dlg.findViewById(R.id.btnError);
        final Button simpa =  dlg.findViewById(R.id.btnSimpa);
        final Button inv = dlg.findViewById(R.id.btnInv);
        final Button ok =  dlg.findViewById(R.id.btnOk);
        final ImageButton edit =  dlg.findViewById(R.id.btnEdit);
        final ImageButton exit = dlg.findViewById(R.id.btnSalir);

        final LinearLayout pneEdit =  dlg.findViewById(R.id.pneEditarMotivo);
        pneEdit.setVisibility(View.GONE);

            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dlg.cancel();
                }
            });

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(pneEdit.getVisibility() == View.VISIBLE)  pneEdit.setVisibility(View.GONE);
                    else pneEdit.setVisibility(View.VISIBLE);
                }
            });

            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (motivo.getText().length() > 0) {
                        try {
                            String idm = m.getString("ID");
                            ContentValues p = new ContentValues();
                            p.put("motivo", motivo.getText().toString());
                            p.put("idm", idm);
                            p.put("idc", cam.getString("ID"));
                            if (myServicio != null){
                                dbMesas.cerrarMesa(idm); dbCuenta.eliminar(idm); RellenarMesas();
                                myServicio.rmMesa(p);
                            }
                            dlg.cancel();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            error.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String idm = m.getString("ID");
                        ContentValues p = new ContentValues();
                        p.put("motivo", error.getText().toString());
                        p.put("idm",idm);
                        p.put("idc", cam.getString("ID"));
                        if (myServicio != null){
                             dbMesas.cerrarMesa(idm); dbCuenta.eliminar(idm); RellenarMesas();
                             myServicio.rmMesa(p);
                        }
                        dlg.cancel();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                 }
            });

            simpa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String idm = m.getString("ID");
                        ContentValues p = new ContentValues();
                        p.put("motivo", simpa.getText().toString());
                        p.put("idm", idm);
                        p.put("idc", cam.getString("ID"));
                        if (myServicio != null){
                            dbMesas.cerrarMesa(idm); dbCuenta.eliminar(idm); RellenarMesas();
                            myServicio.rmMesa(p);
                        }
                        dlg.cancel();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            inv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String idm = m.getString("ID");
                        ContentValues p = new ContentValues();
                        p.put("motivo", inv.getText().toString());
                        p.put("idm", idm);
                        p.put ("idc", cam.getString("ID"));
                        if (myServicio != null){
                            dbMesas.cerrarMesa(idm); dbCuenta.eliminar(idm); RellenarMesas();
                            myServicio.rmMesa(p);
                        }
                        dlg.cancel();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

         dlg.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesas);

        try {

            server = getIntent().getStringExtra("url");
            cam = new JSONObject(getIntent().getStringExtra("cam"));
            lsautorizados = new JSONArray(getIntent().getStringExtra("lsautorizados"));
            lsnoautorizados = new JSONArray(getIntent().getStringExtra("lsnoautorizados"));
            TextView title = findViewById(R.id.lblTitulo);
            title.setText(cam.getString("Nombre"));


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if(mConexion!=null && myServicio!=null) unbindService(mConexion);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        presBack = 0;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(presBack>=1) {
            super.onBackPressed();
        }else{
            Toast.makeText(getApplicationContext(),"Pulsa otra vez para salir", Toast.LENGTH_SHORT).show();
            presBack++;
        }
    }

    @Override
    protected void onResume() {
       Intent intent = new Intent(getApplicationContext(), ServicioCom.class);
       intent.putExtra("url", server);
       bindService(intent, mConexion, Context.BIND_AUTO_CREATE);
       RellenarZonas();
       super.onResume();
    }

    private ServiceConnection mConexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myServicio = ((ServicioCom.MyBinder)iBinder).getService();
            if(myServicio!=null) myServicio.setHandleMesas(controller_mesas);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myServicio = null;
        }
    };

}
