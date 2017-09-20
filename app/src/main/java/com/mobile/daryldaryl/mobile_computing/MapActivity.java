package com.mobile.daryldaryl.mobile_computing;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.mobile.daryldaryl.mobile_computing.models.Place;
import com.mobile.daryldaryl.mobile_computing.tools.ImageHelper;
import com.mobile.daryldaryl.mobile_computing.tools.SingletonQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liboa on 20/09/2017.
 */

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnPoiClickListener {

    private static final int REQUEST_TAKE_PHOTO = 0;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private ArrayList<LatLng> list;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    public RequestQueue queue;
    private RecyclerView recyclerview;
    private LinearLayoutManager mLayoutManager;
    private List<Place> mData = null;
    private PlaceAdapter mAdapter;
    private Uri mUriPhotoTaken;

    // File of the photo taken with camera
    private File mFilePhotoTaken;
    // The image selected to detect.
    private Bitmap mBitmap;
    private Uri mImageUri;
    private Location currentLocation;

    private TextView username;
    private TextView useremail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        String userId = "001";
        list = new ArrayList<>();
        list.add(new LatLng(0, 0));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        username = header.findViewById(R.id.username);
        useremail = header.findViewById(R.id.useremail);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        queue = SingletonQueue.getInstance(this.getApplicationContext()).
                getRequestQueue();

        mData = new ArrayList<>();

        final Dialog dialog = new Dialog(MapActivity.this);
        dialog.setContentView(R.layout.check_in);
        recyclerview = dialog.findViewById(R.id.grid_recycler);
        mLayoutManager = new LinearLayoutManager(this, null, LinearLayoutManager.VERTICAL, 0);
        recyclerview.setLayoutManager(mLayoutManager);
        recyclerview.setAdapter(mAdapter = new PlaceAdapter(mData, dialog, MapActivity.this, userId));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(MapActivity.this,
                mLayoutManager.getOrientation());
        recyclerview.addItemDecoration(dividerItemDecoration);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {


                if (ActivityCompat.checkSelfPermission(MapActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    ActivityCompat.requestPermissions(MapActivity.this, new String[]{
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            101);
                    return;
                }
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(MapActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    currentLocation = location;

                                    mData.clear();
                                    mData.add(null);
                                    String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&radius=300&key=AIzaSyAeMJIpr7CVFQ7hPXnlr-p80bEhNcg5VIs";
                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {

                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                JSONArray jsonArray = (JSONArray) response.get("results");
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    JSONObject place = (JSONObject) jsonArray.get(i);
                                                    JSONObject geo = (JSONObject) place.get("geometry");
                                                    mData.add(new Place(place.get("name").toString(),
                                                            Double.parseDouble(((JSONObject) geo.get("location")).get("lat").toString()),
                                                            Double.parseDouble(((JSONObject) geo.get("location")).get("lng").toString()),
                                                            place.get("vicinity").toString()));
                                                }
                                                mAdapter.notifyDataSetChanged();
                                                dialog.show();

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
                                    });
                                    queue.add(jsonObjectRequest);
                                }
                            }
                        });


            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener()

        {
            @Override
            public boolean onLongClick(View view) {
                //-37.820592,144.942762


                takePhoto(view);
                return false;
            }
        });
    }

    public void takePhoto(View view) {
        ActivityCompat.requestPermissions(MapActivity.this, new String[]{
                        Manifest.permission.CAMERA},
                102);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Save the photo taken to a temporary file.
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                mFilePhotoTaken = File.createTempFile(
                        "IMG_",  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );

                // Create the File where the photo should go
                // Continue only if the File was successfully created
                if (mFilePhotoTaken != null) {
                    mUriPhotoTaken = FileProvider.getUriForFile(this,
                            "com.mobile.daryldaryl.mobile_computing.fileprovider",
                            mFilePhotoTaken);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoTaken);
//                    setResult(RESULT_OK, intent);
                    // Finally start camera activity
                    startActivityForResult(intent, REQUEST_TAKE_PHOTO);

                }
            } catch (IOException e) {
//                setInfo(e.getMessage());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("AnalyzeActivity", "onActivityResult");
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.


                    mImageUri = Uri.fromFile(mFilePhotoTaken);


                    Intent intent = new Intent(MapActivity.this, RecognitionActivity.class);
                    intent.putExtra("BitmapUri", mImageUri);

                    startActivity(intent);

                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.profile) {
            // Handle the camera action
        } else if (id == R.id.my_checkin) {
//            startActivity(new Intent(MapActivity.this, MyCheckinActivity.class));
        } else if (id == R.id.today) {
//            startActivity(new Intent(MapActivity.this, OutlineActivity.class));
        } else if (id == R.id.my_favourite) {

        } else if (id == R.id.settings) {

        } else if (id == R.id.log_out) {
            startActivity(new Intent(MapActivity.this, LoginActivity.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {
        Toast.makeText(getApplicationContext(), "Clicked: " +
                        pointOfInterest.name + "\nPlace ID:" + pointOfInterest.placeId +
                        "\nLatitude:" + pointOfInterest.latLng.latitude +
                        " Longitude:" + pointOfInterest.latLng.longitude,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnPoiClickListener(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
//        LatLng sydney = new LatLng(-33.852, 151.211);
//        googleMap.addMarker(new MarkerOptions().position(sydney)
//                .title("Marker in Sydney"));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(MapActivity.this, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    101);
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
                        }
                    }
                });

        addHeatMap();
    }

    private void addHeatMap() {
        int[] colors = {
                Color.rgb(102, 225, 0), // green
                Color.rgb(255, 0, 0)    // red
        };

        float[] startPoints = {
                0.2f, 1f
        };

        Gradient gradient = new Gradient(colors, startPoints);

        mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .gradient(gradient)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
//        mProvider.setOpacity(0.7);
//        mOverlay.clearTileCache();
    }
}
