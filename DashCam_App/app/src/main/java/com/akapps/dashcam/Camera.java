package com.akapps.dashcam;

import android.app.Activity;
import android.content.Context;
import com.github.dhaval2404.imagepicker.ImagePicker;

public class Camera {

    public static void open(Context context){
        ImagePicker.with((Activity) context)
                .maxResultSize(814, 814)
                .compress(1024)
                .saveDir(context.getExternalFilesDir(null))
                .start();
    }
}
