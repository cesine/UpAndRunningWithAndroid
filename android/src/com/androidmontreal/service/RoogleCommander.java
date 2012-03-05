package com.androidmontreal.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class RoogleCommander extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Send "My Status" to MyRobots.com
        Intent svc = new Intent(this, MyRobotsSender.class);
        svc.putExtra(MyRobotsSender.STATUS, "My Status");
        startService(svc);
        
        // Get the latest status from MyRobots.com
        Intent svcReceiver = new Intent(this, MyRobotsSender.class);
        startService(svcReceiver);
        
        // Display "Send status"
        TextView tv = new TextView(this);
        tv.setText("Sent status");
        setContentView(tv);
    }
}