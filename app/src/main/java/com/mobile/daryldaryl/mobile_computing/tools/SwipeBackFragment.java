package com.mobile.daryldaryl.mobile_computing.tools;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public abstract class SwipeBackFragment extends Fragment {

    private SwipeLayout mSwipeLayout;
    private Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(getContentLayout(), null);
        mSwipeLayout = new SwipeLayout(mContext);
        mSwipeLayout.setContentView(contentView);
        mSwipeLayout.setOnFinishListener(new SwipeLayout.OnFinishListener() {
            @Override
            public void onFinish() {
                getActivity().onBackPressed();
            }
        });
        return mSwipeLayout;
    }

    @Override
    public void onResume() {
        super.onResume();

        mSwipeLayout.setFocusableInTouchMode(true);
        mSwipeLayout.requestFocus();
        mSwipeLayout.setOnKeyListener(backlistener);
    }


    private View.OnKeyListener backlistener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                onBackPressed();
                return true;
            }
            return false;
        }
    };

    public void onBackPressed() {
        mSwipeLayout.closeActivityAnimation();
    }

    public abstract int getContentLayout();
}
