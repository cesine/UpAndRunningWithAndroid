package com.androidmontreal.arduino.bluetooth;

import com.androidmontreal.opencv.R;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


public class DeviceActivity extends ListActivity
{
	public static final String TAG = DeviceActivity.class.getSimpleName();
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevices;
	
	private static final int REQUEST_ENABLE_BT = 1;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.text_activity_layout);
		setTitle("Select device");
		
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBtAdapter == null){
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
		
		mPairedDevices = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		setListAdapter(mPairedDevices);
		
		getListView().setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				Intent intent = new Intent(DeviceActivity.this, SerialBluetooth.class);
				String[] device = ((String)((TextView)arg1).getText()).split("\n");
				intent.putExtra("device", device[1]);
				startActivity(intent);
			}
		});
		
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		if(!mBtAdapter.isEnabled())
			startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
		else
			populatePairedDevices();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == REQUEST_ENABLE_BT){
			if(resultCode == Activity.RESULT_OK){
				populatePairedDevices();
			}else{
				Toast.makeText(this, "Cannot enable BT", Toast.LENGTH_SHORT).show();
                finish();
			}
		}
	}
	
	private void populatePairedDevices()
	{
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		if(pairedDevices.size() > 0){
			mPairedDevices.clear();
			for(BluetoothDevice device : pairedDevices){
				mPairedDevices.add(device.getName() + "\n" + device.getAddress());
			}
		}else{
			mPairedDevices.add("No devices");
		}
	}
	
}
