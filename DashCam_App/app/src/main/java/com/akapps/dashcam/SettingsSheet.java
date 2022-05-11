package com.akapps.dashcam;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsSheet extends RoundedBottomSheetDialogFragment{

    private TextInputLayout nameLayout;
    private TextInputEditText nameInput;
    private TextInputLayout ipAddressLayout;
    private TextInputEditText ipAddressInput;

    public SettingsSheet(){ }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_settings, container, false);

        view.setBackgroundColor(getContext().getColor(R.color.gray));

        // layout
        nameLayout= view.findViewById(R.id.user_name_layout);
        nameInput = view.findViewById(R.id.insert_user_name);
        ipAddressLayout = view.findViewById(R.id.ip_address_layout);
        ipAddressInput = view.findViewById(R.id.insert_ip_address);
        FloatingActionButton submitButton = view.findViewById(R.id.submit);
        TextView photosMessage = view.findViewById(R.id.photos_message);

        // if connected to raspberry pi, then show images
        if(AppData.currentLayout.equals(getContext().getString(R.string.connected_mode))) {
            RecyclerView photosScrollView = view.findViewById(R.id.owner_photos);
            photosScrollView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            RecyclerView.Adapter scrollAdapter = new owner_photos_recyclerview(AppData.allPhotos, getActivity(), getContext());
            photosScrollView.setAdapter(scrollAdapter);
        }
        else
            photosMessage.setVisibility(View.GONE);

        // get data if there is any
        String currentUserName = Helper.getPreference(getContext(), getString(R.string.user_name_key));
        String currentIpAddress = Helper.getPreference(getContext(), getString(R.string.ip_address_key));
        if(currentUserName != null)
            nameInput.setText(currentUserName);
        if(currentIpAddress != null)
            ipAddressInput.setText(currentIpAddress);
        else
            ipAddressInput.setText("192.168.1.");

        submitButton.setOnClickListener(view1 -> getInput());

        return view;
    }

    private void getInput(){
        String inputUserName = nameInput.getText().toString();
        String inputIpAddress = ipAddressInput.getText().toString();

        if (!inputUserName.isEmpty()) {
            nameLayout.setErrorEnabled(false);
            if (!inputIpAddress.isEmpty()) {
                Helper.savePreference(getContext(), inputUserName, getContext().getString(R.string.user_name_key));
                Helper.savePreference(getContext(), inputIpAddress, getContext().getString(R.string.ip_address_key));
                ((MainActivity) getContext()).updateUserData();
                if(AppData.isSetUpMode) {
                    Helper.openMessageSheet(getActivity(), getString(R.string.set_up_title),
                            getString(R.string.setup_message), 0,
                            getString(R.string.message_type_info));
                }
                dismiss();
            }
            else
                ipAddressLayout.setError(getContext().getString(R.string.input_error));
        }
        else
            nameLayout.setError(getContext().getString(R.string.input_error));
    }

    @Override
    public int getTheme() {
        return R.style.BaseBottomSheetDialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    BottomSheetDialog dialog =(BottomSheetDialog) getDialog ();
                    if (dialog != null) {
                        FrameLayout bottomSheet = dialog.findViewById (R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from (bottomSheet);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                });
    }

}