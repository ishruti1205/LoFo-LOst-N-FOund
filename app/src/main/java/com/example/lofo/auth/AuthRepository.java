package com.example.lofo.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {

    private final FirebaseAuth auth;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
    }

    public static class AuthResult {
        public final boolean success;
        public final String errorMessage;
        public AuthResult(boolean success, String errorMessage) {
            this.success      = success;
            this.errorMessage = errorMessage;
        }
    }

    // ─── Login ────────────────────────────────────────────────────────────────
    // Blocks unverified accounts from entering the app.

    public LiveData<AuthResult> login(String email, String password) {
        MutableLiveData<AuthResult> result = new MutableLiveData<>();
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
//                    FirebaseUser user = auth.getCurrentUser();
//                    if (user != null && !user.isEmailVerified()) {
//                        auth.signOut();
//                        result.setValue(new AuthResult(false,
//                                "Please verify your email before logging in. Check your inbox."));
//                    } else {
//                        result.setValue(new AuthResult(true, null));
//                    }
                    FirebaseUser user = authResult.getUser();

                    if (user == null) {
                        result.setValue(new AuthResult(false, "Login failed."));
                        return;
                    }

                    if (!user.isEmailVerified()) {
                        auth.signOut();
                        result.setValue(new AuthResult(false,
                                "Please verify your email before logging in. Check inbox or spam."));
                        return;
                    }

                    // EMAIL SYNC HAPPENS HERE (ONLY AFTER LOGIN SUCCESS)
                    String uid = user.getUid();
                    String newEmail = user.getEmail();

                    FirebaseFirestore.getInstance()
                            .collection("items")
                            .whereEqualTo("uploadedByUid", uid)
                            .get()
                            .addOnSuccessListener(snapshots -> {
                                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                    doc.getReference().update("contactEmail", newEmail);
                                }
                            });

                    result.setValue(new AuthResult(true, null));
                })

                .addOnFailureListener(e -> {
                    String msg;
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        msg = "Incorrect email or password.";
                    } else {
                        msg = "Login failed. Please try again.";
                    }
                    result.setValue(new AuthResult(false, msg));
                });
        return result;
    }

    // ─── Signup ───────────────────────────────────────────────────────────────
    // After account creation:
    //  1. Set display name
    //  2. Send email verification link
    //  3. Sign out (user must verify before first login)
    //  4. Return success so SignupFragment can show "check your email" screen

    public LiveData<AuthResult> signup(String name, String email, String password) {
        MutableLiveData<AuthResult> result = new MutableLiveData<>();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        result.setValue(new AuthResult(false, "Signup failed. Please try again."));
                        return;
                    }
                    // Set display name first
                    user.updateProfile(new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build())
                            .addOnCompleteListener(t ->
                                    // Then send verification regardless of profile update outcome
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(emailTask -> {
                                                // Sign out in all cases — verification required
                                                auth.signOut();
                                                result.setValue(new AuthResult(true, null));
                                            }));
                })
                .addOnFailureListener(e -> {
                    String msg;
                    if (e instanceof FirebaseAuthWeakPasswordException) {
                        msg = "Password must be at least 6 characters.";
                    } else if (e instanceof FirebaseAuthUserCollisionException) {
                        msg = "An account with this email already exists.";
                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        msg = "Please enter a valid email address.";
                    } else {
                        msg = "Signup failed. Please try again.";
                    }
                    result.setValue(new AuthResult(false, msg));
                });
        return result;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void logout() {
        auth.signOut();
    }
}