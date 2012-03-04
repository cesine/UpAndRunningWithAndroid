/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidmontreal.arduino.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidmontreal.opencv.OpenCVPreview;
import com.androidmontreal.opencv.R;

/**
 * This is the main Activity that displays the current chat session.
 */
public class RoogleTank extends Activity implements PictureCallback{
    // Debugging
    private static final String TAG = "RoogleTank";
    private static final boolean D = true;

    // OpenCV
    private TextView textView1;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        
        setContentView(R.layout.main);
       
        /* Create a TextView and set its content.
         * the text is retrieved by calling a native
         * function.
         */
        textView1 = (TextView) findViewById(R.id.textview1);
        textView1.setText( stringFromJNI() );
        
        
        
        getOpenCVResult(textView1);
    }
    public void getOpenCVResult(View view){
    	UpdateFromOpenCVTask getOpenCVResults = new UpdateFromOpenCVTask();
        getOpenCVResults.execute(new String[] { "in execute" });
    
    }
    private class UpdateFromOpenCVTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			
			Log.d(TAG,"Pausing 1 sec before calling again.");
			// Loop every 1 sec
			try {
				new Thread().sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			getOpenCVResult(textView1);
			
			
			String response = "hi in async";
			response = ((RoogleTankApp) getApplication()).getLastMessage(); 
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			textView1.setText(result);
		}
	}


    public void onCaptureClick(View v) {
		Button capture = (Button) findViewById(R.id.capture);
		capture.setEnabled(false);

		// Take picture
		OpenCVPreview previewView = (OpenCVPreview) findViewById(R.id.preview);
		Camera camera = previewView.getCamera();
		camera.takePicture(null, null, this);

    }
    public void onPictureTaken(byte[] data, Camera camera) {
		/*
		 * Do some thing
		 */
		finish();
	}
    /* A native method that is implemented by the
     * 'hello-jni' native library, which is packaged
     * with this application.
     */
    public native String  stringFromJNI();
    static {
        System.loadLibrary("opencv_sample");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }


}