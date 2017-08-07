package system.embedded.pierre.info0064;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private final int REQUEST_ENABLE_BT = 1;
    private MyAdapter adapterDisco;
    private MyAdapter adapterPaired;
    private final BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Set<BluetoothDevice> pairedDevices;


    private final BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                        enableBluetooth();
                        break;
                }

            }
            else if (action.equals(BluetoothDevice.ACTION_FOUND)){
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!pairedDevices.contains(device)) {
                    adapterDisco.add(device);
                    adapterDisco.notifyDataSetChanged();
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (myBluetoothAdapter == null) {
            //Device doeas not support Bluetooth, the applicatioon won't work on this device.
            super.onCreate(savedInstanceState);
            setContentView(R.layout.no_bluetooth);
            Button btn1 = (Button) findViewById(R.id.buttonCloseApp);
            btn1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    finish();
                    System.exit(0);
                }
            });
        } else {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.devices);

            if (!myBluetoothAdapter.isEnabled()) {
                enableBluetooth();
            }

            /*filter for state change of bluetooth*/
            IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(myBroadcastReceiver, filter1);
            /*filter for discovering devices*/
            IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(myBroadcastReceiver,filter2);

            adapterDisco = new MyAdapter(this);
            ListView lv1 = (ListView)findViewById(R.id.discoveredDevice);
            lv1.setAdapter(adapterDisco);

            adapterPaired = new MyAdapter(this);
            ListView lv2 = (ListView)findViewById(R.id.pairedDevice);
            lv2.setAdapter(adapterPaired);

            pairedDevices = myBluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                adapterPaired.addAll(pairedDevices);
            }

            lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    myBluetoothAdapter.cancelDiscovery();
                    BluetoothDevice device = adapterDisco.getItem(position);
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    intent.putExtra("device",device);
                    startActivity(intent);
                }
            });

            lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    myBluetoothAdapter.cancelDiscovery();
                    BluetoothDevice device = adapterPaired.getItem(position);
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    intent.putExtra("device",device);
                    startActivity(intent);
                }
            });

            if(myBluetoothAdapter.isDiscovering())
                myBluetoothAdapter.cancelDiscovery();

            myBluetoothAdapter.startDiscovery();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_ENABLE_BT){
            // Make sure the request was successful
            if (resultCode == RESULT_CANCELED) {
                finish();
                System.exit(0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myBluetoothAdapter.cancelDiscovery();
        unregisterReceiver(myBroadcastReceiver);
    }

    private void enableBluetooth(){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }
}
