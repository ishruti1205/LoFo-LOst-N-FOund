package com.example.lofo.auth;

import android.os.Bundle;
import com.example.lofo.R;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {

    private AuthViewModel viewModel;
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private CheckBox cbStayLoggedIn;
    private MaterialButton btnLogin;
    private TextView tvError, tvGoToSignup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);

        // Share the ViewModel with AuthActivity scope so Login + Signup share one instance
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupClickListeners();
        observeViewModel();
        viewModel.resetLoading();
    }

    @Override
    public void onResume() {
        super.onResume();
        resetLoginForm();
    }

    // ─── View binding ─────────────────────────────────────────────────────────

    private void bindViews(View view) {
        tilEmail       = view.findViewById(R.id.tilContactEmail);
        tilPassword    = view.findViewById(R.id.tilPassword);
        etEmail        = view.findViewById(R.id.etEmail);
        etPassword     = view.findViewById(R.id.etPassword);
        cbStayLoggedIn = view.findViewById(R.id.cbStayLoggedIn);
        btnLogin       = view.findViewById(R.id.btnLogin);
        tvError        = view.findViewById(R.id.tvLoginError);
        tvGoToSignup   = view.findViewById(R.id.tvGoToSignup);
    }

    // ─── Clear any stale state from previous navigation ───────────────────────

    private void clearStaleErrors() {
        tvError.setVisibility(View.GONE);
        tvError.setText("");
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    // ─── Listeners ────────────────────────────────────────────────────────────

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Clear field errors when user starts typing again
        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tilEmail.setError(null);
        });
        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tilPassword.setError(null);
                tvError.setVisibility(View.GONE);
            }
        });

        tvGoToSignup.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                clearStaleErrors();
                ((AuthActivity) getActivity()).showSignup();
            }
        });
    }

    // ─── Validation + login ───────────────────────────────────────────────────

    private void attemptLogin() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        tvError.setVisibility(View.GONE);

        String email    = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_email_required));
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_password_required));
            valid = false;
        }

        if (!valid) {
            btnLogin.setEnabled(true);
            btnLogin.setText(getString(R.string.btn_login));
            return;
        }

        viewModel.login(email, password);
    }

    // ─── Observe — loginResult ONLY, not signupResult ────────────────────────

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {

            boolean errorVisible = tvError.getVisibility() == View.VISIBLE;

            if (loading && !errorVisible) {
                btnLogin.setEnabled(false);
                btnLogin.setText(getString(R.string.label_logging_in));
            } else {
                btnLogin.setEnabled(true);
                btnLogin.setText(getString(R.string.btn_login));
            }
        });

        viewModel.getLoginResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;

            if (result.success) {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).goToMain();
                }
            } else {
                tvError.setText(result.errorMessage);
                tvError.setVisibility(View.VISIBLE);

                // FORCE BUTTON RESET ON ERROR
                btnLogin.setEnabled(true);
                btnLogin.setText(getString(R.string.btn_login));
            }
        });
    }

    private void resetLoginForm() {
        etEmail.setText("");
        etPassword.setText("");

        tilEmail.setError(null);
        tilPassword.setError(null);

        tvError.setText("");
        tvError.setVisibility(View.GONE);

        btnLogin.setEnabled(true);
        btnLogin.setText(getString(R.string.btn_login));
    }
}