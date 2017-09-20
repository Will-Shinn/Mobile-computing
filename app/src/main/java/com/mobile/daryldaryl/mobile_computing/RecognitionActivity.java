package com.mobile.daryldaryl.mobile_computing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.mobile.daryldaryl.mobile_computing.models.RecognitionWord;
import com.mobile.daryldaryl.mobile_computing.tools.ImageHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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

        bitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                mImageUri, getContentResolver());

        if (bitmap != null) {
            // Show the image on screen.
            imageView = (ImageView) findViewById(R.id.selectedImage);
            imageView.setImageBitmap(bitmap);

            // Add detection log.


            doRecognize();
        } else {
            editText.setText("error");
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
//            mButtonSelectImage.setEnabled(true);
        }
    }
}
