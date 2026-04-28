package com.example.lofo.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repository;

    private final MutableLiveData<LoginRequest> loginTrigger = new MutableLiveData<>();
    private final MutableLiveData<SignupRequest> signupTrigger = new MutableLiveData<>();

    private final LiveData<AuthRepository.AuthResult> loginResult;
    private final LiveData<AuthRepository.AuthResult> signupResult;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public AuthViewModel() {
        repository = new AuthRepository();

        // Automatically calls repository when trigger changes
        loginResult = Transformations.switchMap(loginTrigger, req -> {
            if (req == null) return new MutableLiveData<>();
            isLoading.setValue(true);
            return repository.login(req.email, req.password);
        });

        signupResult = Transformations.switchMap(signupTrigger, req -> {
            if (req == null) return new MutableLiveData<>();
            isLoading.setValue(true);
            return repository.signup(req.name, req.email, req.password);
        });
    }

    // ─── Exposed LiveData ─────────────────────────────

    public LiveData<AuthRepository.AuthResult> getLoginResult() {
        return loginResult;
    }

    public LiveData<AuthRepository.AuthResult> getSignupResult() {
        return signupResult;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // ─── Actions ─────────────────────────────────────

    public void login(String email, String password) {
        loginTrigger.setValue(new LoginRequest(email, password));
    }

    public void signup(String name, String email, String password) {
        signupTrigger.setValue(new SignupRequest(name, email, password));
    }

    public FirebaseUser getCurrentUser() {
        return repository.getCurrentUser();
    }

    public void logout() {
        repository.logout();
    }

    // ─── Helper Classes ─────────────────────────────

    private static class LoginRequest {
        String email, password;

        LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    private static class SignupRequest {
        String name, email, password;

        SignupRequest(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
        }
    }

    public void resetLoading() {
        isLoading.setValue(false);
    }
}