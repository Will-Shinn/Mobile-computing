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

import org.json.JSONException;
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

    private String displayName;
    private String city;
    private String email;
    private long contact;

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
        displayName = intent.getStringExtra("displayName");
        city = intent.getStringExtra("city");
        email = intent.getStringExtra("email");
        contact = intent.getLongExtra("contact", 61437730888l);

        edit_name.setText(displayName);
        edit_email.setText(email);
        edit_city.setText(city);
        edit_contact.setText(contact + "");


//        edit_email.setPaintFlags(edit_email.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (changed()) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("displayName", edit_name.getText().toString());
                        jsonObject.put("city", edit_city.getText().toString());
                        jsonObject.put("contact", Long.parseLong(edit_contact.getText().toString()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String url = ServerInfo.url + "/update";
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i("LoginActivity", response.toString());
                            boolean success = false;
                            try {
                                success = response.getBoolean("success");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (success) {
                                Toast.makeText(ProfileActivity.this, "update profile succeed", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "update profile failed", Toast.LENGTH_LONG).show();
                            }


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
                } else {
                    Toast.makeText(ProfileActivity.this, "Nothing changed", Toast.LENGTH_LONG).show();
                }

            }
        });

    }


    public boolean changed() {
        String tmp_name = edit_name.getText().toString();
        String tmp_city = edit_city.getText().toString();
        long tmp_contact = Long.parseLong(edit_contact.getText().toString());

        if (tmp_city.equals(city) && tmp_contact == contact && tmp_name.equals(displayName))
            return false;

        return true;
    }
}
