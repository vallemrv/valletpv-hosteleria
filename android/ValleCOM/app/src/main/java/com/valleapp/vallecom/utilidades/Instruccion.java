package com.valleapp.valleCOM.utilidades;

import android.content.ContentValues;
import android.os.Handler;

public class Instruccion {

    public ContentValues getParams() {
        return params;
    }

    public String getUrl() {
        return url;
    }

    public Handler getHandler() {
        return handler;
    }

    public String getOp() {
        return op;
    }

    String url;
    Handler handler;
    ContentValues params;
    String op = "";

    public Instruccion(ContentValues params, String url, Handler handler, String op){
        this.params = params; this.url = url; this.handler = handler; this.op = op;
    }
    public Instruccion(ContentValues params, String url){
        this.params = params; this.url = url; this.handler = null;
    }
}
