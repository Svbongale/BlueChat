package com.example.bluechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class Divice_list_activity extends AppCompatActivity {

    private ProgressBar progessScanDevices;
    private ListView listPairedDevices, listAvailableDevices;
    private ArrayAdapter<String> adapterPairedDevices, adapterAvailableDevices;
    private Context context;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_divice_list);
        context = this;
        init();
    }


    private  void  init(){
        listPairedDevices = findViewById(R.id.list_paired_devices);
        listAvailableDevices = findViewById(R.id.list_available_devices);
        progessScanDevices =findViewById(R.id.progress_scan_devices);


        adapterPairedDevices = new ArrayAdapter<String>(context, R.layout.device_list_item);
        adapterAvailableDevices = new ArrayAdapter<String>(context,R.layout.device_list_item);

        listPairedDevices.setAdapter(adapterPairedDevices);
        listAvailableDevices.setAdapter(adapterAvailableDevices);

        listAvailableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String info = ((TextView)view).getText().toString();
                String address = info.substring(info.length() - 17);
                Intent intent = new Intent();
                intent.putExtra("Device Address",address);
                setResult(RESULT_OK,intent);
                finish();   
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if(pairedDevices!=null && pairedDevices.size()>0){
            for(BluetoothDevice device: pairedDevices){
                adapterPairedDevices.add(device.getName()+"\n" + device.getAddress());
            }
        }

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothDeviceListener, intentFilter);
        IntentFilter intentFilter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothDeviceListener, intentFilter1);
    }

    private BroadcastReceiver bluetoothDeviceListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState()!=BluetoothDevice.BOND_BONDED){
                    adapterAvailableDevices.add(device.getName() + "\n" + device.getAddress());
                }
            }else  if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                progessScanDevices.setVisibility(View.GONE);
                if(adapterAvailableDevices.getCount()==0){
                    Toast.makeText(context,"No new devices found", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(context,"Click on the device to start the chat", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_list,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.str_menu_scan_devices:
                scanDevices();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private  void  scanDevices(){
        progessScanDevices.setVisibility(View.VISIBLE);
        adapterAvailableDevices.clear();

        Toast.makeText(context, "Scan Started", Toast.LENGTH_SHORT).show();

        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }
}