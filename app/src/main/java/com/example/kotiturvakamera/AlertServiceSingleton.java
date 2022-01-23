package com.example.kotiturvakamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

public class AlertServiceSingleton {

    final String url = "https://raspberry-api.herokuapp.com/api/alerts";
    @SuppressLint("StaticFieldLeak")
    private static AlertServiceSingleton instance;
    private RequestQueue requestQueue;
    @SuppressLint("StaticFieldLeak")
    private static Context ctx;

    public AlertServiceSingleton(Context context) {
        this.ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized AlertServiceSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new AlertServiceSingleton(context);
        }
        return instance;
    }

    public interface VolleyResponseListener {

        void onError(String message);

        void onResponse(Integer alertCount);
    }

    public void getAlerts(VolleyResponseListener listener) {
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response) {
                        listener.onResponse(response.length());

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error.toString());
                    }
                }
        );
        getRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        AlertServiceSingleton.getInstance(ctx).addToRequestQueue(getRequest);
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
