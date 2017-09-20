package com.mobile.daryldaryl.mobile_computing.tools;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;


public class SwipeBackActivity extends AppCompatActivity {
    private SwipeLayout mSwipeLayout;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getBaseContext();

        mSwipeLayout = new SwipeLayout(mContext);
        mSwipeLayout.setOnFinishListener(new SwipeLayout.OnFinishListener() {
            @Override
            public void onFinish() {
                finish();
            }
        });
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getDecorView().setBackgroundDrawable(null);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        ViewGroup decorChild = ((ViewGroup) decor.getChildAt(0));

        TypedArray a = getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.windowBackground
        });

        int background = a.getResourceId(0, 0);
        a.recycle();

        decorChild.setBackgroundResource(background);

        decor.removeView(decorChild);
        mSwipeLayout.setContentView(decorChild);
        decor.addView(mSwipeLayout);
    }

    @Override
    public void onBackPressed() {
        mSwipeLayout.closeActivityAnimation();
    }
}
