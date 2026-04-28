package com.example.lofo.profile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.lofo.R;
import com.example.lofo.auth.AuthActivity;
import com.example.lofo.util.ToastUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileFragment extends Fragment {

    private FirebaseUser user;

    // Name section
    private TextInputLayout tilName;
    private TextInputEditText etName;
    private View layoutNameContent;

    // Email section
    private TextInputLayout tilCurrentPasswordEmail, tilNewEmail;
    private TextInputEditText etCurrentPasswordEmail, etNewEmail;
    private View layoutEmailContent;

    // Password section
    private TextInputLayout tilCurrentPasswordPwd, tilNewPassword, tilConfirmPassword;
    private TextInputEditText etCurrentPasswordPwd, etNewPassword, etConfirmPassword;
    private View layoutPasswordContent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.editProfileTopBar),
                (v, insets) -> {
                    int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                    v.setPadding(v.getPaddingLeft(), top,
                            v.getPaddingRight(), v.getPaddingBottom());
                    return insets;
                });

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.editProfileScroll),
                (v, insets) -> {
                    int navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                    v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
                            v.getPaddingRight(), navBar + dpToPx(24));
                    return insets;
                });

        bindViews(view);
        prefillCurrentValues();
        setupAccordions();
        setupSaveButtons(view);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void bindViews(View view) {
        layoutNameContent     = view.findViewById(R.id.layoutNameContent);
        layoutEmailContent    = view.findViewById(R.id.layoutEmailContent);
        layoutPasswordContent = view.findViewById(R.id.layoutPasswordContent);

        tilName = view.findViewById(R.id.tilName);
        etName  = view.findViewById(R.id.etName);

        tilCurrentPasswordEmail = view.findViewById(R.id.tilCurrentPasswordEmail);
        etCurrentPasswordEmail  = view.findViewById(R.id.etCurrentPasswordEmail);
        tilNewEmail             = view.findViewById(R.id.tilNewEmail);
        etNewEmail              = view.findViewById(R.id.etNewEmail);

        tilCurrentPasswordPwd   = view.findViewById(R.id.tilCurrentPasswordPwd);
        etCurrentPasswordPwd    = view.findViewById(R.id.etCurrentPasswordPwd);
        tilNewPassword          = view.findViewById(R.id.tilNewPassword);
        etNewPassword           = view.findViewById(R.id.etNewPassword);
        tilConfirmPassword      = view.findViewById(R.id.tilConfirmPassword);
        etConfirmPassword       = view.findViewById(R.id.etConfirmPassword);
    }

    private void prefillCurrentValues() {
        if (user == null) return;
        if (user.getDisplayName() != null) etName.setText(user.getDisplayName());
    }

    // ─── Accordion ────────────────────────────────────────────────────────────

    private void setupAccordions() {
        layoutNameContent.setVisibility(View.GONE);
        layoutEmailContent.setVisibility(View.GONE);
        layoutPasswordContent.setVisibility(View.GONE);

        View chevronName     = requireView().findViewById(R.id.chevronName);
        View chevronEmail    = requireView().findViewById(R.id.chevronEmail);
        View chevronPassword = requireView().findViewById(R.id.chevronPassword);

        chevronName.setOnClickListener(v ->
                toggleSection(layoutNameContent, chevronName,
                        layoutEmailContent, layoutPasswordContent,
                        chevronEmail, chevronPassword));

        chevronEmail.setOnClickListener(v ->
                toggleSection(layoutEmailContent, chevronEmail,
                        layoutNameContent, layoutPasswordContent,
                        chevronName, chevronPassword));

        chevronPassword.setOnClickListener(v ->
                toggleSection(layoutPasswordContent, chevronPassword,
                        layoutNameContent, layoutEmailContent,
                        chevronName, chevronEmail));
    }

    private void toggleSection(View target, View targetChevron,
                               View other1, View other2,
                               View chevron1, View chevron2) {
        boolean opening = target.getVisibility() != View.VISIBLE;
        other1.setVisibility(View.GONE);
        other2.setVisibility(View.GONE);
        setChevronRotation(chevron1, false);
        setChevronRotation(chevron2, false);
        target.setVisibility(opening ? View.VISIBLE : View.GONE);
        setChevronRotation(targetChevron, opening);
    }

    private void setChevronRotation(View chevron, boolean open) {
        chevron.animate().rotation(open ? 180f : 0f).setDuration(200).start();
    }

    // ─── Save buttons ─────────────────────────────────────────────────────────

    private void setupSaveButtons(View view) {
        view.findViewById(R.id.ivEditProfileBack).setOnClickListener(v -> popBack());
        view.findViewById(R.id.btnSaveName).setOnClickListener(v -> saveName());
        view.findViewById(R.id.btnSaveEmail).setOnClickListener(v -> saveEmail());
        view.findViewById(R.id.btnSavePassword).setOnClickListener(v -> savePassword());
    }

    // ─── Save name ────────────────────────────────────────────────────────────

    private void saveName() {
        String name = getText(etName);
        if (TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.error_name_required));
            return;
        }
        tilName.setError(null);
        UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                .setDisplayName(name).build();
        user.updateProfile(req)
                .addOnSuccessListener(v ->
                        ToastUtils.show(requireContext(), getString(R.string.toast_name_updated)))
                .addOnFailureListener(e ->
                        ToastUtils.show(requireContext(), getString(R.string.error_generic)));
    }

    // ─── Save email ───────────────────────────────────────────────────────────
    // Flow: re-authenticate → verifyBeforeUpdateEmail (sends link to NEW address)
    // → update email in all Firestore items (optimistic, before user clicks link)
    // → sign out → go to AuthActivity with animation

    private void saveEmail() {
        String currentPwd = getText(etCurrentPasswordEmail);
        String newEmail = getText(etNewEmail).trim();

        tilCurrentPasswordEmail.setError(null);
        tilNewEmail.setError(null);

        if (TextUtils.isEmpty(currentPwd)) {
            tilCurrentPasswordEmail.setError(getString(R.string.error_password_required));
            return;
        }
        if (TextUtils.isEmpty(newEmail)
                || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            tilNewEmail.setError(getString(R.string.error_email_invalid));
            return;
        }
        if (user == null || user.getEmail() == null) return;

        if (newEmail.equalsIgnoreCase(user.getEmail())){
            ToastUtils.show(requireContext(),
                    getString(R.string.toast_email_same), Toast.LENGTH_SHORT);
            return;
        }

        String oldEmail = user.getEmail();
        String uid      = user.getUid();

        user.reauthenticate(EmailAuthProvider.getCredential(oldEmail, currentPwd))
                .addOnSuccessListener(v -> {
                    user.verifyBeforeUpdateEmail(newEmail)
                            .addOnSuccessListener(unused -> {
                                // Update contactEmail in all Firestore posts by this user
//                                updateFirestoreEmail(uid, newEmail);

                                ToastUtils.show(requireContext(),
                                        getString(R.string.toast_email_verify_sent), Toast.LENGTH_LONG);

                                new Handler(Looper.getMainLooper()).postDelayed(
                                        this::redirectToAuth, 1800);
                            })
                            .addOnFailureListener(e -> {
                                if (e instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                                    ToastUtils.show(requireContext(),
                                            getString(R.string.toast_email_another_user), Toast.LENGTH_LONG);
                                } else {
                                    ToastUtils.show(requireContext(),
                                            getString(R.string.error_generic), Toast.LENGTH_SHORT);
                                }
                            });
                })
                .addOnFailureListener(e ->
                        tilCurrentPasswordEmail.setError(getString(R.string.error_wrong_password)));

    }

    /**
     * Updates contactEmail field in every Firestore item uploaded by this user.
     * This runs in the background — no blocking UI needed.
     */
    private void updateFirestoreEmail(String uid, String newEmail) {
        FirebaseFirestore.getInstance()
                .collection("items")
                .whereEqualTo("uploadedByUid", uid)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        doc.getReference().update("contactEmail", newEmail);
                    }
                });
        // Failures are silent — the email change verification is the primary action
    }

    // ─── Save password ────────────────────────────────────────────────────────

    private void savePassword() {
        String currentPwd = getText(etCurrentPasswordPwd);
        String newPwd     = getText(etNewPassword);
        String confirmPwd = getText(etConfirmPassword);

        tilCurrentPasswordPwd.setError(null);
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);

        if (TextUtils.isEmpty(currentPwd)) {
            tilCurrentPasswordPwd.setError(getString(R.string.error_password_required));
            return;
        }
        if (newPwd.length() < 6) {
            tilNewPassword.setError(getString(R.string.error_password_short));
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            tilConfirmPassword.setError(getString(R.string.error_passwords_no_match));
            return;
        }
        if (user == null || user.getEmail() == null) return;

        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), currentPwd))
                .addOnSuccessListener(v ->{
                        if (currentPwd.equals(newPwd)) {
                            ToastUtils.show(requireContext(),
                                    getString(R.string.toast_password_same), Toast.LENGTH_SHORT);
                            return;
                        }

                        user.updatePassword(newPwd)
                            .addOnSuccessListener(unused -> {
                                ToastUtils.show(requireContext(),
                                        getString(R.string.toast_password_updated), Toast.LENGTH_LONG);
                                new Handler(Looper.getMainLooper()).postDelayed(
                                        this::redirectToAuth, 1800);
                            })
                            .addOnFailureListener(e ->
                                    ToastUtils.show(requireContext(),
                                            getString(R.string.error_generic)));
                            })

                .addOnFailureListener(e ->
                        tilCurrentPasswordPwd.setError(
                                getString(R.string.error_wrong_password)));
    }

    // ─── Redirect to AuthActivity ─────────────────────────────────────────────
    // CORRECT approach: start AuthActivity as a new task (with animation),
    // finish MainActivity. This ensures the bottom nav is never visible,
    // and LoginFragment inside AuthActivity has full access to AuthActivity methods.

    private void redirectToAuth() {
        if (getActivity() == null) return;
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(requireActivity(), AuthActivity.class);
        // FLAG_ACTIVITY_NEW_TASK | CLEAR_TASK wipes the back stack including MainActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Slide-in animation on the activity transition
        requireActivity().overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);

        requireActivity().finish();
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void popBack() {
        if (getActivity() != null)
            getActivity().getSupportFragmentManager().popBackStack();
    }
}