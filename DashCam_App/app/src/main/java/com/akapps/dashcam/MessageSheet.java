package com.akapps.dashcam;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.airbnb.lottie.LottieAnimationView;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

public class MessageSheet extends RoundedBottomSheetDialogFragment{

    // data
    private String title;
    private String message;
    private int messageDuration;
    private String messageType;

    public MessageSheet(){ }

    public MessageSheet(String title, String message, int messageDuration, String messageType) {
        this.title = title;
        this.message = message;
        this.messageDuration = messageDuration;
        this.messageType = messageType;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_message, container, false);

        view.setBackgroundColor(getContext().getColor(R.color.gray));

        // layout
        TextView message_Title = view.findViewById(R.id.title);
        TextView message_Text = view.findViewById(R.id.message_text);
        LottieAnimationView messageTypeAnimation = view.findViewById(R.id.message_status);
        MaterialButton dismiss = view.findViewById(R.id.dismiss);

        // populate data
        message_Title.setText(title);
        message_Text.setText(message);

        // if message duration > 0, it will auto dismiss
        if(messageDuration != 0) {
            dismiss.setVisibility(View.GONE);
            new Handler().postDelayed(this::dismiss, messageDuration);
        }

        // show animation based on message type
        messageTypeAnimation.setAnimation(getResources().getIdentifier(messageType, "raw", getActivity().getPackageName()));

        dismiss.setOnClickListener(view1 -> dismiss());

        return view;
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