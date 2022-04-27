package com.akapps.dashcam;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;

public class SocketConnection {

    // layout
    private TextView tempReading;
    private TextView deviceStatus;
    private ImageButton turnOffRasPi;
    private Context context;

    // class data
    private static Socket s;
    private PrintWriter pw;
    private BufferedReader in;
    private int PORT = 8001;
    private String IP = "192.168.4.1";

    private Handler ha;
    private final int delay = 1000;

    private static String currentTemp = "";
    public static boolean isRasTurnedOff;

    public SocketConnection(Context context, TextView tempReading, TextView deviceStatus, ImageButton turnOffRasPi){
        this.context = context;
        this.tempReading = tempReading;
        this.deviceStatus = deviceStatus;
        this.turnOffRasPi = turnOffRasPi;
        ha = new Handler();
    }

    public void connectSocket(){
        ha.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (s == null) {
                    new Thread(() -> {
                        try {
                            s = new Socket(IP, PORT);
                            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                            Log.d("Here", "Connecting...");
                            String response = null;
                            while(in.readLine() != null) {
                                response = in.readLine();
                                Log.d("Here", "Received: --> " + response);
                                currentTemp = response;
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    // displays car temp for user to see
                                    if (tempReading != null && null != currentTemp && !currentTemp.isEmpty()) {
                                        if(tempReading.getVisibility() != TextView.VISIBLE)
                                            tempReading.setVisibility(View.VISIBLE);
                                        String currentCarTemp = currentTemp + context.getString(R.string.default_temp);
                                        tempReading.setText(currentCarTemp);
                                        deviceStatus.setText("Status: Connected");
                                    }
                                    else {
                                        deviceStatus.setText("Status: Not Connected");
                                        tempReading.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
                ha.postDelayed(this, delay);
            }
        }, delay);
    }

    public void sendData(String action) {
        // connects to raspberry pi and waits to receive information
        new Thread(() -> {
            try {
                Log.d("Here", "Sending data");
                if (!s.isConnected()) {
                    Log.d("Here", "Socket not connected");
                    s = new Socket(IP, PORT);
                }
                else
                    Log.d("Here", "Socket  connected");
                pw = new PrintWriter(s.getOutputStream());
                pw.write(action);
                new Handler(Looper.getMainLooper()).post(() -> {
                    tempReading.setVisibility(View.VISIBLE);
                    tempReading.setText("Turned off");
                    turnOffRasPi.setImageDrawable(context.getDrawable(R.drawable.refresh_icon));
                    isRasTurnedOff = true;
                });
                pw.flush();
                pw.close();
                s.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
