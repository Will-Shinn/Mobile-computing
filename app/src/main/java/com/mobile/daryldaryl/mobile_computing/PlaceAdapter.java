package com.mobile.daryldaryl.mobile_computing;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.daryldaryl.mobile_computing.models.Checkin;
import com.mobile.daryldaryl.mobile_computing.models.Place;
import com.mobile.daryldaryl.mobile_computing.models.User;
import com.mobile.daryldaryl.mobile_computing.tools.ServerInfo;
import com.mobile.daryldaryl.mobile_computing.tools.SingletonQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liboa on 20/09/2017.
 */


public class PlaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_PLACE = 0;
    private final int VIEW_TYPE_CURRENT = 1;
    private Context context;
    private Dialog dialog;
    private List<Place> mData = null;
    private FusedLocationProviderClient mFusedLocationClient;
    public RequestQueue queue;
    private Activity activity;
    private String userId;


    public PlaceAdapter(List mData, Dialog dialog, Activity activity, String userId) {
        this.dialog = dialog;
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.mData = mData;
        this.userId = userId;

        queue = SingletonQueue.getInstance(context).
                getRequestQueue();


    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_PLACE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
            return new PlaceViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_current_location, parent, false);
            return new CurrentViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position) instanceof Place) {
            return VIEW_TYPE_PLACE;
        } else {
            return VIEW_TYPE_CURRENT;
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PlaceViewHolder) {
            final Place place = mData.get(position);
            PlaceViewHolder placeViewHolder = (PlaceViewHolder) holder;
            placeViewHolder.placeName.setText(place.getName());
            placeViewHolder.placeAddress.setText(place.getVicinity());

            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", place.getName());
                jsonObject.put("lat", place.getLat());
                jsonObject.put("lng", place.getLng());
                jsonObject.put("vicinity", place.getVicinity());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    String url = ServerInfo.url + "/check_in";
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i("LoginActivity", response.toString());
                            Toast.makeText(activity, "check-in succeed", Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO Auto-generated method stub
                            Toast.makeText(activity, "Network issues, please try later.", Toast.LENGTH_LONG).show();
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            SharedPreferences sharedPref = activity.getApplicationContext().getSharedPreferences(
                                    "Mobile", Context.MODE_PRIVATE);
                            String access_token = sharedPref.getString("access_token", "");

                            Map<String, String> headers = new HashMap<>();
                            headers.put("Accept", "application/json");
                            headers.put("Authorization", access_token);

                            return headers;
                        }
                    };
                    queue.add(jsonObjectRequest);

//                    Toast.makeText(view.getContext(), place.getName(), Toast.LENGTH_SHORT).show();


                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        Log.i("map", "denied");
                        return;
                    }
                    mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        final Place place = new Place("customized place", location.getLatitude(), location.getLongitude(), "customized place");

                                        Toast.makeText(context, "current location ", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    private class PlaceViewHolder extends RecyclerView.ViewHolder {
        public TextView placeName;
        public TextView placeAddress;

        public PlaceViewHolder(View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.place_name);
            placeAddress = itemView.findViewById(R.id.place_address);

        }

    }

    private class CurrentViewHolder extends RecyclerView.ViewHolder {

        public CurrentViewHolder(View itemView) {
            super(itemView);
        }

    }


}