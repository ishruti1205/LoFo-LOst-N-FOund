package com.example.lofo.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.lofo.R;

public class HelpSupportFragment extends Fragment {

    private static final String SUPPORT_EMAIL = "shruti12052004@email.com";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help_support, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Status bar inset
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.helpTopBar),
                (v, insets) -> {
                    int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                    v.setPadding(v.getPaddingLeft(), top,
                            v.getPaddingRight(), v.getPaddingBottom());
                    return insets;
                });

        view.findViewById(R.id.ivHelpBack).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Email us
        view.findViewById(R.id.ivEmailUs).setOnClickListener(v -> {
            String subject = Uri.encode("LoFo App — Support Request");
            Uri uri = Uri.parse("mailto:" + SUPPORT_EMAIL + "?subject=" + subject);
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(uri);
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        // Report a bug (same email with different subject)
        view.findViewById(R.id.ivReportBug).setOnClickListener(v -> {
            String subject = Uri.encode("LoFo App — Bug Report");
            Uri uri = Uri.parse("mailto:" + SUPPORT_EMAIL + "?subject=" + subject);
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(uri);
            startActivity(Intent.createChooser(intent, "Report Bug"));
        });

        // Privacy policy placeholder
        view.findViewById(R.id.ivPrivacy).setOnClickListener(v -> {
            // TODO: replace with your actual privacy policy URL
            Intent intent = new Intent(Intent.ACTION_VIEW,
//                    Uri.parse("https://yourwebsite.com/privacy"));
                    Uri.parse("https://www.google.com/privacy"));
            startActivity(intent);
        });
    }
}