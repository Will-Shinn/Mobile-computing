package com.mobile.daryldaryl.mobile_computing;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mobile.daryldaryl.mobile_computing.models.Checkin;
import com.mobile.daryldaryl.mobile_computing.models.Place;
import com.mobile.daryldaryl.mobile_computing.tools.ServerInfo;
import com.mobile.daryldaryl.mobile_computing.tools.SingletonQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyCheckInActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private List<Checkin> mData = null;
    private CheckInAdapter mAdapter;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_check_in);

        queue = SingletonQueue.getInstance(this).getRequestQueue();
        mData = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.check_in_list);
        mLayoutManager = new LinearLayoutManager(this, null, LinearLayoutManager.VERTICAL, 0);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter = new CheckInAdapter(mData, this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(MyCheckInActivity.this,
                mLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        mAdapter.notifyDataSetChanged();

        init();
    }

    public void init() {
        String url = ServerInfo.url + "/my_check_in";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = (JSONArray) response.get("results");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                        Checkin checkin = new Checkin();
                        checkin.setName(jsonObject.getString("name"));
                        checkin.setVicinity(jsonObject.getString("vicinity"));
                        checkin.setLat(jsonObject.getDouble("lat"));
                        checkin.setLng(jsonObject.getDouble("lng"));
                        checkin.setTime(jsonObject.getLong("time"));
                        mData.add(checkin);
                    }
                    mAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.i("map", error.toString());
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
}
