package com.mobile.daryldaryl.mobile_computing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.mobile.daryldaryl.mobile_computing.tools.ImageHelper;
import com.mobile.daryldaryl.mobile_computing.tools.ServerInfo;
import com.mobile.daryldaryl.mobile_computing.tools.SingletonQueue;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.maxHeight;
import static android.R.attr.maxWidth;

public class RecognitionActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private VisionServiceClient client;
    private ImageView imageView;
    private Uri mImageUri;

    private View mProgressView;

    private RequestQueue queue;

    String name;
    double lat;
    double lng;
    String vicinity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);

        mProgressView = findViewById(R.id.recognition_progress);

        queue = SingletonQueue.getInstance(this).getRequestQueue();


        if (client == null) {
            client = new VisionServiceRestClient(getString(R.string.subscription_key),
                    "https://westeurope.api.cognitive.microsoft.com/vision/v1.0");
        }


        Intent intent = getIntent();
        mImageUri = intent.getParcelableExtra("BitmapUri");
        name = intent.getStringExtra("name");
        lat = intent.getDoubleExtra("lat", 0);
        lng = intent.getDoubleExtra("lng", 0);
        vicinity = intent.getStringExtra("vicinity");

        Log.i("uri", mImageUri + "");

        UCrop.Options options = new UCrop.Options();
        options.setFreeStyleCropEnabled(true);

        UCrop.of(mImageUri, Uri.fromFile(new File(getCacheDir(), "tmp")))
//                .withAspectRatio(16, 9)
                .withOptions(options)
                .withMaxResultSize(maxWidth, maxHeight)
                .start(this);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("uri", resultCode + "  " + requestCode);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            bitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                    resultUri, getContentResolver());

            if (bitmap != null) {
                // Show the image on screen.
                imageView = (ImageView) findViewById(R.id.selectedImage);
                imageView.setImageBitmap(bitmap);

                // Add detection log.


                doRecognize();
            } else {
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Log.i("uri", "wrongggg");
        }
    }

    public void doRecognize() {
//        editText.setText("Analyzing...");
        showProgress(true);
        try {
            new doRequest().execute();
        } catch (Exception e) {
//            mEditText.setText("Error encountered. Exception is: " + e.toString());
        }
    }

    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        OCR ocr;
        ocr = this.client.recognizeText(inputStream, LanguageCodes.English, true);

        String result = gson.toJson(ocr);
        Log.i("result", result);

        return result;
    }

    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        public doRequest() {
        }

        @Override
        protected String doInBackground(String... args) {
            try {

                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            showProgress(false);
            if (e != null) {
                this.e = null;
            } else {
                Gson gson = new Gson();
                OCR r = gson.fromJson(data, OCR.class);

                String result = "";
                for (Region reg : r.regions) {
                    for (Line line : reg.lines) {
                        for (Word word : line.words) {
                            result += word.text + " ";
                        }
                        result += "\n";
                    }
                    result += "\n\n";
                }

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("name", name);
                    jsonObject.put("lat", lat);
                    jsonObject.put("lng", lng);
                    jsonObject.put("vicinity", vicinity);
                    jsonObject.put("receipt", result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String url = ServerInfo.url + "/check_in_receipt";
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("LoginActivity", response.toString());
                        Toast.makeText(RecognitionActivity.this, "check-in succeed", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Toast.makeText(RecognitionActivity.this, "Network issues, please try later.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        SharedPreferences sharedPref = getApplicationContext().getApplicationContext().getSharedPreferences(
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
        }
    }
}
