package system.embedded.pierre.info0064;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import static system.embedded.pierre.info0064.Constants.STATE_CAL;

public class MainActivity extends FragmentActivity {

    private Button openButton;
    private Button closeButton;
    private Button exitButton;
    private Button nextButton;
    private TextView textView;
    private TextView actionToDO;
    private ImageView icon;

    // Local Bluetooth adapter
    private BluetoothService bluetoothService;
    private BluetoothDevice device;
    private BluetoothHandler handler;

    private AlertDialog dialog;

    private int state;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        state = Constants.STATE_USE;
        device = getIntent().getExtras().getParcelable("device");
        textView = (TextView) findViewById(R.id.ConnectedDevice);
        if (device == null) {
            textView.setText(R.string.no_device);
        } else {
            Resources res = getResources();
            String s = String.format(res.getString(R.string.connecting), device.getName());
            textView.setText(s);
        }
        exitButton = (Button) findViewById(R.id.buttonCloseApp);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });
        handler = new BluetoothHandler(MainActivity.this);
        bluetoothService = new BluetoothService(handler,device);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        dialog = new AlertDialog.Builder(MainActivity.this).create();
        switch (item.getItemId()) {
            case R.id.help:
                dialog.setTitle("Help");
                dialog.setMessage("This app allows to control the INFO0064 project");
                break;
            case R.id.calibrate:
                state = STATE_CAL;
                sendMessage("3");
                setContentView(R.layout.calibrate);
                break;
            case R.id.credits:
                dialog.setTitle("Credits");
                dialog.setMessage("Nicolay Pierre");
                break;
            default:
                super.onOptionsItemSelected(item);
                break;
        }
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
        dialog.getButton(dialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_layout, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothService.connect();
        Log.d(Constants.TAG, "Connecting");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bluetoothService != null) {
            bluetoothService.stop();
            Log.d(Constants.TAG, "Stopping");
        }

    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (bluetoothService.getState() != Constants.STATE_CONNECTED) {
            Log.d(Constants.TAG, "Disconected");
        } else {
            byte[] send = message.getBytes();
            bluetoothService.write(send);
        }
    }

    private class BluetoothHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public BluetoothHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            final MainActivity activity = mActivity.get();

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_CONNECTED:
                            Resources res = getResources();
                            String s = String.format(res.getString(R.string.connected), device.getName());
                            activity.textView.setText(s);
                            activity.closeButton = (Button) activity.findViewById(R.id.buttonCloseDoor);
                            activity.closeButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    activity.sendMessage("2");
                                }
                            });
                            activity.openButton = (Button) activity.findViewById(R.id.buttonOpenDoor);
                            activity.openButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    activity.sendMessage("1");
                                }
                            });
                            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                            break;
                        case Constants.STATE_CONNECTING:
                            Toast.makeText(getApplicationContext(), "Connecting", Toast.LENGTH_SHORT).show();
                            break;
                        case Constants.STATE_NONE:
                            Toast.makeText(getApplicationContext(), "Not Connected", Toast.LENGTH_SHORT).show();
                            break;
                        case Constants.STATE_ERROR:
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    break;
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    if (state == Constants.STATE_CAL){
                        if(readMessage.equals("OK\r\n")){
                            setContentView(R.layout.activity_main);
                        }else {
                            actionToDO = (TextView) findViewById(R.id.action);
                            icon = (ImageView) findViewById(R.id.actionIcon);
                            nextButton = (Button) findViewById(R.id.next);
                            if (readMessage.equals("hand\r\n")) {
                                actionToDO.setText(R.string.hand);
                                icon.setImageResource(R.drawable.hand);

                            } else if (readMessage.equals("noHand")) {
                                actionToDO.setText(R.string.no_hand);
                                icon.setImageResource(R.drawable.no_hand);
                            }
                            actionToDO.setVisibility(View.VISIBLE);
                            icon.setVisibility(View.VISIBLE);
                            nextButton.setVisibility(View.VISIBLE);
                            nextButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    activity.sendMessage("1");
                                }
                            });
                        }
                    }
                    break;
                case Constants.MESSAGE_SNACKBAR:
                    break;
            }
        }
    }

}