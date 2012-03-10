package com.androidmontreal.gesture.commander;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.gesture.builder.GestureBuilderActivity;
import com.androidmontreal.arduino.commander.R;

public class GestureVoiceCommander extends Activity implements
		OnGesturePerformedListener , TextToSpeech.OnInitListener {
	private GestureLibrary gestureLib;
	// Debugging
	private static final String TAG = "RoogleCommander";
	private static final boolean D = true;

	/** Talk to the user */
	private TextToSpeech mTts;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
		View inflate = getLayoutInflater().inflate(R.layout.commander, null);
		gestureOverlayView.addView(inflate);
		gestureOverlayView.addOnGesturePerformedListener(this);
		gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!gestureLib.load()) {
			finish();
		}
		setContentView(gestureOverlayView);
		mTts = new TextToSpeech(this, this);
        
	}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
		for (Prediction prediction : predictions) {
			if (prediction.score > 3.0) {
				Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT)
						.show();
				Log.d(TAG, "Detected this gesture " + prediction.name
						+ " with a score of " + prediction.score);
			}
		}
		if(predictions.size() > 0){
			sendCommand(predictions.get(0).name);
		}
	}

	public String sendCommand(String command){
		mTts.speak("I will tell the robot to "+command,TextToSpeech.QUEUE_ADD, null);
		return "Sent";
	}
	public void onViewGesturesClick(View v) {
		Intent i = new Intent(this, GestureBuilderActivity.class);
		startActivity(i);
	}

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			// Set preferred language to US english.
			// Note that a language may not be available, and the result will
			// indicate this.
			int result = mTts.setLanguage(Locale.US);
			// Try this someday for some interesting results. TODO localize
			// int result mTts.setLanguage(Locale.FRANCE);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
								Log.e(TAG, "Language is not available.");
				 Toast.makeText(this,
				 "The English TextToSpeech isn't installed, you can go into the \nAndroid's settings in the \nVoice Input and Output menu to turn it on. ",
				 Toast.LENGTH_LONG).show();
			} else {
				// everything is working.
			}
		} else {
			Log.e(TAG,
					"Sorry, I can't talk to you because I could not initialize TextToSpeech.");
		}
	}

	@Override
	protected void onDestroy() {
		if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
		super.onDestroy();
	}
	
}