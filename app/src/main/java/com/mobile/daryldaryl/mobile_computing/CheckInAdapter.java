package com.mobile.daryldaryl.mobile_computing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.mobile.daryldaryl.mobile_computing.models.Checkin;
import com.mobile.daryldaryl.mobile_computing.tools.SingletonQueue;
import com.mobile.daryldaryl.mobile_computing.tools.Utils;

import java.util.List;

/**
 * Created by liboa on 30/09/2017.
 */

public class CheckInAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_CHECKIN = 0;
    private Activity activity;
    private List<Checkin> mData = null;
    private FusedLocationProviderClient mFusedLocationClient;
    public RequestQueue queue;


    public CheckInAdapter(List mData, Activity activity) {
        this.activity = activity;
        this.mData = mData;

        queue = SingletonQueue.getInstance(activity).
                getRequestQueue();


    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CHECKIN) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_check_in, parent, false);
            return new ChechkInViewHolder(view);
        } else {
            return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position) instanceof Checkin) {
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
            chechkInViewHolder.chechInLat.setText(checkin.getLat() + "");
            chechkInViewHolder.chechInLng.setText(checkin.getLng() + "");
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
                    activity.startActivity(intent);

                }
            });
        } else {

        }
    }


    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    private class ChechkInViewHolder extends RecyclerView.ViewHolder {
        public TextView chechInName;
        public TextView chechInVicinity;
        public TextView chechInLat;
        public TextView chechInLng;
        public TextView chechInTime;

        public ChechkInViewHolder(View itemView) {
            super(itemView);
            chechInName = itemView.findViewById(R.id.check_in_name);
            chechInVicinity = itemView.findViewById(R.id.check_in_vicinity);
            chechInLat = itemView.findViewById(R.id.check_in_lat);
            chechInLng = itemView.findViewById(R.id.check_in_lng);
            chechInTime = itemView.findViewById(R.id.check_in_time);

        }

    }


}
