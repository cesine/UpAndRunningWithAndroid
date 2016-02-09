package com.androidmontreal.gesturevoicecommander.practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidmontreal.gesturevoicecommander.GestureBuilderActivity;
import com.androidmontreal.gesturevoicecommander.R;
import com.androidmontreal.gesturevoicecommander.robots.Lexicon;

import watch.nudge.phonegesturelibrary.AbstractPhoneGestureActivity;

/**
 * Building on what we saw in MakeItListenAndRepeat, now lets make it understand
 * gestures, or speech (sometimes its too noisy or too public to speak to your
 * Android). Here is some super simple code that builds on the GestureBuilder
 * sample code to recognize what the user wants the Android to do, and then use
 * Text To Speech to tell the user what it might have understood.
 *
 * @author cesine
 */
public class MakeItUnderstandGestures extends AbstractPhoneGestureActivity implements OnInitListener, OnGesturePerformedListener {
    private static final String TAG = "MakeItUnderstandGesture";
    private static final int RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE = 341;
    private static final boolean D = true;

    /**
     * Talk to the user
     */
    private TextToSpeech mTts;

    /*
     * A gesture library we created with the GestureBuilder, saved on the SDCard
     * and then imported into the res/raw folder of this project
     */
    private GestureLibrary gestureLib;

    /* A little lexicon we made for the DFR Rover at Cloud Robotics Hackathon */
    private Lexicon lexicon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTts = new TextToSpeech(this, this);

        GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
        View inflate = getLayoutInflater().inflate(R.layout.commander, null);
        gestureOverlayView.addView(inflate);
        gestureOverlayView.addOnGesturePerformedListener(this);
        // gestureLib = GestureLibraries.fromFile(fileOnYourSDCard);
        gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!gestureLib.load()) {
            finish();
        }
        setContentView(gestureOverlayView);

        lexicon = new Lexicon();
    }

    protected void promptTheUserToTalk() {
        if (isIntentAvailable(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)) {
            this.speak(getString(R.string.im_listening));
        } else {
            this.speak(getString(R.string.i_cant_listen));
        }
    }

    /**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.im_listening));
        if (isIntentAvailable(intent)) {
            startActivityForResult(intent, RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE);
        } else {
            Log.w(TAG, "This device doesn't have speech recognition, maybe its an emulator or a phone from china without google products?");
        }
    }

    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
      /* try to find a robot command in the first match */
            if (matches.size() > 0) {
                sendRobotThisCommand(matches.get(0));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language is not available.");
                Toast.makeText(this,
                        "The " + Locale.getDefault().getDisplayLanguage()
                                + " TextToSpeech isn't installed, you can go into the "
                                + "\nAndroid's settings in the "
                                + "\nVoice Input and Output menu to turn it on. ",
                        Toast.LENGTH_LONG).show();
            } else {
                // everything is working.
                this.speak(getString(R.string.instructions_to_look_at_menu));
            }
        } else {
            Toast.makeText(this, "Sorry, I can't talk to you because " +
                    "I could not initialize TextToSpeech.", Toast.LENGTH_LONG).show();
        }
    }

    public boolean speak(String message) {
        if (mTts != null) {
            mTts.speak(message, TextToSpeech.QUEUE_ADD, null);
        } else {
            Toast.makeText(this, "Sorry, I can't speak to you: " + message, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
        for (Prediction prediction : predictions) {
            if (prediction.score > 3.0) {
                Log.d(TAG, "Detected this gesture " + prediction.name + " with a score of " + prediction.score);
            }
        }
        if (predictions.size() > 0) {
            sendRobotThisCommand(predictions.get(0).name);
        }
    }
    @Override
    public void onSnap() {
        sendRobotThisCommand(lexicon.stop());
    }

    @Override
    public void onFlick() {
        sendRobotThisCommand(lexicon.explore());
    }

    @Override
    public void onTwist() {
        sendRobotThisCommand(lexicon.rotateRight());
    }

//These functions won't be called until you subscribe to the appropriate gestures
//in a class that extends AbstractGestureClientActivity in a wear app.

    @Override
    public void onTiltX(float x) {
        Log.e(TAG, "This function should not be called unless subscribed to TILT_X " + x);
        if (x < 0){
            sendRobotThisCommand(lexicon.turnLeft());
        } else {
            sendRobotThisCommand(lexicon.turnRight());
        }
//        throw new IllegalStateException("This function should not be called unless subscribed to TILT_X.");
    }

    @Override
    public void onTilt(float x, float y, float z) {
        Log.e(TAG, "This function should not be called unless subscribed to onTilt." + x + " " + y + " " + z);
    }

    @Override
    public void onWindowClosed() {
        Log.e("MainWatchActivity","This function should not be called unless windowed gesture detection is enabled.");
    }


    public String sendRobotThisCommand(String command) {
        String guessedCommand = lexicon.guessWhatToDo(command);
        Toast.makeText(this, guessedCommand, Toast.LENGTH_SHORT).show();

        if (Locale.getDefault().getLanguage().contains("fr")) {
            mTts.speak(lexicon.FR_CARRIER_PHRASE + guessedCommand, TextToSpeech.QUEUE_ADD, null);
        } else {
            mTts.speak(lexicon.EN_CARRIER_PHRASE + guessedCommand, TextToSpeech.QUEUE_ADD, null);
        }
        return lexicon.executeGuess();
    }

    public void onCommandByVoiceClick(View v) {
        promptTheUserToTalk();
        startVoiceRecognitionActivity();
    }

    public void onViewGesturesClick(View v) {
        Intent i = new Intent(this, GestureBuilderActivity.class);
        startActivity(i);
    }

    public boolean isIntentAvailable(String action) {
        final Intent intent = new Intent(action);
        return isIntentAvailable(intent);
    }

    public boolean isIntentAvailable(final Intent intent) {
        final PackageManager packageManager = this.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
