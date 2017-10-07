package com.mobile.daryldaryl.mobile_computing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.luolc.emojirain.EmojiRainLayout;

public class WelcomeActivity extends AppCompatActivity {
    private EmojiRainLayout mContainer;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
//                finish();
//            }
//        }, 10500);

        imageView = (ImageView) findViewById(R.id.app_logo);

        mContainer = (EmojiRainLayout) findViewById(R.id.group_emoji_container);

        // add emoji sources
        mContainer.addEmoji(R.drawable.kaola);
        mContainer.addEmoji(R.drawable.kaola);
        mContainer.addEmoji(R.drawable.kaola);
        mContainer.addEmoji(R.drawable.kaola);
        mContainer.addEmoji(R.drawable.kaola);

        // set emojis per flow, default 6
        mContainer.setPer(8);

        // set total duration in milliseconds, default 8000
        mContainer.setDuration(580000);

        // set average drop duration in milliseconds, default 2400
        mContainer.setDropDuration(2000);

        // set drop frequency in milliseconds, default 500
        mContainer.setDropFrequency(300);

        mContainer.startDropping();
    }
}
