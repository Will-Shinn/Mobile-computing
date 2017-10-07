package com.mobile.daryldaryl.mobile_computing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Build;
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

    private View mProgressView;
    private View mRegFormView;

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


        mRegFormView = rootView.findViewById(R.id.email_reg_form);
        mProgressView = rootView.findViewById(R.id.register_progress);


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

                                getUser();

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

    public void getUser() {

        String url = ServerInfo.url + "/user";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.i("LoginActivity", response.toString());
                try {
                    JSONObject results = (JSONObject) response.getJSONArray("results").get(0);

                    String displayName = results.getString("displayName");
                    String city = results.getString("city");
                    String email = results.getString("email");
                    long contact = results.getLong("contact");


                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("displayName", displayName);
                    intent.putExtra("city", city);
                    intent.putExtra("email", email);
                    intent.putExtra("contact", contact);


                    startActivity(intent);
                    getActivity().finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Network issues, please try later.", Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences sharedPref = getActivity().getSharedPreferences(
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

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
