package com.example.myapplication.notificationservice;

import android.app.Activity;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NotificationSender {
    private static String postUrl = "https://fcm.googleapis.com/fcm/send";
    private static String key = "AAAAsB0Y868:APA91bGLrsxbhlRNJej685lIq07QudDt0hPYN_F9rrcFNT0tW0U1EIxjeuhP1lrh7Dg6PSmV2eJLny1wlkAfef1XXbDYPQDVns9b7QYe5bEec5rp3fS94d9zG1Gk2YzRCES5OEfUJhUa";

    public static void pushNotification(String userFcmToken, Context context){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        RequestQueue queue = Volley.newRequestQueue(context);

        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("to", userFcmToken);
            JSONObject notificationObject = new JSONObject();
            notificationObject.put("title", "EVDAŞ");
            notificationObject.put("body", "Bir bildiriminiz var!");
            jsonObject.put("notification", notificationObject);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("FCM", response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                public Map<String, String> getHeaders() throws AuthFailureError{
                    Map<String, String> header = new HashMap<>();
                    header.put("Content-Type", "application/json");
                    header.put("Authorization", "key=" + key);
                    return header;
                }
            };
            queue.add(request);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void pushRejectNotification(String senderToken, Context context) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        RequestQueue queue = Volley.newRequestQueue(context);

        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("to", senderToken);
            JSONObject notificationObject = new JSONObject();
            notificationObject.put("title", "EVDAŞ");
            notificationObject.put("body", "Eşleşme talebiniz reddedildi :(");
            jsonObject.put("notification", notificationObject);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("FCM", response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                public Map<String, String> getHeaders() throws AuthFailureError{
                    Map<String, String> header = new HashMap<>();
                    header.put("Content-Type", "application/json");
                    header.put("Authorization", "key=" + key);
                    return header;
                }
            };
            queue.add(request);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
