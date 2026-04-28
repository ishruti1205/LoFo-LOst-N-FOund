package com.example.lofo.auth;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.example.lofo.R;
import android.content.Intent;
import com.example.lofo.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

/**
 * AuthActivity — entry point for unauthenticated users.
 * If Firebase already has a signed-in user (stay logged in),
 * it skips straight to MainActivity.
 */

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

        // White status bar icons on dark topbar
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false); // false = WHITE icons
        }

        // ── Stay logged in check ──────────────────────────────────────────────
        // Firebase Auth persists the session automatically between app launches.
        // If getCurrentUser() is not null, the user chose "Stay logged in"
        // (or simply hasn't logged out), so we skip the auth flow entirely.
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            goToMain();
            return; // don't set content view or load fragments
        }

        // ── Not logged in: show login screen ──────────────────────────────────
        setContentView(R.layout.activity_auth); // called ONCE, only if not logged in

        // Load LoginFragment as the default screen
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.authFragmentContainer, new LoginFragment())
                    .commit();
        }
    }

    // ─── Navigation helper ────────────────────────────────────────────────────

    public void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        // Clear the back stack so pressing Back from MainActivity
        // doesn't return to the login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void showSignup() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right)
                .replace(R.id.authFragmentContainer, new SignupFragment())
                .addToBackStack("signup")
                .commit();
    }

    public void showLogin() {
        // Pop back to LoginFragment
        getSupportFragmentManager().popBackStack();
    }
}