package com.akapps.dashcam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // layout
    private TextView tempReading;
    private TextView deviceStatus;
    private ImageButton turnOffRasPi;
    private ImageButton stop_recording;
    private ImageButton take_photo;

    // activity data
    private SocketConnection socketConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();
        populateWebViewCameraFeed();

        initSocket();
    }

    private void initLayout(){
        turnOffRasPi = findViewById(R.id.turn_off_ras_pi);
        stop_recording = findViewById(R.id.stop_recording);
        take_photo = findViewById(R.id.take_photo);
        tempReading = findViewById(R.id.temp_read);
        deviceStatus = findViewById(R.id.device_status);

        // currently, there is no temp reading, so set it to be invisible
        tempReading.setVisibility(View.INVISIBLE);

        // sends command to ras pi to turn off
        turnOffRasPi.setOnClickListener(view -> {
            if(socketConnection.isRasTurnedOff) {
                // restart app
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                Runtime.getRuntime().exit(0);
            }
            else
                socketConnection.sendData(getString(R.string.off_command));
        });

        stop_recording.setOnClickListener(view -> socketConnection.sendData("stop_recording"));

        take_photo.setOnClickListener(view -> socketConnection.sendData("take_photo"));
    }

    private void populateWebViewCameraFeed(){
        //liveFeedStreetCam.loadUrl("192.168.4.1:8081");
        //liveFeedFaceCam.loadUrl("192.168.4.1:8082");
        // cannot use cameras via python program and in android app
    }

    private void initSocket(){
        socketConnection = new SocketConnection(this, tempReading, deviceStatus, turnOffRasPi);
        socketConnection.connectSocket();
    }

}