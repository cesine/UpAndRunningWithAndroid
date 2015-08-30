package com.androidmontreal.gesturevoicecommander.practice;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

import com.androidmontreal.gesturevoicecommander.R;

public class MakeItTalk extends Activity implements OnInitListener {
    private static final String TAG = "MakeItTalk";
    /**
     * Talk to the user
     */
    private TextToSpeech mTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTts = new TextToSpeech(this, this);
    }

    protected void sayFirstWords() {
        mTts.speak(getString(R.string.my_first_words), TextToSpeech.QUEUE_ADD, null);
    }

    @Override
    protected void onDestroy() {
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
                sayFirstWords();
            }
        } else {
            Toast.makeText(this,
                    "Sorry, I can't talk to you because "
                            + "I could not initialize TextToSpeech.", Toast.LENGTH_LONG)
                    .show();
        }
    }
}
