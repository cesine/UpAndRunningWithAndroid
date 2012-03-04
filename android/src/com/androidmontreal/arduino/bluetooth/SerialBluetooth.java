package com.androidmontreal.arduino.bluetooth;

import com.androidmontreal.opencv.R;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.UUID;


public class SerialBluetooth extends ListActivity
{
	public static final String TAG = SerialBluetooth.class.getSimpleName();
	private BluetoothAdapter mBtAdapter;
	private BluetoothDevice mBtDevice;
	private BluetoothThread mBtThread;
	private ArrayAdapter<String> mTextEntries;
	private Button mSendButton;
	private EditText mInputBox;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.text_activity_layout);
//		getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		
		mTextEntries = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		mTextEntries.add("Each message sent and received will appear below.");
		setListAdapter(mTextEntries);
		
		View inputPanel = findViewById(R.id.input_panel);
		inputPanel.setVisibility(View.VISIBLE);
		
		mInputBox = (EditText)findViewById(R.id.input_box);
		mSendButton = (Button)findViewById(R.id.send_button);
		mSendButton.setOnClickListener(mSendClicked);
		
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		mBtDevice  = mBtAdapter.getRemoteDevice(getIntent().getStringExtra("device"));
		
		setConnectedState(false);
		
		try{
			mBtAdapter.cancelDiscovery();
			mBtThread  = new BluetoothThread();
			mBtThread.connectToDevice();
		}
		catch(IOException e){
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}
	
	@Override
	protected void onStop()
	{
		mBtThread.quit();
		super.onStop();
	}
	
	private void setConnectedState(boolean connected)
	{
		if(connected){
//			setProgressBarIndeterminateVisibility(false);
//			setTitle("Talking with " + mBtDevice.getName() + " (" + mBtDevice.getAddress() + ")");
			mSendButton.setEnabled(true);
		}else{
//			setProgressBarIndeterminateVisibility(true);
//			setTitle("Connecting to " + mBtDevice.getName() + " (" + mBtDevice.getAddress() + ")");
			mSendButton.setEnabled(false);
		}
	}
	
	private OnClickListener mSendClicked = new OnClickListener()
	{
		public void onClick(View arg0)
		{
			Editable inputText = mInputBox.getText();
			mBtThread.sendTextMessage(inputText.toString());
			inputText.clear();	
		}
	};
	
	private class BluetoothThread extends HandlerThread implements Handler.Callback
	{
		BluetoothSocket mBtSocket;
		BufferedReader mInput;
		BufferedWriter mOutput;
		Handler mHandler;
		
		public static final int MESSAGE_CONNECT = 1;
		public static final int MESSAGE_SEND    = 2;
		public static final int MESSAGE_RECV    = 3;
		
		
		public BluetoothThread() throws IOException
		{
			super("Bluetooth Thread");
			start();
			mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(
				UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				// Bluetooth SPP Serial Port 0x1101
			mHandler  = new Handler(getLooper(), this);
			mHandler.sendEmptyMessageDelayed(MESSAGE_RECV, 100);
		}
		
		@Override
		public boolean quit()
		{
			mHandler.removeMessages(MESSAGE_RECV);
			return super.quit();
		}
		
		public void connectToDevice()
		{
			mHandler.sendEmptyMessage(BluetoothThread.MESSAGE_CONNECT);
		}
		
		public void sendTextMessage(String message)
		{
			mHandler.obtainMessage(MESSAGE_SEND, message).sendToTarget();
		}
		
		public boolean handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case MESSAGE_CONNECT:
					{
						try{
							mBtSocket.connect();
							mInput  = new BufferedReader(new InputStreamReader(mBtSocket.getInputStream()));
							mOutput = new BufferedWriter(new OutputStreamWriter(mBtSocket.getOutputStream()));
							runOnUiThread(new Runnable()
							{
								public void run()
								{
									setConnectedState(true);
								}
							});
						}
						catch(IOException e){
							Toast.makeText(SerialBluetooth.this, e.getMessage(), Toast.LENGTH_LONG).show();
							finish();
						}
					}
					return true;
					
				case MESSAGE_SEND:
					{
						String text = (String)msg.obj;
						try{
							if(mOutput != null){
								mOutput.append(text);
								mOutput.newLine();
								mOutput.flush();
								mTextEntries.add("> " + text);
							}else{
								throw new IOException("No Output Stream");
							}
						}
						catch(IOException e){
							final String errMsg = e.getMessage(); 
							runOnUiThread(new Runnable()
							{
								public void run()
								{
									mTextEntries.add("! Failed to send message: " + errMsg);
								}
							});
						}
					}
					return true;
					
				case MESSAGE_RECV:
					{
						try{
							if(mInput != null && mInput.ready()){
								String text = mInput.readLine();
								if(text != null)
									mTextEntries.add("< " + text);
							}
						}
						catch(IOException e){
							final String errMsg = e.getMessage(); 
							runOnUiThread(new Runnable()
							{
								public void run()
								{
									mTextEntries.add("! Failed to recv message: " + errMsg);
								}
							});
						}
						mHandler.sendEmptyMessageDelayed(MESSAGE_RECV, 100);
					}
					return true;
			}
			
			return false;
		}
		
	};
}
