package com.mobile.daryldaryl.mobile_computing;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
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
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.PhoneNumberUtils;
import android.location.Geocoder;
import android.location.Address;
import android.app.PendingIntent;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
//import com.google.android.gms.identity.intents.Address;
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
import com.google.android.gms.vision.text.Text;
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

import android.content.BroadcastReceiver;

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
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnPoiClickListener {

    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_SELECT_IMAGE_IN_ALBUM = 1;

    public static MainActivity mainActivity;
    public static Boolean isVisible = false;
    private GoogleCloudMessaging gcm;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    static GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    public static ArrayList<LatLng> list;
    public static HeatmapTileProvider mProvider;
    public static HeatmapTileProvider mProviderdb;
    public static TileOverlay mOverlay;
    public static TileOverlay mOverlaydb;
    public RequestQueue queue;
    private RecyclerView recyclerview;
    private LinearLayoutManager mLayoutManager;
    private List<Place> mData = null;
    private Uri mUriPhotoTaken;

    private Place pic_place;
    // File of the photo taken with camera
    private File mFilePhotoTaken;
    // The image selected to detect.
    private Bitmap mBitmap;
    private Uri mImageUri;
    private Location currentLocation;


    private RelativeLayout relativeLayout;

    private TextView nav_displayName;
    private TextView nav_email;


    private Boolean isFabOpen = false;
    private FloatingActionButton fab1, fab2, fab3, fab4, fab5;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

    FloatingActionButton fab;
    Dialog dialog;
    private BasicAdapter mAdapter;

    private String displayName;
    private String city;
    private String email;
    private long contact;
    public static boolean HEATMAP = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        Intent intent = getIntent();
        displayName = intent.getStringExtra("displayName");
        city = intent.getStringExtra("city");
        email = intent.getStringExtra("email");
        contact = intent.getLongExtra("contact", 61437730888l);

        mainActivity = this;

        NotificationsManager.handleNotifications(this, NotificationSettings.SenderId, MyHandler.class);
        registerWithNotificationHubs();


        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        fab4 = (FloatingActionButton) findViewById(R.id.fab4);
        fab5 = (FloatingActionButton) findViewById(R.id.fab5);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        fab3.setOnClickListener(this);
        fab4.setOnClickListener(this);
        fab5.setOnClickListener(this);

        list = new ArrayList<>();

        relativeLayout = (RelativeLayout) findViewById(R.id.map_layout);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        nav_displayName = header.findViewById(R.id.nav_displayName);
        nav_email = header.findViewById(R.id.nav_email);

        nav_displayName.setText(displayName);
        nav_email.setText(email);


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

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(MainActivity.this,
                mLayoutManager.getOrientation());
        recyclerview.addItemDecoration(dividerItemDecoration);
//        recyclerview.setAdapter(mAdapter = new PlaceAdapter(mData, dialog, MainActivity.this, userId));
//        mAdapter.notifyDataSetChanged();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {
                fab.setOnClickListener(this);
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
            case 105: {
                sendSMS(String.valueOf(contact), "help me");
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


    public void setPlace(Place pic_place) {
        this.pic_place = pic_place;
    }

    public Place getPlace() {
        return pic_place;
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
                    intent.putExtra("name", getPlace().getName());
                    intent.putExtra("lat", getPlace().getLat());
                    intent.putExtra("lng", getPlace().getLng());
                    intent.putExtra("vicinity", getPlace().getVicinity());
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
                    intent.putExtra("name", getPlace().getName());
                    intent.putExtra("lat", getPlace().getLat());
                    intent.putExtra("lng", getPlace().getLng());
                    intent.putExtra("vicinity", getPlace().getVicinity());
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
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("displayName", displayName);
            intent.putExtra("city", city);
            intent.putExtra("email", email);
            intent.putExtra("contact", contact);


            startActivity(intent);

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
                            list.add(new LatLng(location.getLatitude(), location.getLongitude()));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
//                            addHeatMap();
                        }
                    }
                });
    }

    private void addHeatMap() {


        mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .build();

        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

        mProviderdb = new HeatmapTileProvider.Builder()
                .data(list)
                .build();

        mOverlaydb = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProviderdb));

//        mOverlay.clearTileCache();
    }

    public void switch_heatmap() {
        if (HEATMAP) {
            mOverlay.remove();
            mOverlaydb.remove();
            HEATMAP = false;
        } else {
            addHeatMap();
            HEATMAP = true;
        }
    }

    public static synchronized void addCheckIn(LatLng point, HeatmapTileProvider mProvider, TileOverlay mOverlay) {
        list.add(point);
        mProvider.setData(list);
        mOverlay.clearTileCache();
    }

    public static synchronized void removeCheckIn(int i) {
        if (list.size() <= 1)
            return;

        list.remove(1);
        switch (i) {
            case 1:
                mProvider.setData(list);
                mOverlay.clearTileCache();
                break;
            case 2:
                mProviderdb.setData(list);
                mOverlaydb.clearTileCache();
                break;
        }

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
            Log.i("MainActivity", "success");
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }
//    public void doSendSMSTo(String phoneNumber,String message){
//        if(PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)){
//            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNumber));
//            intent.putExtra("sms_body", message);
//            startActivity(intent);
//        }
//    }
//private String getAddress(String  adressStr) throws IOException {
//    Geocoder geocoder = new Geocoder(this);
////    boolean falg = geocoder.isPresent();
//
//    StringBuilder stringBuilder = new StringBuilder();
//    try {
//
//        //根据经纬度获取地理位置信息
///            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//
//        //根据地址获取地理位置信息
//        List<Address> addresses = geocoder.getFromLocationName(adressStr, 1);
//
//        if (addresses.size() > 0) {
//            Address address = addresses.get(0);
//            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
//                stringBuilder.append(address.getAddressLine(i)).append("\n");
//            }
//            stringBuilder.append(address.getCountryName()).append("_");//国家
//            stringBuilder.append(address.getFeatureName()).append("_");//周边地址
//            stringBuilder.append(address.getLocality()).append("_");//市
//            stringBuilder.append(address.getPostalCode()).append("_");
//            stringBuilder.append(address.getCountryCode()).append("_");//国家编码
//            stringBuilder.append(address.getAdminArea()).append("_");//省份
//            stringBuilder.append(address.getSubAdminArea()).append("_");
//            stringBuilder.append(address.getThoroughfare()).append("_");//道路
//            stringBuilder.append(address.getSubLocality()).append("_");//香洲区
//            stringBuilder.append(address.getLatitude()).append("_");//经度
//            stringBuilder.append(address.getLongitude());//维度
//
//        }
//    } catch (IOException e) {
//        // TODO Auto-generated catch block
//        Toast.makeText(this, "报错", Toast.LENGTH_LONG).show();
//        e.printStackTrace();
//    }
//    return stringBuilder.toString();

    //}
    public void makemessage() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    102);
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {

                            double lat = location.getLatitude();
                            double lng = location.getLongitude();
                            Geocoder geo = new Geocoder(mainActivity);
                            try {

                                List<Address> list = geo.getFromLocation(lat, lng, 5);

                                if (list != null) {
                                    sendSMS(String.valueOf(contact), "help me, I am at" +
                                            location.getLatitude() + " " + location.getLongitude() +
                                            list.get(0).getLocality() + "https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude());
                                }
                            } catch (Exception e) {
                                Log.e("WEI", "Error : " + e.toString());
                            }
                        }


                    }

                });
    }

    public void sendSMS(String phoneNumber, String message) {

        String SENT_SMS_ACTION = "SENT_SMS_ACTION";
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        PendingIntent sentPI = PendingIntent.getBroadcast(mainActivity, 0, sentIntent, 0);
        android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();


        List<String> divideContents = smsManager.divideMessage(message);
        for (String text : divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, sentPI, null);
            Log.i("fd", "df");
            Log.i("dfd", "df");
        }
        mainActivity.registerReceiver(new BroadcastReceiver() {

            @Override
            public IBinder peekService(Context myContext, Intent service) {
                return super.peekService(myContext, service);
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context,
                                "Message has be sent successfully!", Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
//                        Toast.makeText(context,
//                                "Message failure!", Toast.LENGTH_SHORT)
//                                .show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
//                        Toast.makeText(context,
//                                "Message failure!", Toast.LENGTH_SHORT)
//                                .show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
//                        Toast.makeText(context,
//                                "Message failure!", Toast.LENGTH_SHORT)
//                                .show();
                        break;
                }
            }

        }, new IntentFilter("SENT_SMS_ACTION"));

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.fab:
                animateFAB();
                break;
            case R.id.fab1:
                recyclerview.setAdapter(mAdapter = new PlaceAdapter(mData, dialog, this));
                getNearbyPlace();
                break;
            case R.id.fab2:
                recyclerview.setAdapter(mAdapter = new CameraAdapter(mData, dialog, this));
                getNearbyPlace();
                break;
            case R.id.fab3:
                recyclerview.setAdapter(mAdapter = new PhotoAdapter(mData, dialog, this));
                getNearbyPlace();
                break;
            case R.id.fab4:
                switch_heatmap();
                break;
            case R.id.fab5:
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                    Manifest.permission.SEND_SMS},
                            105);

                    return;
                }
                makemessage();
                break;


        }

    }

    public void getNearbyPlace() {
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

    public void animateFAB() {

        if (isFabOpen) {

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab3.startAnimation(fab_close);
            fab4.startAnimation(fab_close);
            fab5.startAnimation(fab_close);

            fab1.setClickable(false);
            fab2.setClickable(false);
            fab3.setClickable(false);
            fab4.setClickable(false);
            fab5.setClickable(false);

            isFabOpen = false;
            Log.d("Raj", "close");

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab3.startAnimation(fab_open);
            fab4.startAnimation(fab_open);
            fab5.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            fab3.setClickable(true);
            fab4.setClickable(true);
            fab5.setClickable(true);
            isFabOpen = true;
            Log.d("abcdefg", "open hello");

        }
    }

}
