package com.androidmontreal.arduino.bluetooth;

import com.androidmontreal.opencv.R;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BluetoothLister extends ListActivity {
	private static final int REQUEST_ENABLE_BT = 0;
	public static final String EXTRA_PROBE_INFO = "probeinfo";
	public static final String EXTRA_PROBE_MAC = "probemac";
    protected static final String TAG = "InspecTire";
	
	TextView selection;
	ListView list;

	private BluetoothAdapter mBluetoothAdapter;
	protected ArrayList<String> mArrayAdapter;
	
    private LayoutInflater mInflater;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		selection = (TextView) findViewById(R.id.selection);
		
        mArrayAdapter = new ArrayList<String>();
       
        
        ArrayAdapter listItemAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mArrayAdapter);
        setListAdapter(listItemAdapter);
        
		int ready = prepareBluetooth();
		if (ready == REQUEST_ENABLE_BT) {
			// waiting for dialog to come back
		} else if (ready == 1) {
			String status = "The bluetooth is already ready.";
			getPneuLogicProbe();
		} else if (ready == 0) {
			String status = "There is no bluetooth on this device.";
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String text = mArrayAdapter.get(position);
		selection.setText(text);
		Log.d(TAG, "Got selection: "+ text);
		Intent intent = this.getIntent();
		intent.putExtra(EXTRA_PROBE_INFO , text);
		
		Pattern macAddress = Pattern.compile("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}"); 
		Matcher matcher = macAddress.matcher(text);
		while (matcher.find()) {
			text =  matcher.group();
		}
		intent.putExtra(EXTRA_PROBE_MAC, text);
		this.setResult(RESULT_OK, intent);
		finish();
	}

	// http://stackoverflow.com/questions/8034237/android-bluetooth-paired-devices-list
	// Created new CachedBluetoothDevice: 00:80:98:F6:DD:94
	// 12-31 10:51:59.780: D/BluetoothEventLoop(150): Device property changed:
	// 00:80:98:F6:DD:94 property: Alias value: Pneu Logic Probe D10I018
	private int prepareBluetooth() {

		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			return 0;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			return REQUEST_ENABLE_BT;
		}

		return 1;
	}

	private int getPneuLogicProbe() {
		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a
				// ListView
				String deviceInfo = "";
				if (device.getName() != null) {
					deviceInfo = "MAC: " + device.getAddress() + ", Name: " + device.getName() ;
				} else {
					deviceInfo = "MAC: " + device.getAddress() + ", Name: " + "Unknown name";
				}
				mArrayAdapter.add(deviceInfo);

			}
//			Intent discoverableIntent = new Intent(
//					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//			discoverableIntent.putExtra(
//					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//			startActivity(discoverableIntent);

		}
		return 0;
	}

	public void onReceivedBroadcast(Context context, Intent intent) {

		String action = intent.getAction();
		// When discovery finds a device
		Log.d(TAG,"Got an actionfrom the bluetooth "+action);
		if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			// Get the BluetoothDevice object from the Intent
			BluetoothDevice device = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			// Add the name and address to an array adapter to show in a
			// ListView
			String deviceInfo = "";
			if (device.getName() != null) {
				deviceInfo = "MAC: " + device.getAddress() + ", Name: " + device.getName();
			} else {
				deviceInfo = "MAC: " + device.getAddress() + ", Name: " + "Unknown name";
			}
			mArrayAdapter.add(deviceInfo);
		}else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
			Log.d(TAG,"Got an ACL Connection from the bluetooth "+action);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				getPneuLogicProbe();
			}
		}

	}
}
