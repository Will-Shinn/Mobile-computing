package com.mobile.daryldaryl.mobile_computing.tools;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.mobile.daryldaryl.mobile_computing.R;

public class SwipeLayout extends LinearLayout {
    public static final String TAG = "SwipeLayout";

    private View mEmptyView;
    private View mContentView;

    private int mLeftEdge;
    private int mWidth;
    private int mMaxScrollX;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker = null;
    private int mMaxFlingVelocity;
    private int mLastX;

    ViewGroup.LayoutParams childParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    private Context mContext;
    public static final int DURATION = 1500;
    public static final int OPEN_ANIM_DURATION = 1000;
    public static int SNAP_VELOCITY = 600;

    private OnFinishListener mOnFinishListener;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        init();
    }

    public void setOnFinishListener(OnFinishListener mOnFinishListener) {
        this.mOnFinishListener = mOnFinishListener;
    }

    void init() {
        mScroller = new Scroller(mContext);
        mMaxFlingVelocity = ViewConfiguration.get(mContext).getScaledMaximumFlingVelocity();

        mWidth = DisplayUtils.getScreenWidth(mContext) * 2;
        mMaxScrollX = mWidth / 2;
        mLeftEdge = mMaxScrollX - mMaxScrollX / 3;

        setOrientation(LinearLayout.HORIZONTAL);

        childParams.width = DisplayUtils.getScreenWidth(mContext);

        mEmptyView = LayoutInflater.from(mContext).inflate(R.layout.view_translate, null);

        addView(mEmptyView, childParams);
    }

    public void setContentView(View contentView) {
        if (mContentView != null) {
            removeView(mContentView);
        }
        mContentView = contentView;
        addView(contentView, childParams);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                openActivityAnimation();
            }
        }, 200);
    }


    private VelocityTracker getVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        return mVelocityTracker;
    }


    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        getVelocityTracker();

        mVelocityTracker.addMovement(ev);

        int pointId = -1;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

//                clearScrollHis();
                mLastX = (int) ev.getX();
                pointId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                int nextScrollX = (int) (mLastX - ev.getX() + getScrollX());

                if (scrollTo(nextScrollX)) {
                    mLastX = (int) ev.getX();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);

                float vX = mVelocityTracker.getXVelocity(pointId);

                Log.i(TAG, "mVelocityX:" + vX);


                if (vX > SNAP_VELOCITY) {
                    scrollToLeft();
                } else if (vX < -SNAP_VELOCITY) {
                    scrollToRight();
                } else {
                    snapToDestation();
                }


                recycleVelocityTracker();
                break;
        }
        return true;
    }

    private void openActivityAnimation() {
        clearScrollHis();
        mScroller.startScroll(getScrollX(), 0, mMaxScrollX - getScrollX(), 0, OPEN_ANIM_DURATION);
        invalidate();
    }

    public void closeActivityAnimation() {
        clearScrollHis();
        mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, OPEN_ANIM_DURATION);
        invalidate();
    }

    private void clearScrollHis() {
        if (mScroller != null) {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
        }
    }


    private void snapToDestation() {
        int scrollX = getScrollX();
        if (scrollX > 0 && scrollX <= mLeftEdge) {
            smoothScrollTo(0);
        } else if (scrollX > mLeftEdge) {
            smoothScrollTo(mMaxScrollX);
        }
    }


    public boolean scrollTo(int x) {
        if (x < 0) {
            scrollTo(0, 0);
        } else if (x > mMaxScrollX) {
            scrollTo(mMaxScrollX, 0);
        } else {
            scrollTo(x, 0);
        }
        return true;
    }

    public void scrollToRight() {
        smoothScrollTo(mMaxScrollX);
    }

    public void scrollToLeft() {
        smoothScrollTo(0);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        Log.d(TAG, "left:" + l);

        if (l == 0) {
            Log.d(TAG, "OnFinish");


            if (mOnFinishListener != null) {
                mOnFinishListener.onFinish();
            }
        }
    }

    public void smoothScrollTo(int fx) {
        if (fx < 0) {
            smoothScrollTo(0, 0);
        } else if (fx > mMaxScrollX) {
            smoothScrollTo(mMaxScrollX, 0);
        } else {
            smoothScrollTo(fx, 0);
        }
    }


    public void smoothScrollTo(int fx, int fy) {
        int dx = fx - getScrollX();
        int dy = fy - getScrollY();
        smoothScrollBy(dx, dy);
    }


    public void smoothScrollBy(int dx, int dy) {


        mScroller.startScroll(getScrollX(), 0, dx, dy, Math.abs(dx * DURATION / mMaxScrollX));
        invalidate();
    }

    @Override
    public void computeScroll() {


        if (mScroller.computeScrollOffset()) {


            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());


            postInvalidate();
        }
        super.computeScroll();
    }


    public interface OnFinishListener {
        void onFinish();
    }
}
