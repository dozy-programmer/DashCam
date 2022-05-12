package com.akapps.dashcam;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import www.sanju.motiontoast.MotionToast;

public class MainActivity extends AppCompatActivity {

    // app layout
    private FloatingActionButton settings;
    private MaterialCardView deviceStatus;
    private MaterialButton action_button;
    private MaterialButton take_photo;
    private LottieAnimationView takePictureAnimation;
    private MaterialCardView dashboard;
    private ShapeableImageView userImage;
    private ShapeableImageView imageTaken;
    private TextView userName;
    private TextView deviceName;
    private TextView deviceIP;
    private TextView tempReading;
    private TextView setupText;
    private LottieAnimationView emptyAnimation;

    // activity data
    private Context context;
    private SocketConnection socketConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        // delete cache to decrease app size
        Helper.deleteCache(context);

        initLayout();

        // initialize data
        AppData.getAppData(this);

        // show the default layout (out of 3)
        showCurrentLayout();

        // checks if name and ip address are set and open bottom sheet if not
        getUserData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Here", "Disconnecting socket");
        AppData.disconnectFromDevice(this);
    }

    private void getUserData(){
        if(Helper.getPreference(this, getString(R.string.user_name_key)) == null ||
                Helper.getPreference(this, getString(R.string.ip_address_key)) == null)
            Helper.openSettingsSheet(this, false);
    }

    @SuppressLint("SetTextI18n")
    public void updateUserData(){
        // retrieves saved user preferences
        userName.setText(Helper.getPreference(this, getString(R.string.user_name_key)));
        deviceIP.setText(context.getString(R.string.ip) + "\n\n" +
                Helper.getPreference(this, getString(R.string.ip_address_key)));
    }

    private void initSocket(){
        // connects to raspberry pi
        socketConnection = new SocketConnection(this, tempReading, deviceStatus, deviceName);
        socketConnection.connectDevice();
    }

    @SuppressLint("SetTextI18n")
    public void showCurrentLayout(){
        // empty layout
        emptyAnimation.setVisibility(View.GONE);
        // set up layout
        setupText.setVisibility(View.GONE);
        takePictureAnimation.setVisibility(View.GONE);
        imageTaken.setVisibility(View.GONE);
        // dashboard layout
        dashboard.setVisibility(View.GONE);
        action_button.setBackgroundColor(getColor(R.color.darker_blue));

        if(AppData.currentLayout.equals(getString(R.string.no_mode))){
            // show correct layout
            emptyAnimation.setVisibility(View.VISIBLE);
            take_photo.setVisibility(View.GONE);

            // populate layout
            action_button.setText(getString(R.string.connect));
        }
        else if(AppData.currentLayout.equals(getString(R.string.set_up_mode))){
            // show correct layout
            setupText.setVisibility(View.VISIBLE);
            takePictureAnimation.setVisibility(View.VISIBLE);
            imageTaken.setVisibility(View.VISIBLE);
            take_photo.setVisibility(View.GONE);

            // populate layout
            action_button.setText(getString(R.string.take_photo));
            if(!AppData.isSetUpMode) {
                setupText.setText(getString(R.string.take_selfie));
                take_photo.setVisibility(View.VISIBLE);
                take_photo.setText(getString(R.string.close));
                take_photo.setBackgroundColor(getColor(R.color.red));
            }
            else
                setupText.setText(getString(R.string.setting_up_text));
        }
        else if(AppData.currentLayout.equals(getString(R.string.connected_mode))){
            // show correct layout
            dashboard.setVisibility(View.VISIBLE);
            take_photo.setVisibility(View.VISIBLE);
            action_button.setText(getString(R.string.disconnect));
            action_button.setBackgroundColor(getColor(R.color.red));
            take_photo.setText(getString(R.string.take_photo));
            take_photo.setBackgroundColor(getColor(R.color.sunset_orange));

            userImage.setOnClickListener(view -> Helper.expandImage(context, AppData.getSelectedImageArraylist(), userImage, 0, true, true));

            // populates dashboard
            Helper.setImage(context, AppData.selectedImage, userImage, true);
            userName.setText(Helper.getPreference(this, getString(R.string.user_name_key)));
            if(Helper.getPreference(this, "device_name") == null)
                deviceName.setText(getString(R.string.device_name) + "\n\n" + getString(R.string.click_here));
            else
                deviceName.setText(getString(R.string.device_name) + "\n\n" + Helper.getPreference(this, getString(R.string.device_name_key)));
            deviceIP.setText(getString(R.string.ip) + "\n\n" + Helper.getPreference(this, getString(R.string.ip_address_key)));
        }
    }

    private void initLayout(){
        settings = findViewById(R.id.settings);
        setupText = findViewById(R.id.set_up_text);
        emptyAnimation = findViewById(R.id.no_device_connected_animation);
        deviceStatus = findViewById(R.id.device_status);
        action_button = findViewById(R.id.action_button);
        take_photo = findViewById(R.id.take_photo);
        takePictureAnimation = findViewById(R.id.take_picture_animation);
        imageTaken = findViewById(R.id.picture_taken);
        dashboard = findViewById(R.id.dashboard);
        userImage = findViewById(R.id.user_photo);
        userName = findViewById(R.id.user_name);
        deviceName = findViewById(R.id.device_name);
        deviceIP = findViewById(R.id.ip_address);
        tempReading = findViewById(R.id.temp);

        settings.setOnClickListener(view -> Helper.openSettingsSheet(this, true));

        action_button.setOnClickListener(view -> {
            if(action_button.getText().equals(getString(R.string.connect)))
                initSocket();
            else if(action_button.getText().equals(getString(R.string.disconnect))) {
                // stops looking for device to connect to
                AppData.socketHandler.postDelayed(null, 0);
                AppData.disconnectFromDevice(this);
                showCurrentLayout();
                deviceStatus.setCardBackgroundColor(getColor(R.color.red));
            }
            else if(action_button.getText().equals(getString(R.string.take_photo)))
                Camera.open(this);
            else if(action_button.getText().equals(getString(R.string.next))) {
                // show loading screen
                AppData.progressDialog = Helper.showLoading("Sending Image...", AppData.progressDialog, MainActivity.this, true);
                // send command to raspberry pi to get ready to receive an image
                socketConnection.sendData("initiate_sending_image");
                Log.d("Here", "Image sent to raspberry pi");
            }
        });

        deviceName.setOnClickListener(view -> {
            socketConnection.sendData("get_host_name");
        });

        imageTaken.setOnLongClickListener(view -> {
            Camera.open(this);
            return false;
        });

        take_photo.setOnClickListener(view -> {
            if(take_photo.getText().toString().equals(getString(R.string.take_photo))) {
                AppData.currentLayout = getString(R.string.set_up_mode);
                Camera.open(this);
            }
            else if(take_photo.getText().toString().equals(getString(R.string.close)))
                AppData.currentLayout = getString(R.string.connected_mode);
            showCurrentLayout();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();

            // save photo taken to user images
            ArrayList<String> userImages = AppData.allPhotos;
            userImages.add(uri.getPath() + (userImages.size() == 0 ? "_true" : "_false"));
            Helper.saveArrayList(this, userImages);
            AppData.pathToImageTaken = uri.getPath();

            // add image to arraylist (expand method requires an arraylist) and allow image to expand when clicked
            ArrayList<String> imageJustTaken = new ArrayList<>();
            imageJustTaken.add(uri.getPath());
            imageTaken.setOnClickListener(view -> Helper.expandImage(context, imageJustTaken, imageTaken, 0, false, false));

            // update layout
            Helper.setImage(context, imageJustTaken.get(0), imageTaken, false);
            takePictureAnimation.setVisibility(View.INVISIBLE);
            action_button.setText(getString(R.string.next));
            action_button.setBackgroundColor(getColor(R.color.neptune));
            setupText.setText("Long Click Photo to Retake");
        }
        else if (resultCode== ImagePicker.RESULT_ERROR)
            Helper.showMessage(this, "Error", "Try again", MotionToast.TOAST_ERROR);
    }

    public void sendImageToRasPi(String filePath){
        final int[] fileSize = {0};
        new Thread(() -> {
            try {
                // gets file ready for sending
                File photoPath = new File(filePath);
                fileSize[0] = Integer.parseInt(String.valueOf(photoPath.length()/1024));
                FileInputStream fis = new FileInputStream(photoPath);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream dis = new DataInputStream(bis);
                OutputStream os = AppData.socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);

                // sends file
                byte[] mybytearray = new byte[16384];
                int read = dis.read(mybytearray);
                while (read != -1) {
                    dos.write(mybytearray, 0, read);
                    read = dis.read(mybytearray);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //
                new Handler(Looper.getMainLooper()).post(() -> {
                    new Handler().postDelayed(() -> {
                        // sends command to signal file has been sent and also includes file size
                        // so that raspberry pi can ensure it received the correct file size
                        socketConnection.sendData("image_sent~" + fileSize[0]);

                        new Handler().postDelayed(() -> {
                            // update layout
                            imageTaken.setImageResource(0);
                            // restarts the raspberry pi socket to receive temperature data
                            socketConnection.sendData("get_host_name");
                            // reset values to stop loop in SocketConnection.java
                            AppData.isImageSent = AppData.sendingImage = false;
                            // data has been sent and loading screen is closed
                            Helper.showLoading("", AppData.progressDialog, MainActivity.this, false);
                        }, 2000);

                    }, 2000);
                });
            }
        }).start();
    }

}