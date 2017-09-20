package com.mobile.daryldaryl.mobile_computing;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
    private DatabaseReference mDatabase;
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

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();

                    mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Get user value
                                    User user = dataSnapshot.getValue(User.class);

                                    // [START_EXCLUDE]
                                    if (user == null) {
                                        // User is null, error out
                                        Log.e("adapter", "User " + userId + " is unexpectedly null");

                                    } else {
                                        // Write new post
                                        getTime(userId, user.username, place);
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w("adapter", "getUser:onCancelled", databaseError.toException());
                                    // [START_EXCLUDE]
//                                        setEditingEnabled(true);
                                    // [END_EXCLUDE]
                                }
                            });

                    Toast.makeText(view.getContext(), place.getName(), Toast.LENGTH_SHORT).show();


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

                                        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                                                new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        // Get user value
                                                        User user = dataSnapshot.getValue(User.class);

                                                        // [START_EXCLUDE]
                                                        if (user == null) {
                                                            // User is null, error out
                                                            Log.e("adapter", "User " + userId + " is unexpectedly null");

                                                        } else {
                                                            // Write new post

                                                            getTime(userId, user.username, place);
                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                        Log.w("adapter", "getUser:onCancelled", databaseError.toException());
                                                    }
                                                });

                                        Toast.makeText(context, "current location ", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }
            });
        }
    }


    private void getTime(final String userId, final String username, final Place place) {
        String url = "http://api.geonames.org/timezoneJSON?lat=" + place.getLat() + "&lng=" + place.getLng() + "&username=daryldaryl";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    String time = response.get("time").toString();
                    Checkin(userId, username, place, time);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.i("ii", error.toString());
            }
        });
        queue.add(jsonObjectRequest);
    }


    private void Checkin(String userId, String username, Place place, String time) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("check-in").push().getKey();
        Checkin checkin = new Checkin(userId, username, place, time);
        Map<String, Object> postValues = checkin.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/check-in/" + key, postValues);
        childUpdates.put("/user-check-in/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
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