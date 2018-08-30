package com.example.ab.seee_chargecontrol;


import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.view.View;
import android.widget.*;
/*import android.util.Log;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import android.app.Service;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;*/



public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();
    static final int CMD_STOP_SERVICE = 0x01;
    static final int CMD_SEND_DATA = 0x02;
    static final int CMD_SYSTEM_EXIT = 0x03;
    static final int CMD_SHOW_TOAST = 0x04;
    private TextView mTitleTextView;
    MyReceiver receiver;
    IBinder serviceBinder;
    MyService mService;
    Intent intent;



    /*private TextView mTitleTextView;
    private TextView mDumpTextView;
    private ScrollView mScrollView;
    private static UsbSerialPort sPort = null;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SerialInputOutputManager mSerialIoManager;
    private byte[] high=new byte[5];*/

    /*private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };*/

    private int BatteryN;
    private boolean result = false;
    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mTitleTextView = (TextView) findViewById(R.id.mTitleTextView);
        //mDumpTextView = (TextView) findViewById(R.id.mDumpTextView);
        //mScrollView = (ScrollView) findViewById(R.id.mScrollView);

        Intent intent = new Intent(MainActivity.this, MyService.class);
        startService(intent);

        BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();


                //如果捕捉到的action是ACTION_BATTERY_CHANGED， 就运行onBatteryInfoReceiver()

                if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                    int level = intent.getIntExtra("level", 0);
                    //电量的总刻度
                    int scale = intent.getIntExtra("scale", 100);
                    //把它转成百分比
                    BatteryN = (level * 100) / scale;
                }

            }
        };

        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


        final Button btn = (Button) findViewById(R.id.button3);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
                EditText editText1 = (EditText) findViewById(R.id.editText);
                String s = editText1.getText().toString();
                TextView txv = (TextView) findViewById(R.id.textView2);
                int setBattery = Integer.parseInt(s);
                if (setBattery <= 0 || setBattery > 100 || editText1.getText().length() == 0)
                    txv.setText("请重新输入");
                else {
                    txv.setText("当前手机电量为：" + BatteryN);
                    //txv.setText("终止充电电量为：" + editText1.getText().toString());
                }
                while (!result) {
                    int battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                    if (battery >= setBattery&&flag) {
                        txv.setText("当前手机电量为：" + battery + "已达到指定电量");
                        result = true;
                        break;
                    }else if(battery >= setBattery&&!flag){
                        byte command = 45;
                        int value = 0x12345;
                        sendCmd(command,value);
                        txv.setText("当前手机电量为：" + battery + "已达到指定电量");
                        result = true;
                        break;
                    }
                    else {
                        try {
                            flag=false;
                            Thread.sleep(5 * 1000);
                            txv.setText("当前手机电量为：" + battery + "未达到指定电量");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(receiver!=null){
            MainActivity.this.unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MyReceiver receiver = new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("android.intent.action.example.ab.seee_chargecontrol");
        MainActivity.this.registerReceiver(receiver,filter);
    }


    public void sendCmd(byte command, int value) {
        Intent intent = new Intent();//创建Intent对象
        intent.setAction("android.intent.action.cmd");
        intent.putExtra("cmd", CMD_SEND_DATA);
        intent.putExtra("command", command);
        intent.putExtra("value", value);
        sendBroadcast(intent);//发送广播
    }

    public void showToast(String str) {//显示提示信息
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
        //mTitleTextView.setText(str);
    }


    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("android.intent.action.example.ab.seee_chargecontrol")) {
                Bundle bundle = intent.getExtras();
                int cmd = bundle.getInt("cmd");

                if (cmd == CMD_SHOW_TOAST) {
                    String str = bundle.getString("str");
                    showToast(str);
                } else if (cmd == CMD_SYSTEM_EXIT) {
                    System.exit(0);
                }

            }
        }
    }

}



    /*@Override
    protected void onResume() {
        super.onResume();
        for (int i=0;i<5;i++)
            high[i]=1;
        Log.d(TAG, "Resumed, port=" + sPort);
        if (sPort == null) {
            mTitleTextView.setText("No serial device.");
        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            UsbDeviceConnection connection = usbManager.openDevice(sPort.getDriver().getDevice());
            if (connection == null) {
                mTitleTextView.setText("Opening device failed");
                return;
            }

            try {
                sPort.open(connection);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                sPort.write(high,2000);

                /*showStatus(mDumpTextView, "CD  - Carrier Detect", sPort.getCD());
                showStatus(mDumpTextView, "CTS - Clear To Send", sPort.getCTS());
                showStatus(mDumpTextView, "DSR - Data Set Ready", sPort.getDSR());
                showStatus(mDumpTextView, "DTR - Data Terminal Ready", sPort.getDTR());
                showStatus(mDumpTextView, "DSR - Data Set Ready", sPort.getDSR());
                showStatus(mDumpTextView, "RI  - Ring Indicator", sPort.getRI());
                showStatus(mDumpTextView, "RTS - Request To Send", sPort.getRTS());*/

            /*} catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                mTitleTextView.setText("Error opening device: " + e.getMessage());
                try {
                    sPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sPort = null;
                return;
            }
            mTitleTextView.setText("Serial device: " + sPort.getClass().getSimpleName());
        }
        onDeviceStateChange();
    }



    /*void showStatus(TextView theTextView, String theLabel, boolean theValue){
        String msg = theLabel + ": " + (theValue ? "enabled" : "disabled") + "\n";
        theTextView.append(msg);
    }

    @Override
    /* protected void onPause() {
        super.onPause();
        stopIoManager();
        if (sPort != null) {
            try {
                sPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            sPort = null;
        }
        finish();
    }

   /* private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

   /* private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    /*private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    /*private void updateReceivedData(byte[] data) {
        final String message = "Read " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n";
        mDumpTextView.append(message);
        mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
    }*/
