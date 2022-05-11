package com.akapps.dashcam;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.io.File;
import java.util.ArrayList;
import www.sanju.motiontoast.MotionToast;

public class owner_photos_recyclerview extends RecyclerView.Adapter<owner_photos_recyclerview.MyViewHolder>{

    // project data
    private final ArrayList<String> allPhotos;
    private final FragmentActivity activity;
    private final Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView background;
        private final ImageView image;
        private final ImageView delete;
        private final View view;

        public MyViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            delete = v.findViewById(R.id.delete);
            background = v.findViewById(R.id.image_outline);
            view = v;
        }
    }

    public owner_photos_recyclerview(ArrayList<String> allPhotos, FragmentActivity activity, Context context) {
        this.allPhotos = allPhotos;
        this.activity = activity;
        this.context = context;
    }

    @Override
    public owner_photos_recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_owner_photo_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        // retrieves current photo
        String currentPhoto = allPhotos.get(position);

        String photoPath = currentPhoto.split("png_")[0] + "png";
        boolean isSelected = Boolean.valueOf(currentPhoto.split("png_")[1]);

        if(isSelected)
            holder.background.setStrokeColor(context.getColor(R.color.orange_red));
        else
            holder.background.setStrokeColor(context.getColor(R.color.gray));

        // populates photo into imageview
        Helper.setImage(context, photoPath, holder.image, true);

        // if photo is clicked, then it is expanded and can be zoomed
        holder.image.setOnClickListener(v -> {
            ArrayList<String> images = new ArrayList<>();
            for(int i = 0; i < allPhotos.size(); i++){
                if(!allPhotos.get(i).isEmpty())
                    images.add(allPhotos.get(i).split("png_")[0] + "png");
            }

            Helper.expandImage(context, images, holder.image, position, false, false);
        });

        // if delete icon is long clicked, photo is deleted
        holder.delete.setOnLongClickListener(v -> {
            if(!isSelected) {
                File fdelete = new File(photoPath);
                if (fdelete.exists())
                    fdelete.delete();
                allPhotos.remove(position);
                Helper.saveArrayList(v.getContext(), allPhotos);
                notifyItemRemoved(position);
                new Handler().postDelayed(this::notifyDataSetChanged, 1000);
            }
            else
                Helper.showMessage((MainActivity)context, "Error",
                        "Un-select image to delete", MotionToast.TOAST_ERROR);
            return false;
        });

        holder.background.setOnLongClickListener(view -> {
            selectPhoto(isSelected, position, photoPath);
            return false;
        });

        holder.image.setOnLongClickListener(view -> {
            selectPhoto(isSelected, position, photoPath);
            return false;
        });
    }

    // unselects all photos and selects the one selected by user
    private void selectPhoto(boolean isSelected, int position, String photoPath){
        if(!isSelected) {
            unSelectAllPhotos();
            allPhotos.set(position, photoPath + "_" + !isSelected);
            Helper.saveArrayList(context, allPhotos);
            notifyDataSetChanged();
            if (AppData.currentLayout.equals(context.getString(R.string.connected_mode)))
                ((MainActivity) context).showCurrentLayout();
        }
    }

    private void unSelectAllPhotos(){
        for(int i = 0; i< allPhotos.size(); i++){
            String photoPath = allPhotos.get(i).split("png_")[0] + "png";
            allPhotos.set(i, photoPath + "_false");
        }
    }

    @Override
    public int getItemCount() {
        return allPhotos.size();
    }
}
