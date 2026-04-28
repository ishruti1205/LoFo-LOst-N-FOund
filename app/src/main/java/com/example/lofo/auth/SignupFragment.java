package com.example.lofo.auth;

import android.os.Bundle;
import com.example.lofo.R;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignupFragment extends Fragment {

    private AuthViewModel viewModel;
    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnSignup;
    private CircularProgressIndicator progressSignup;
    private TextView tvError, tvGoToLogin;
    private View layoutSignupForm, layoutVerificationSent;
    private TextView tvVerifyEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);

        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupClickListeners();
        observeViewModel();
        viewModel.resetLoading();
    }

    @Override
    public void onResume() {
        super.onResume();

        btnSignup.setVisibility(View.VISIBLE);
        progressSignup.setVisibility(View.GONE);
    }

    // ─── View binding ─────────────────────────────────────────────────────────

    private void bindViews(View view) {
        tilName            = view.findViewById(R.id.tilName);
        tilEmail           = view.findViewById(R.id.tilContactEmail);
        tilPassword        = view.findViewById(R.id.tilPassword);
        tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);
        etName             = view.findViewById(R.id.etName);
        etEmail            = view.findViewById(R.id.etEmail);
        etPassword         = view.findViewById(R.id.etPassword);
        etConfirmPassword  = view.findViewById(R.id.etConfirmPassword);
        btnSignup          = view.findViewById(R.id.btnSignup);
        progressSignup     = view.findViewById(R.id.progressSignup);
        tvError            = view.findViewById(R.id.tvSignupError);
        tvGoToLogin        = view.findViewById(R.id.tvGoToLogin);
        layoutSignupForm      = view.findViewById(R.id.layoutSignupForm);
        layoutVerificationSent = view.findViewById(R.id.layoutVerificationSent);
        tvVerifyEmail         = view.findViewById(R.id.tvVerifyEmail);

        view.findViewById(R.id.btnGoToLoginFromVerify).setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showLogin();
            }
        });

        view.findViewById(R.id.ivBack).setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showLogin();
            }
        });
    }

    // ─── Clear any stale state ────────────────────────────────────────────────

    private void clearStaleErrors() {
        tvError.setVisibility(View.GONE);
        tvError.setText("");
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    // ─── Listeners ────────────────────────────────────────────────────────────

    private void setupClickListeners() {
        btnSignup.setOnClickListener(v -> attemptSignup());

        // Clear field errors when user starts re-typing
        etName.setOnFocusChangeListener((v, hasFocus)  -> { if (hasFocus) tilName.setError(null); });
        etEmail.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) tilEmail.setError(null); });
        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tilPassword.setError(null);
        });
        etConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tilConfirmPassword.setError(null);
        });

        tvGoToLogin.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showLogin();
            }
        });
    }

    // ─── Validation + signup ──────────────────────────────────────────────────

    private void attemptSignup() {
        clearStaleErrors();

        String name            = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email           = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password        = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        boolean valid = true;

        if (TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.error_name_required));
            valid = false;
        }
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_email_required));
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_password_required));
            valid = false;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_short));
            valid = false;
        }
        if (!TextUtils.isEmpty(password) && !password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_passwords_no_match));
            valid = false;
        }

        if (!valid) {
            btnSignup.setVisibility(View.VISIBLE);
            progressSignup.setVisibility(View.GONE);
            return;
        }

        viewModel.signup(name, email, password);
    }

    // ─── Observe — signupResult ONLY, not loginResult ────────────────────────

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            btnSignup.setVisibility(loading ? View.GONE : View.VISIBLE);
            progressSignup.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        // Only observe signupResult — never loginResult
        viewModel.getSignupResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return; // initial null value, ignore

            if (result.success) {
                layoutSignupForm.setVisibility(View.GONE);
                layoutVerificationSent.setVisibility(View.VISIBLE);

                tvVerifyEmail.setText(
                        etEmail.getText() != null
                                ? etEmail.getText().toString().trim()
                                : ""
                );
            } else {
                tvError.setText(result.errorMessage);
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

}