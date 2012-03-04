package com.androidmontreal.commander;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.IntentService;
import android.content.Intent;

public class MyRobotsSender extends IntentService {
	public static final String STATUS = "Sending with intent.";
	
	public MyRobotsSender() {
		super("MyRobots");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
    	// MyRobots update URL
    	String url = "http://bots.myrobots.com/update";
    	
    	// Add the key for the RoogleRover
    	url += "?key=5C29C48B3DA44610";
    	
    	// Add the RoogleRover's status
    	try {
			url += "&status=" + URLEncoder.encode(intent.getStringExtra(MyRobotsSender.STATUS), "utf-8");
		} catch (UnsupportedEncodingException e) {
			url += "&status=errorSendingMessage";
		}
    	
    	// Send the status to MyRobots.com
		URLConnectionReader.sendGET(url);
	}	
}
