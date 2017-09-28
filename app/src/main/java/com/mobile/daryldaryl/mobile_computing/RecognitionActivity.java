package com.mobile.daryldaryl.mobile_computing;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

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
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static android.R.attr.maxHeight;
import static android.R.attr.maxWidth;

public class RecognitionActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private VisionServiceClient client;
    private EditText editText;
    private ImageView imageView;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);


        editText = (EditText) findViewById(R.id.recognition_result);

        if (client == null) {
            client = new VisionServiceRestClient(getString(R.string.subscription_key),
                    "https://westeurope.api.cognitive.microsoft.com/vision/v1.0");
        }


        Intent intent = getIntent();
        mImageUri = intent.getParcelableExtra("BitmapUri");
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
                editText.setText("error");
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Log.i("uri", "wrongggg");
        }
    }

    public void doRecognize() {
        editText.setText("Analyzing...");
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

            if (e != null) {
                editText.setText("Error: " + e.getMessage());
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

                editText.setText(result);
            }
        }
    }
}
