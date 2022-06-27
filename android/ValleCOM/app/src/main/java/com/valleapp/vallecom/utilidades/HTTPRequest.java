package com.valleapp.vallecom.utilidades;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class HTTPRequest {

    public String getParams(ContentValues params){
        StringBuilder sbParams = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()) {
            try {
                if (i != 0){
                    sbParams.append("&");
                }
                sbParams.append(key).append("=")
                        .append(URLEncoder.encode((String) params.get(key), "UTF-8"));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }
        return sbParams.toString();
    }

    public HTTPRequest(){} // esto es solo para utiliazar la funcion sendMessage()
    public HTTPRequest(String strUrl, final ContentValues params, final String op, final Handler handlerExternal){
        // Create a new HttpClient and Post Header
        HttpURLConnection conn = null;

        try {

            if(!strUrl.contains("http://")) strUrl = "http://"+ strUrl;
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept-Charset", "UTF-8");

            // Execute HTTP Post Request
            final HttpURLConnection finalConn = conn;
            final Thread th = new Thread(){
                public void run(){
                    int statusCode = -1;
                    try {
                        DataOutputStream wr = new DataOutputStream(finalConn.getOutputStream());
                        wr.writeBytes(getParams(params));
                        wr.flush();
                        wr.close();

                        statusCode = finalConn.getResponseCode();
                        finalConn.setReadTimeout(3000);

                        InputStream in = new BufferedInputStream(finalConn.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder result = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }

                        if (handlerExternal != null)
                            sendMessage(handlerExternal, op, result.toString());

                     } catch ( ConnectException e){
                        if(handlerExternal!= null) sendMessage(handlerExternal, "no_connexion", null);
                     } catch (Exception e) {
                         // TODO Auto-generated catch block
                        if(handlerExternal!= null && statusCode==500) sendMessage(handlerExternal, "ERROR", null);
                    }
                }
            };
            th.start();
        } catch (Exception e) {
           e.printStackTrace();
        }finally {
            if(conn != null) conn.disconnect();
        }

    }

    public void sendMessage(Handler handler, String op, String res){
        Message msg = handler.obtainMessage();
        Bundle bundle = msg.getData();
        if (bundle == null) bundle = new Bundle();
        bundle.putString("RESPONSE", res);
        bundle.putString("op", op);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }


}