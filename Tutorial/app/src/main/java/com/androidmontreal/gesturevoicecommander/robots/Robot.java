package com.androidmontreal.gesturevoicecommander.robots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidmontreal.gesturevoicecommander.GestureBuilderActivity;
import com.androidmontreal.gesturevoicecommander.R;

/**
 * Building on what we saw in MakeItUnderstand, now lets make it perform
 * actions. Here is some super simple code that builds on the BluetoothChat
 * sample code to send meassages to a bluetooth device/robot.
 *
 * @author cesine
 */
public class Robot extends Activity implements OnInitListener, OnGesturePerformedListener {
    private static final String TAG = "Robot";
    private static final int RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE = 341;
    public static final int REQUEST_CONNECT_DEVICE = 8888;

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

    /* A re-executable sequence of commands in time */
    private HashMap<Long, String> mCommandMemory;

    /* Message passing to an actual bluetooth device/robot */
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService;
    private String mConnectedDeviceName = "";
    private static final boolean SECURE_CONNECTION = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTts = new TextToSpeech(this, this);

        GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
        View inflate = getLayoutInflater().inflate(R.layout.commander, null);
        gestureOverlayView.addView(inflate);
        gestureOverlayView.addOnGesturePerformedListener(this);
        // gestureLib = GestureLibraries.fromFile(fileOnYourSDCard);
        gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!gestureLib.load()) {
            Toast.makeText(this, R.string.gestures_empty, Toast.LENGTH_SHORT).show();
            finish();
        }
        setContentView(gestureOverlayView);

        lexicon = new Lexicon();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCommandMemory == null) {
            mCommandMemory = new HashMap<Long, String>();
        }

        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
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
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RETURN_FROM_VOICE_RECOGNITION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    /* try to find a robot command in the first match */
                    if (matches.size() > 0) {
                        sendRobotThisCommand(matches.get(0));
                    }
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    if (mChatService == null) {
                        mChatService = new BluetoothChatService(this, mHandler);
                    }
                    connectDevice(data);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }

        if (mChatService != null) {
            mChatService.stop();
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

    /**
     * Establish connection with a physical device/body via bluetooth
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     */
    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, SECURE_CONNECTION);
    }

    public String sendRobotThisCommand(String requestedCommand) {
        String understoodCommand = lexicon.guessWhatToDo(requestedCommand);

        // communicate understood command
        Toast.makeText(this, understoodCommand, Toast.LENGTH_SHORT).show();
        if (Locale.getDefault().getLanguage().contains("fr")) {
            mTts.speak(lexicon.FR_CARRIER_PHRASE + understoodCommand, TextToSpeech.QUEUE_ADD, null);
        } else {
            mTts.speak(lexicon.EN_CARRIER_PHRASE + understoodCommand, TextToSpeech.QUEUE_ADD, null);
        }

        // remember understood command
        mCommandMemory.put(System.currentTimeMillis(), "I want to: " + understoodCommand);

        // translate into body commands
        String bodyCommand = lexicon.executeGuess();
        if ("CONNECT".equals(bodyCommand)) {
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return "";
        }
        if (mChatService != null){
            // Check that we're actually connected before trying anything
            if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                Toast.makeText(this, R.string.bluetooth_not_connected, Toast.LENGTH_SHORT).show();
                return understoodCommand;
            }
            // Check that there's actually something to send
            if (bodyCommand.length() > 0) {
                // Get the message bytes and tell the BluetoothChatService to write
                byte[] send = bodyCommand.getBytes();
                mChatService.write(send);
            }
        }

        return understoodCommand;
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothChatService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            Log.d(TAG, getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            Log.d(TAG, "title_connecting");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                            break;
                        case BluetoothChatService.STATE_NONE:
                            Log.d(TAG, "title_not_connected");
                            break;
                    }
                    break;
                case BluetoothChatService.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String bodyCommand = new String(writeBuf);
                    mCommandMemory.put(System.currentTimeMillis(), "I told my body to:  " + bodyCommand);
                    break;
                case BluetoothChatService.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    speak(readMessage);
                    mCommandMemory.put(System.currentTimeMillis(), "My body did:  " + readMessage);
                    break;
                case BluetoothChatService.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(BluetoothChatService.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "My body is: "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothChatService.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothChatService.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

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
