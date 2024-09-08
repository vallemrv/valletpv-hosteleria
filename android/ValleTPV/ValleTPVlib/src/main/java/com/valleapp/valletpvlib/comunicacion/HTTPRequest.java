package com.valleapp.valletpvlib.comunicacion;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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


    public HTTPRequest(String strUrl, ContentValues params, final String op, final Handler handlerExternal){
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
            HttpURLConnection finalConn = conn;
            new Thread(() -> {
                int statusCode = -1;
                try {
                    Log.d("HTTPRequeest", getParams(params));
                    DataOutputStream wr = new DataOutputStream(finalConn.getOutputStream());
                    wr.writeBytes(getParams(params));
                    wr.flush();
                    wr.close();
                    statusCode = finalConn.getResponseCode();
                    InputStream in = new BufferedInputStream(finalConn.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    if (handlerExternal != null)
                        sendMessage(handlerExternal, op, result.toString());

                } catch (ConnectException e) {
                    if (handlerExternal != null) sendMessage(handlerExternal, "no_connexion", null);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Log.e("HTTPRequeest", e.toString());
                    if (handlerExternal != null && statusCode == 500)
                        sendMessage(handlerExternal, "ERROR", null);

                }
            }).start();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("HTTPRequeest", e.toString());
        }finally {
            if(conn != null) conn.disconnect();
        }

    }

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
                Log.e("HTTPRequeest", e.toString());
            }
            i++;
        }
        return sbParams.toString();
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