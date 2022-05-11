package com.akapps.dashcam;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stfalcon.imageviewer.StfalconImageViewer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import www.sanju.motiontoast.MotionToast;

public class Helper {

    // uses Glide to populate image in an imageview
    public static void setImage(Context context, String image, View imageView, boolean centerCrop){
        if(centerCrop)
            Glide.with(context).load(image).centerCrop().placeholder(context.getDrawable(R.drawable.error_icon))
                .into((ImageView) imageView);
        else
            Glide.with(context).load(image).placeholder(context.getDrawable(R.drawable.error_icon))
                    .into((ImageView) imageView);
    }

    // allow image to be clicked and expanded for zoom capabilities
    public static void expandImage(Context context, ArrayList<String> images, View imageToOpen,
                                   int position, boolean filter, boolean centerCrop){
        if(images.size() == 0)
            return;
        ArrayList<String> selectedImage = new ArrayList<>();

        if(filter) {
            if (images.get(0).contains("png_"))
                selectedImage.add(images.get(0).split("png_")[0] + "png");
            else
                selectedImage.add(images.get(0));
        }
        else
            selectedImage = images;

        new StfalconImageViewer.Builder<>(context, selectedImage, (imageView, image) ->
                setImage(context, image, imageView, centerCrop))
                .withBackgroundColor(context.getColor(R.color.gray))
                .allowZooming(true)
                .allowSwipeToDismiss(true)
                .withHiddenStatusBar(false)
                .withStartPosition(position)
                .withTransitionFrom((ImageView) imageToOpen)
                .show();
    }

    public static void openSettingsSheet(FragmentActivity activity, boolean isCancelable){
        SettingsSheet settingsSheet = new SettingsSheet();
        settingsSheet.show(activity.getSupportFragmentManager(), settingsSheet.getTag());
        settingsSheet.setCancelable(isCancelable);
    }

    public static void openMessageSheet(FragmentActivity activity, String title, String message,
                                        int duration, String messageType){
        MessageSheet messageSheet = new MessageSheet(title, message, duration, messageType + "_animation");
        messageSheet.show(activity.getSupportFragmentManager(), messageSheet.getTag());
        messageSheet.setCancelable(false);
    }

    // saves list of images via arraylist in shared preferences
    public static void saveArrayList(Context context, ArrayList<String> list){
        SharedPreferences prefs = context.getSharedPreferences("app", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(context.getString(R.string.photos_key), json);
        editor.apply();
        AppData.allPhotos = list;

        if(list.size() > 0) {
            AppData.isSetUpMode = false;
            AppData.selectedImage = getSelectedImage(context);
        }
        else
            AppData.isSetUpMode = true;
    }

    // gets list of image paths that are saved
    public static ArrayList<String> getArrayList(Context context){
        SharedPreferences prefs = context.getSharedPreferences("app", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(context.getString(R.string.photos_key), null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();

        if(gson.fromJson(json, type) == null)
            return new ArrayList<String>();
        else
            return gson.fromJson(json, type);
    }

    // returns image that is currently selected by user to be their profile picture
    public static String getSelectedImage(Context context){
        ArrayList<String> allPhotos = getArrayList(context);
        for(int i = 0; i < allPhotos.size(); i++){
            if(Boolean.valueOf(allPhotos.get(i).split("png_")[1])) {
                AppData.selectedImage = allPhotos.get(i).split("png_")[0] + "png";
                return AppData.selectedImage;
            }
        }
        return null;
    }

    // saves a small piece of data
    public static void savePreference(Context context, String data, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, data);
        editor.apply();
    }

    // retrieved data saved
    public static String getPreference(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app", MODE_PRIVATE);
        String data = sharedPreferences.getString(key, null);
        return data;
    }

    // shows the user a message
    public static void showMessage(Activity activity, String title, String message, String typeOfMessage){
        try {
            MotionToast.Companion.darkColorToast(activity, title, message, typeOfMessage,
                    MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(activity, www.sanju.motiontoast.R.font.helvetica_regular));
        }
        catch (Exception e){
            Toast.makeText(activity.getBaseContext(), title + "\n" + message, Toast.LENGTH_LONG).show();
        }
    }

    // show loading screen
    public static Dialog showLoading(String loadingText, Dialog progressDialog, Context context, boolean show){
        try {
            if (show) {
                progressDialog = new Dialog(context);
                progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                progressDialog.setContentView(com.app.aimatedprogresslib.R.layout.custom_dialog_progress);

                TextView progressTv = progressDialog.findViewById(com.app.aimatedprogresslib.R.id.progress_tv);
                progressTv.setText(loadingText);
                progressTv.setGravity(Gravity.CENTER);
                progressTv.setTextColor(ContextCompat.getColor(context, R.color.golden_rod));
                progressTv.setTextSize(19F);
                if (progressDialog.getWindow() != null)
                    progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                progressDialog.setCancelable(false);
                progressDialog.show();
            } else
                progressDialog.cancel();
        }catch (Exception e){}

        return progressDialog;
    }
}
