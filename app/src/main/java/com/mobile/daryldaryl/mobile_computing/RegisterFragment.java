package com.mobile.daryldaryl.mobile_computing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

/**
 * Created by liboa on 3/10/2017.
 */

public class RegisterFragment extends Fragment {
    private Button button;

    private EditText input_username;
    private EditText input_password;
    private EditText input_email;

    private TextView textView;

    private String username;
    private String password;
    private String email;


    private RequestQueue queue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_register, container, false);

        queue = SingletonQueue.getInstance(getActivity()).getRequestQueue();
        input_username = rootView.findViewById(R.id.input_name);
        input_password = rootView.findViewById(R.id.input_password);
        input_email = rootView.findViewById(R.id.input_email);

        textView = rootView.findViewById(R.id.link_login);
        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        button = rootView.findViewById(R.id.btn_signup);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", input_username.getText().toString());
                    jsonObject.put("password", input_password.getText().toString());
                    jsonObject.put("email", input_email.getText().toString());
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

                                SharedPreferences sharedPref = getActivity().getSharedPreferences(
                                        "Mobile", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("access_token", access_token);
                                editor.putBoolean("login", true);
                                editor.commit();

                                startActivity(new Intent(getActivity(), MainActivity.class));
                                getActivity().finish();
                            } else {
                                Toast.makeText(getActivity(), "username exists", LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Toast.makeText(getActivity(), "Network issues, please try later.", Toast.LENGTH_LONG).show();
                        Log.i("error", error.toString());
                    }
                });
                queue.add(jsonObjectRequest);
            }
        });

        return rootView;
    }
}
