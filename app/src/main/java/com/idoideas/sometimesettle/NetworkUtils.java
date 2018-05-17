package com.idoideas.sometimesettle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NetworkUtils {
    public static void addNumberToServer(Context context, final String username){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url ="http://www.wewillneversettle.com/";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.w("Response is: ", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("That didn't work!", "That number request.");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("seed", "");
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                final JSONObject body = new JSONObject();
                try {
                    body.put("username", username);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        queue.add(stringRequest);
    }

    public static void addNewUserToDatabase(final Context context, final String username, final String email){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url ="http://www.wewillneversettle.com/";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.w("Response is: ", response);
                        if(response.equals("user-exists")){
                            MainActivity.prefs.edit().putString("username", null).apply();
                            MainActivity.prefs.edit().putBoolean("not-signed", true).apply();
                            WelcomeActivity.openExistsPopup();
                        } else {
                            context.startActivity(new Intent(context, MainActivity.class));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("That didn't work!", "That number request.");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("seed", "");
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                final JSONObject body = new JSONObject();
                try {
                    body.put("username", username);
                    body.put("email", email);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        queue.add(stringRequest);
    }
}
