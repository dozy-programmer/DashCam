package com.akapps.dashcam;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.card.MaterialCardView;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import www.sanju.motiontoast.MotionToast;

public class SocketConnection {

    // layout
    private TextView tempReading;
    private TextView deviceName;
    private MaterialCardView deviceStatus;
    private Context context;

    // class data
    private PrintWriter pw;
    private BufferedReader in;
    private int PORT = 8001;
    private final int delay = 1000;
    private int connectingTime = 0;
    private static String currentTemp = "";

    public SocketConnection(Context context, TextView tempReading, MaterialCardView deviceStatus, TextView deviceName){
        this.context = context;
        this.tempReading = tempReading;
        this.deviceName = deviceName;
        this.deviceStatus = deviceStatus;
    }

    public void receiveData(){
        new Thread(() -> {
            try {
                String response;
                // continuously listen for data from raspberry pi
                while(in.readLine() != null) {
                    response = in.readLine();
                    currentTemp = response;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // displays car temp for user to see
                        if (null != currentTemp && !currentTemp.isEmpty()) {
                            if(currentTemp.contains("send_image")) {
                                // ensures if statement only runs once
                                if(!AppData.sendingImage) {
                                    AppData.sendingImage = true;
                                    ((MainActivity) context).sendImageToRasPi(AppData.pathToImageTaken);
                                }
                            }
                            else if(currentTemp.contains("image_received_success")){
                                // ensures if statement only runs once
                                if(!AppData.isImageSent) {
                                    AppData.isImageSent = true;
                                    Helper.openMessageSheet((MainActivity) context, "Image Status",
                                            "Image Received & Face Detected", 0,
                                            context.getString(R.string.message_type_success));
                                    AppData.currentLayout = context.getString(R.string.connected_mode);
                                    ((MainActivity)context).showCurrentLayout();
                                }
                            }
                            else if(currentTemp.contains("image_received_fail")){
                                // ensures if statement only runs once
                                if(!AppData.isImageSent) {
                                    AppData.isImageSent = true;
                                    // deletes image taken if no face was detected in picture
                                    File imageFile = new File(AppData.pathToImageTaken);
                                    if (imageFile.exists()) {
                                        if (imageFile.delete())
                                        AppData.allPhotos.remove(AppData.allPhotos.size()-1);
                                        Helper.saveArrayList(context, AppData.allPhotos);
                                        AppData.currentLayout = context.getString(R.string.set_up_mode);
                                        ((MainActivity)context).showCurrentLayout();
                                    }
                                    Helper.openMessageSheet((MainActivity)context, "Image Status",
                                            "Face not detected in photo send, please resend", 0,
                                            context.getString(R.string.message_type_error));
                                }
                            }
                            else if(currentTemp.contains("resend_image")){
                                // reset values
                                AppData.isImageSent = AppData.sendingImage = false;
                                // resend image
                                ((MainActivity) context).sendImageToRasPi(AppData.pathToImageTaken);
                            }
                            else if(currentTemp.contains("*")) {
                                // getting raspberry pi device name
                                Helper.savePreference(context, currentTemp.split("_")[1].replace("*", ""), "device_name");
                                deviceName.setText("Device Name\n\n" + currentTemp.split("_")[1].replace("*", ""));
                            }
                            currentTemp = currentTemp.split("_")[0];
                            // update layout to view temp reading
                            if (tempReading.getVisibility() != TextView.VISIBLE) {
                                tempReading.setVisibility(View.VISIBLE);
                                deviceStatus.setCardBackgroundColor(context.getColor(R.color.dark_green));
                            }
                            String currentCarTemp = currentTemp + context.getString(R.string.default_temp);
                            tempReading.setText(context.getString(R.string.temp) + "\n" + currentCarTemp);
                        }
                        else {
                            // device is disconnected
                            AppData.disconnectFromDevice(context);
                            deviceStatus.setCardBackgroundColor(context.getColor(R.color.red));
                            AppData.currentLayout = context.getString(R.string.no_mode);
                            ((MainActivity)context).showCurrentLayout();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void connectDevice() {
        // show loading screen
        AppData.progressDialog = Helper.showLoading("Connecting to\n" + Helper.getPreference(context,
                context.getString(R.string.ip_address_key)) + "...", AppData.progressDialog, context, true);

        AppData.socketHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AppData.socket == null) {
                    new Thread(() -> {
                        try {
                            // attempts to connect to raspberry pi, if nothings happens
                            // in 20-30 seconds, then it stops trying to connect
                            connectingTime += delay * 3;
                            if(connectingTime >= 20000) {
                                AppData.socketHandler.removeCallbacks(this);
                                // close loading screen
                                Helper.showLoading("", AppData.progressDialog, context, false);
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    Helper.showMessage((Activity) context, "No device Found",
                                            "Make sure ras pi is running and try again",
                                            MotionToast.TOAST_ERROR);
                                });
                            }
                            // attempts to connect to raspberry pi
                            AppData.socket = new Socket(Helper.getPreference(context,
                                    context.getString(R.string.ip_address_key)), PORT);
                            in = new BufferedReader(new InputStreamReader(AppData.socket.getInputStream()));
                            Log.d("Here", "Connected!");
                            new Handler(Looper.getMainLooper()).post(() -> {
                                // checks to see if user has ever taken a photo, if not enter setup mode
                                if(!AppData.isSetUpMode)
                                    AppData.currentLayout = context.getString(R.string.connected_mode);
                                else
                                    AppData.currentLayout = context.getString(R.string.set_up_mode);
                                // update layout
                                ((MainActivity) context).showCurrentLayout();
                                Helper.showMessage((Activity) context, "Device Found", "Connected to Ras Pi",
                                        MotionToast.TOAST_SUCCESS);
                                deviceStatus.setCardBackgroundColor(context.getColor(R.color.dark_green));
                            });
                            // close loading screen
                            Helper.showLoading("", AppData.progressDialog, context, false);
                            // continuously listen for data from raspberry pu
                            receiveData();
                            // stop running this method
                            AppData.socketHandler.removeCallbacks(this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
                AppData.socketHandler.postDelayed(this, delay * 5);
            }
        }, delay);
    }

    public void sendData(String action) {
        // sends data to raspberry pi
        new Thread(() -> {
            try {
                // attempts to reconnect socket if not connected
                if (!AppData.socket.isConnected())
                    AppData.socket = new Socket(Helper.getPreference(context, context.getString(R.string.ip_address_key)), PORT);

                pw = new PrintWriter(AppData.socket.getOutputStream());
                pw.write(action);
                pw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
