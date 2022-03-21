package com.valleapp.valletpv.Util;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class HTTPRequest {


    public HTTPRequest( String Url, List <NameValuePair> params, final String op, final Handler success){
        // Create a new HttpClient and Post Header

        if(!Url.contains("http://")) Url = "http://"+Url;
        final HttpClient httpclient = new DefaultHttpClient();
        final HttpPost httppost = new HttpPost(Url);

        try {

            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            // Execute HTTP Post Request
            new Thread(){
                public void run(){
                    try {
                        HttpResponse response = httpclient.execute(httppost);
                        HttpEntity entity = response.getEntity();

                        Message msg = success.obtainMessage();
                        Bundle bundle = msg.getData();
                        if (bundle == null) bundle = new Bundle();
                        bundle.putString("RESPONSE", EntityUtils.toString(entity));
                        bundle.putString("op", op);
                        msg.setData(bundle);
                        success.sendMessage(msg);


                     } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Message msg = success.obtainMessage();
                        Bundle bundle = msg.getData();
                        if(bundle==null) bundle = new Bundle();
                        bundle.putString("RESPONSE", op);
                        bundle.putString("op","error");
                        msg.setData(bundle);
                        success.sendMessage(msg);
                    }
                }
            }.start();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


}