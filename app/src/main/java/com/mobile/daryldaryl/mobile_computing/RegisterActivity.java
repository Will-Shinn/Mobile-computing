package com.mobile.daryldaryl.mobile_computing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mobile.daryldaryl.mobile_computing.tools.ServerInfo;
import com.mobile.daryldaryl.mobile_computing.tools.SingletonQueue;

import org.json.JSONException;
import org.json.JSONObject;

import static android.widget.Toast.LENGTH_SHORT;

public class RegisterActivity extends AppCompatActivity {

    private Button button;

    private EditText input_username;
    private EditText input_password;
    private EditText input_email;

    private String username;
    private String password;
    private String email;

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        queue = SingletonQueue.getInstance(this).getRequestQueue();
        
        input_username = (EditText) findViewById(R.id.input_name);
        input_password = (EditText) findViewById(R.id.input_password);
        input_email = (EditText) findViewById(R.id.input_email);

        button = (Button) findViewById(R.id.btn_signup);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", input_username.getText());
                    jsonObject.put("password", input_password.getText());
                    jsonObject.put("email", input_email.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                String url = ServerInfo.url + "/register";
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("LoginActivity", response.toString());
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                String access_token = response.getString("access_token");

                                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                                        "Mobile", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("access_token", access_token);
                                editor.putBoolean("login", true);
                                editor.commit();

                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "username exists", LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Toast.makeText(RegisterActivity.this, "Network issues, please try later.", Toast.LENGTH_LONG).show();
                        Log.i("error", error.toString());
                    }
                });
                queue.add(jsonObjectRequest);
            }
        });




    }
}
