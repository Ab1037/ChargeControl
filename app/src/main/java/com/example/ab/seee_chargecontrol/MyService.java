package com.example.ab.seee_chargecontrol;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class MyService extends Service {

    public boolean threadFlag = true;
    MyThread myThread;
    CommandReceiver cmdReceiver;//继承自BroadcastReceiver对象，用于得到Activity发送过来的命令

    static final int CMD_STOP_SERVICE = 0x01;
    static final int CMD_SEND_DATA = 0x02;
    static final int CMD_SYSTEM_EXIT =0x03;
    static final int CMD_SHOW_TOAST =0x04;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;
    public boolean bluetoothFlag = true;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        cmdReceiver = new CommandReceiver();
        IntentFilter filter = new IntentFilter();//创建IntentFilter对象
        //注册一个广播，用于接收Activity传送过来的命令，控制Service的行为，如：发送数据，停止服务等
        filter.addAction("android.intent.action.cmd");
        //注册Broadcast Receiver
        registerReceiver(cmdReceiver, filter);
        myStartService();//调用方法启动线程
        return super.onStartCommand(intent, flags, startId);
    }



    public void myBtConnect() {
        showToast("连接中...");

        /* Discovery device */
//	BluetoothDevice mBtDevice = mBtAdapter.getRemoteDevice(HC_MAC);
        BluetoothDevice mBtDevice = null;
        Set<BluetoothDevice> mBtDevices = mBluetoothAdapter.getBondedDevices();
        if ( mBtDevices.size() > 0 ) {
            for (Iterator<BluetoothDevice> iterator = mBtDevices.iterator();
                 iterator.hasNext(); ) {
                mBtDevice = (BluetoothDevice)iterator.next();
                showToast(mBtDevice.getName() + "|" + mBtDevice.getAddress());
            }
        }

        try {
            btSocket = mBtDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
            bluetoothFlag = false;
            showToast("Create bluetooth socket error");
        }

        mBluetoothAdapter.cancelDiscovery();

        /* Setup connection */
        try {
            btSocket.connect();
            showToast("已成功连接蓝牙");
        } catch (IOException e) {
            e.printStackTrace();
            try {
                showToast("连接错误");
                btSocket.close();
                bluetoothFlag = false;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        /* I/O initialize */
        if ( bluetoothFlag ) {
            try {
                inStream  = btSocket.getInputStream();
                outStream = btSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        showToast("蓝牙就绪！");
    }

    private void myStartService() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if ( mBluetoothAdapter == null ) {
            showToast("未使用蓝牙");
            bluetoothFlag  = false;
            return;
        }
        if ( !mBluetoothAdapter.isEnabled() ) {
            bluetoothFlag  = false;
            stopService();
            showToast("请打开蓝牙并重启APP");
            return;
        }

        showToast("开始搜索");
        threadFlag = true;
        MyThread mThread = new MyThread();
        mThread.start();
    }

    public class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            myBtConnect();
            while( threadFlag ) {
                readSerial();
                try{
                    Thread.sleep(30);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }



    public void DisplayToast(String str)
    {
        Log.d("Season",str);
    }


    private class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("android.intent.action.cmd")){
                int cmd = intent.getIntExtra("cmd", -1);//获取Extra信息
                if(cmd == CMD_STOP_SERVICE){
                    stopService();
                }

                if(cmd == CMD_SEND_DATA)
                    writeSerial();

            }
        }
    }

    public void stopService(){//停止服务
        threadFlag = false;//停止线程
        stopSelf();//停止服务
    }

    public void writeSerial() {
        if(!bluetoothFlag){
            return;
        }
        byte[] b=new byte[5];
        for(int i=0;i<5;i++)
            b[i]=1;
        try {
            outStream.write(b);
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int readSerial() {
        int ret = 0;
        byte[] rsp = null;

        if ( !bluetoothFlag ) {
            return -1;
        }
        try {
            rsp = new byte[inStream.available()];
            ret = inStream.read(rsp);
            showToast(new String(rsp));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    public void showToast(String str){
        Intent intent = new Intent();
        intent.putExtra("cmd", CMD_SHOW_TOAST);
        intent.putExtra("str", str);
        intent.setAction("android.intent.action.example.ab.seee_chargecontrol");
        sendBroadcast(intent);
    }
}
