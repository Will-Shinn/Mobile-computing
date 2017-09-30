package com.mobile.daryldaryl.mobile_computing;

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
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
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
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.mobile.daryldaryl.mobile_computing.gcm.MyHandler;
import com.mobile.daryldaryl.mobile_computing.gcm.NotificationSettings;
import com.mobile.daryldaryl.mobile_computing.gcm.RegistrationIntentService;
import com.mobile.daryldaryl.mobile_computing.models.Place;
import com.mobile.daryldaryl.mobile_computing.tools.SingletonQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.*;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by liboa on 20/09/2017.
 */

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener, NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnPoiClickListener, PopupMenu.OnMenuItemClickListener {

    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_SELECT_IMAGE_IN_ALBUM = 1;

    public static MainActivity mainActivity;
    public static Boolean isVisible = false;
    private GoogleCloudMessaging gcm;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    static GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    static ArrayList<LatLng> list;
    static HeatmapTileProvider mProvider;
    static TileOverlay mOverlay;
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
    private RelativeLayout relativeLayout;

    FloatingActionButton fab;
    Dialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        mainActivity = this;
        NotificationsManager.handleNotifications(this, NotificationSettings.SenderId, MyHandler.class);
        registerWithNotificationHubs();

        String userId = "001";
        list = new ArrayList<>();
        list.add(new LatLng(0, 0));

        relativeLayout = (RelativeLayout) findViewById(R.id.map_layout);

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

        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.check_in);
        recyclerview = dialog.findViewById(R.id.grid_recycler);
        mLayoutManager = new LinearLayoutManager(this, null, LinearLayoutManager.VERTICAL, 0);
        recyclerview.setLayoutManager(mLayoutManager);
        recyclerview.setAdapter(mAdapter = new PlaceAdapter(mData, dialog, MainActivity.this, userId));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(MainActivity.this,
                mLayoutManager.getOrientation());
        recyclerview.addItemDecoration(dividerItemDecoration);
        mAdapter.notifyDataSetChanged();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);


        fab.setOnLongClickListener(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {

                fab.setOnClickListener(this);
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    fab.setOnClickListener(this);
//
//                } else {
//                    fab.setOnClickListener(this);
//                }
                return;
            }
            case 102: {
                initMap();
                return;
            }
            case 103: {
                takePhoto();
                return;
            }
            case 104: {
                selectImageInAlbum();
                return;
            }
        }
    }

    public void takePhoto() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.CAMERA},
                    103);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {

            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                mFilePhotoTaken = File.createTempFile(
                        "IMG_",  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );


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

    public void selectImageInAlbum() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    104);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM);
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

                    Intent intent = new Intent(MainActivity.this, RecognitionActivity.class);

                    intent.putExtra("BitmapUri", mImageUri);

                    startActivity(intent);

                }
                break;
            case REQUEST_SELECT_IMAGE_IN_ALBUM:
                if (resultCode == RESULT_OK) {
                    Uri imageUri;
                    if (data == null || data.getData() == null) {
                        imageUri = mUriPhotoTaken;
                    } else {
                        imageUri = data.getData();
                    }
                    Intent intent = new Intent(MainActivity.this, RecognitionActivity.class);

                    intent.putExtra("BitmapUri", imageUri);

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
            startActivity(new Intent(MainActivity.this, MyCheckInActivity.class));
        } else if (id == R.id.today) {
//            startActivity(new Intent(MainActivity.this, OutlineActivity.class));
        } else if (id == R.id.my_favourite) {

        } else if (id == R.id.settings) {

        } else if (id == R.id.log_out) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
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

        initMap();

    }

    private void initMap() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    102);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnPoiClickListener(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

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

        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
//        mProvider.setOpacity(0.7);
//        mOverlay.clearTileCache();
    }

    public static void addCheckIn(LatLng point) {
        list.add(point);
        mProvider.setData(list);
        mOverlay.clearTileCache();


    }

    public static void removeCheckIn() {
        list.remove(1);
        mProvider.setData(list);
        mOverlay.clearTileCache();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Log.i("menu", item + " " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.camera:
                takePhoto();
                break;
            case R.id.album:
                selectImageInAlbum();
                break;
        }
        return false;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("MainActivity", "This device is not supported by Google Play Services.");
//                ToastNotify("This device is not supported by Google Play Services.");
                Toast.makeText(this, "This device is not supported by Google Play Services.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    public void registerWithNotificationHubs() {
        Log.i("MainActivity", " Registering with Notification Hubs");

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    public void onClick(View view) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    101);
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if (location != null) {
                            currentLocation = location;

                            mData.clear();
                            mData.add(null);
                            dialog.show();

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

    @Override
    public boolean onLongClick(View view) {
        //-37.820592,144.942762
        PopupMenu popup = new PopupMenu(MainActivity.this, view, Gravity.CENTER);
        popup.getMenuInflater()
                .inflate(R.menu.select_pic, popup.getMenu());

        popup.setOnMenuItemClickListener(MainActivity.this);

        popup.show();

//                takePhoto();
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
    }

//    public void ToastNotify(final String notificationMessage) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(MainActivity.this, notificationMessage, Toast.LENGTH_LONG).show();
//                TextView helloText = (TextView) findViewById(R.id.text_hello);
//                helloText.setText(notificationMessage);
//            }
//        });
//    }
}
