package com.example.lofo.addpost;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.example.lofo.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddPostFragment extends BottomSheetDialogFragment {

    public static final String TAG = "AddPostBottomSheet";

    @Override
    public int getTheme() {
        return R.style.LoFo_BottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── KEY FIX: prevent the root view from consuming window insets ───────
        // Material's BottomSheetDialog adds bottom padding equal to the nav bar
        // height by calling setOnApplyWindowInsetsListener on the sheet container.
        // Setting fitsSystemWindows=false on our content view stops that listener
        // from propagating insets into our layout, eliminating the gap.
        view.setFitsSystemWindows(false);

        view.findViewById(R.id.lostIcon).setOnClickListener(v -> {
            dismiss();
            openForm("lost");
        });
        view.findViewById(R.id.foundIcon).setOnClickListener(v -> {
            dismiss();
            openForm("found");
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        // Enable edge-to-edge (this is what actually fixes the gap)
        dialog.getWindow().setDecorFitsSystemWindows(false);

        dialog.setOnShowListener(d -> {
            BottomSheetDialog bsd = (BottomSheetDialog) d;

            View sheet = bsd.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet);

            if (sheet == null) return;

            // Remove all default insets handling
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(sheet, (v, insets) -> insets);

            sheet.setPadding(0, 0, 0, 0);

            if (sheet.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams lp =
                        (ViewGroup.MarginLayoutParams) sheet.getLayoutParams();
                lp.bottomMargin = 0;
                sheet.setLayoutParams(lp);
            }

            // Also fix parent (VERY IMPORTANT)
            View parent = (View) sheet.getParent();
            if (parent != null) {
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(parent, (v, insets) -> insets);
                parent.setPadding(0, 0, 0, 0);
            }

            // Expand fully
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        return dialog;
    }

    private void openForm(String postType) {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.fragmentContainer, AddPostFormFragment.newInstance(postType))
                .addToBackStack("postForm")
                .commit();
    }
}