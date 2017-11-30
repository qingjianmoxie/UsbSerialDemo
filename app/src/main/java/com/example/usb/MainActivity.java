package com.example.usb;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnSend;

    private UsbService usbService;

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSend = (Button) findViewById(R.id.btn_send);
        startService(UsbService.class, usbConnection, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_RECEIVER);
        this.registerReceiver(this.usbReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(usbConnection);
        unregisterReceiver(usbReceiver);
    }



    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UsbService.ACTION_USB_RECEIVER)) {
                String data = intent.getStringExtra("usbData");
                Log.d("MainActivity", data);
            }
        }
    };

    public void walkUSBChannel(String motion) {
        if (usbService != null) {
            byte[] bOutArray = HexUtils.HexToByteArr(motion);
            usbService.write(bOutArray);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_send:
                walkUSBChannel("你的指令");
                break;
        }
    }
}
