package com.mobile.daryldaryl.mobile_computing;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobile.daryldaryl.mobile_computing.models.Checkin;
import com.mobile.daryldaryl.mobile_computing.models.CheckinHeader;
import com.mobile.daryldaryl.mobile_computing.tools.SingletonQueue;
import com.mobile.daryldaryl.mobile_computing.tools.Utils;

import java.util.List;

/**
 * Created by liboa on 30/09/2017.
 */

public class CheckInAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_CHECKIN = 0;
    private final int VIEW_TYPE_CHECKIN_HEADER = 1;
    private Activity activity;
    private List<Checkin> mData = null;
    private FusedLocationProviderClient mFusedLocationClient;
    private FragmentManager fragmentManager;
    public RequestQueue queue;
    public double lat;
    public double lng;


    public CheckInAdapter(List mData, Activity activity, FragmentManager fragmentManager) {
        this.activity = activity;
        this.fragmentManager = fragmentManager;
        this.mData = mData;

        queue = SingletonQueue.getInstance(activity).
                getRequestQueue();


    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CHECKIN) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_check_in, parent, false);
            return new ChechkInViewHolder(view);
        } else if (viewType == VIEW_TYPE_CHECKIN_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_check_in_header, parent, false);
            return new ChechkInHeaderViewHolder(view);
        } else {
            return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position) instanceof CheckinHeader) {
            final Checkin checkin = mData.get(position);
            lat = checkin.getLat();
            lng = checkin.getLng();

            return VIEW_TYPE_CHECKIN_HEADER;
        } else if (mData.get(position) instanceof Checkin) {
            return VIEW_TYPE_CHECKIN;
        } else {
            return 404;
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChechkInViewHolder) {
            final Checkin checkin = mData.get(position);
            ChechkInViewHolder chechkInViewHolder = (ChechkInViewHolder) holder;
            chechkInViewHolder.chechInName.setText(checkin.getName());
            chechkInViewHolder.chechInVicinity.setText(checkin.getVicinity());
            chechkInViewHolder.chechInTime.setText(Utils.parseRecentTime(checkin.getTime()));


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), checkin.getName(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(activity, CheckInDetailActivity.class);
                    intent.putExtra("chechInName", checkin.getName());
                    intent.putExtra("chechInVicinity", checkin.getVicinity());
                    intent.putExtra("chechInLat", checkin.getLat());
                    intent.putExtra("chechInLng", checkin.getLng());
                    intent.putExtra("chechInTime", Utils.parseRecentTime(checkin.getTime()));
                    intent.putExtra("receipt", checkin.getReceipt());
                    activity.startActivity(intent);

                }
            });
        } else if (holder instanceof ChechkInHeaderViewHolder) {
            final Checkin checkin = mData.get(position);
            ChechkInHeaderViewHolder chechkInHeaderViewHolder = (ChechkInHeaderViewHolder) holder;
            chechkInHeaderViewHolder.chechInName.setText(checkin.getName());
            chechkInHeaderViewHolder.chechInVicinity.setText(checkin.getVicinity());
            chechkInHeaderViewHolder.chechInTime.setText(Utils.parseRecentTime(checkin.getTime()));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), checkin.getName(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(activity, CheckInDetailActivity.class);
                    intent.putExtra("chechInName", checkin.getName());
                    intent.putExtra("chechInVicinity", checkin.getVicinity());
                    intent.putExtra("chechInLat", checkin.getLat());
                    intent.putExtra("chechInLng", checkin.getLng());
                    intent.putExtra("chechInTime", Utils.parseRecentTime(checkin.getTime()));
                    intent.putExtra("receipt", checkin.getReceipt());
                    activity.startActivity(intent);

                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    private class ChechkInViewHolder extends RecyclerView.ViewHolder {
        public TextView chechInName;
        public TextView chechInVicinity;
        public TextView chechInTime;

        public ChechkInViewHolder(View itemView) {
            super(itemView);
            chechInName = itemView.findViewById(R.id.check_in_name);
            chechInVicinity = itemView.findViewById(R.id.check_in_vicinity);
            chechInTime = itemView.findViewById(R.id.check_in_time);

        }

    }

    private class ChechkInHeaderViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        public TextView chechInName;
        public TextView chechInVicinity;
        public TextView chechInTime;
        public GoogleMap mMap;

        public ChechkInHeaderViewHolder(View itemView) {
            super(itemView);
            chechInName = itemView.findViewById(R.id.check_in_name);
            chechInVicinity = itemView.findViewById(R.id.check_in_vicinity);
            chechInTime = itemView.findViewById(R.id.check_in_time);

            SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.list_map);
            mapFragment.getMapAsync(this);

        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            LatLng latLng = new LatLng(lat, lng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(""));
        }
    }

}
