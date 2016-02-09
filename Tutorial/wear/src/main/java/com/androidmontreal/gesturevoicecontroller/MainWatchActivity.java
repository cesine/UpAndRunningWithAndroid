package com.androidmontreal.gesturevoicecontroller;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import watch.nudge.gesturelibrary.AbstractGestureClientActivity;
import watch.nudge.gesturelibrary.DefaultUnwindowedGestureActivity;
import watch.nudge.gesturelibrary.GestureConstants;

public class MainWatchActivity extends AbstractGestureClientActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_watch);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);

        setSubscribeWindowEvents(true);
    }

    @Override
    public ArrayList<GestureConstants.SubscriptionGesture> getGestureSubscpitionList() {
        ArrayList<GestureConstants.SubscriptionGesture> gestures = new ArrayList<GestureConstants.SubscriptionGesture>();
        gestures.add(GestureConstants.SubscriptionGesture.FLICK);
        gestures.add(GestureConstants.SubscriptionGesture.SNAP);
        gestures.add(GestureConstants.SubscriptionGesture.TWIST);
        gestures.add(GestureConstants.SubscriptionGesture.TILT_X);
        gestures.add(GestureConstants.SubscriptionGesture.TILT);
        return gestures;
    }

    @Override
    public void onSnap() {
        Toast.makeText(this,"Stop", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFlick() {
        Toast.makeText(this,"Explore",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTwist() {
        Toast.makeText(this,"Rotate",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGestureWindowClosed() {
        Toast.makeText(this,"Gesture window closed.",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTiltX(float x) {
        Toast.makeText(this,"onTiltX " + x,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTilt(float x, float y, float z) {
        Toast.makeText(this,"onTilt " + x + " " + y + " " + z,Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean sendsGestureToPhone() {
        return true;
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTextView.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            mTextView.setTextColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }
}
