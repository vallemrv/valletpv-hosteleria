package com.valleapp.valletpv.tools;

import android.content.ContentValues;

import org.json.JSONObject;

public class ServerConfig {
    String url =null ;
    String code = null;
    String UID = null;
    public ServerConfig(String code, String UID, String url){
        this.code = code;
        this.UID = UID;
        this.url = url;
    }
    public ServerConfig(String url){
        this.url = url;
    }
    public static ServerConfig loadJSON(String json){
        try {
            JSONObject obj = new JSONObject(json);
            return new ServerConfig(obj.getString("code"), obj.getString("UID"), obj.getString("url"));

        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }

    public ContentValues getParams(){
        ContentValues params = new ContentValues();
        if (code != null && UID != null) {
            params.put("code", code);
            params.put("UID", UID);
        }
        return params;
    }

    public String getUrl(String endPoint){
        if (url == null || url.isEmpty()) return null;
        String strUrl = "";
        if(!url.contains("http://") && !url.contains("https://")) strUrl = "http://"+ url;
        if (!endPoint.startsWith("/")) endPoint = "/"+endPoint;
        if (strUrl.endsWith("/")) strUrl = strUrl.substring(0, strUrl.length()-1);
        if (!strUrl.endsWith("api")) strUrl = strUrl+"/api";
        return strUrl+endPoint;
    }
    public String toJson(){
        if (url != null && code != null && UID != null)
        return "{\"code\":\""+code+"\",\"UID\":\""+UID+"\",\"url\":\""+url+"\"}";
        else return null;
    }
}
