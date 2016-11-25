package com.giousa.sendipserver;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();
    private Button mSendIP,mStopIP;
    private TextView mIpCount;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private boolean isPause = false;
    private static int delay = 0;  //延迟0s
    private static int period = 3000;  //重复执行2s
    private static final int HOST_IP = 110;
    private int mSendCount=0;

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case HOST_IP:
                    Log.d(TAG,"HOST_IP="+HOST_IP);
                    sendIPToClient();
                    mSendCount++;
                    mIpCount.setText("发送IP次数："+mSendCount);
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mSendIP = (Button) findViewById(R.id.btn_sendip);
        mStopIP = (Button) findViewById(R.id.btn_stop);
        mIpCount = (TextView) findViewById(R.id.tv_count);
        mSendIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
                mSendCount = 0;
            }
        });

        mStopIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTimer();
                mSendCount = 0;
                mIpCount.setText("发送IP次数："+mSendCount);
            }
        });
    }

    private void sendIPToClient() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 广播的实现 :由客户端发出广播，服务器端接收
                String host = "255.255.255.255";//广播地址
                int port = 9999;//广播的目的端口

                String message = getIp();

                try {
                    InetAddress adds = InetAddress.getByName(host);
                    Log.d(TAG, "sendIPToClient:" + message);
                    DatagramSocket ds = new DatagramSocket();
                    DatagramPacket dp = new DatagramPacket(message.getBytes(),
                            message.length(), adds, port);
                    ds.send(dp);
                    ds.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private String getIp() {
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //检查Wifi状态
        if (!wm.isWifiEnabled())
            wm.setWifiEnabled(true);
        WifiInfo wi = wm.getConnectionInfo();
        //获取32位整型IP地址
        int ipAdd = wi.getIpAddress();
        //把整型地址转换成“*.*.*.*”地址
        String ip = intToIp(ipAdd);
        return ip;
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }


    private void startTimer(){
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Log.d(TAG,"timer start");
                    sendMessage(HOST_IP);
                    do {
                        try {
                            Log.i(TAG, "sleep(5000)...");
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                        }
                    } while (isPause);

                }
            };
        }

        if(mTimer != null && mTimerTask != null )
            mTimer.schedule(mTimerTask, delay, period);

    }

    private void stopTimer(){

        Log.d(TAG,"timer end");

        if(mTimer!=null){
            mTimer.cancel();
            mTimer = null;
        }

        if(mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }

    }

    public void sendMessage(int id){
        if (mHandler != null) {
            Message message = Message.obtain(mHandler, id);
            mHandler.sendMessage(message);
        }
    }
}
