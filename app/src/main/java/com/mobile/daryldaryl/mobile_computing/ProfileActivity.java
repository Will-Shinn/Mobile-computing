package com.mobile.daryldaryl.mobile_computing;

import android.app.PendingIntent;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mobile.daryldaryl.mobile_computing.tools.ServerInfo;
import com.mobile.daryldaryl.mobile_computing.tools.SingletonQueue;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView save;
    private TextView edit_email;
    private EditText edit_name;
    private EditText edit_city;
    private EditText edit_contact;

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        queue = SingletonQueue.getInstance(this).getRequestQueue();

        save = (TextView) findViewById(R.id.save);
        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_email = (TextView) findViewById(R.id.edit_email);
        edit_city = (EditText) findViewById(R.id.edit_city);
        edit_contact = (EditText) findViewById(R.id.edit_contact);


        Intent intent = getIntent();
        String displayName = intent.getStringExtra("displayName");
        String city = intent.getStringExtra("city");
        String email = intent.getStringExtra("email");
        long contact = intent.getLongExtra("contact", 61437730888l);

        edit_name.setText(displayName);
        edit_email.setText(email);
        edit_city.setText(city);
        edit_contact.setText(contact + "");


//        edit_email.setPaintFlags(edit_email.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = ServerInfo.url + "/update";
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("LoginActivity", response.toString());

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ProfileActivity.this, "Network issues, please try later.", Toast.LENGTH_LONG).show();

                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                                "Mobile", Context.MODE_PRIVATE);
                        String access_token = sharedPref.getString("access_token", "");

                        Map<String, String> headers = new HashMap<>();
                        headers.put("Accept", "application/json");
                        headers.put("Authorization", access_token);

                        return headers;
                    }
                };
                queue.add(jsonObjectRequest);
            }
        });

    }

    

}
