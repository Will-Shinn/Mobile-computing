package com.mobile.daryldaryl.mobile_computing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobile.daryldaryl.mobile_computing.tools.Utils;

public class CheckInDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    private String chechInName;
    private String chechInVicinity;
    private Double chechInLat;
    private Double chechInLng;
    private String chechInTime;
    private String receipt;

    private TextView checkInPlaceView;
    private TextView checkInCoorView;
    private TextView checkInVicinityView;
    private TextView checkInTimeView;
    private TextView receiptView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_detail);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detail_map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        chechInName = intent.getStringExtra("chechInName");
        chechInVicinity = intent.getStringExtra("chechInVicinity");
        chechInLat = intent.getDoubleExtra("chechInLat", 404);
        chechInLng = intent.getDoubleExtra("chechInLng", 404);
        chechInTime = intent.getStringExtra("chechInTime");
        receipt = intent.getStringExtra("receipt");

        checkInPlaceView = (TextView) findViewById(R.id.check_in_name);
        checkInCoorView = (TextView) findViewById(R.id.check_in_coor);
        checkInVicinityView = (TextView) findViewById(R.id.check_in_vicinity);
        checkInTimeView = (TextView) findViewById(R.id.check_in_time);
        receiptView = (TextView) findViewById(R.id.receipt);

        checkInPlaceView.setText(chechInName);
        checkInCoorView.setText("( " + chechInLng + ", " + chechInLng + " )");
        checkInVicinityView.setText(chechInVicinity);
        checkInTimeView.setText(chechInTime);
        if (!receipt.equals("null"))
            receiptView.setText(receipt);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng latLng = new LatLng(chechInLat, chechInLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(""));
    }
}
