package com.akapps.dashcam;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class AppData {
    private static AppData appData;

    public static Socket socket;
    public static String currentLayout;

    public static Dialog progressDialog;
    public static Handler socketHandler;

    // set up data
    public static String pathToImageTaken;
    public static boolean sendingImage;
    public static boolean isImageSent;
    public static boolean isSetUpMode;

    // image data
    public static ArrayList<String> allPhotos;
    public static String selectedImage;

    private AppData() { }

    public static AppData getAppData(Context context) {
        //instantiate a new CustomerLab if we didn't instantiate one yet
        if (appData == null) {
            appData = new AppData();
            currentLayout = context.getString(R.string.no_mode);
            allPhotos = Helper.getArrayList(context);
            isSetUpMode = allPhotos.size() == 0;
            selectedImage = Helper.getSelectedImage(context);
            AppData.socketHandler = new Handler();
        }
        return appData;
    }

    // returns the user profile image in an arraylist
    public static ArrayList<String> getSelectedImageArraylist(){
        ArrayList<String> selectedImageArraylist = new ArrayList<>();
        selectedImageArraylist.add(selectedImage);
        return selectedImageArraylist;
    }

    // disconnects from raspberry pi
    public static void disconnectFromDevice(Context context){
        try {
            if(AppData.socket != null) {
                AppData.socket.close();
                AppData.socket = null;
                AppData.currentLayout = context.getString(R.string.no_mode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}