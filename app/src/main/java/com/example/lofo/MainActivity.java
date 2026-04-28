package com.example.lofo;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import com.example.lofo.addpost.AddPostFragment;
import com.example.lofo.auth.AuthActivity;
import com.example.lofo.home.HomeFragment;
import com.example.lofo.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // ── Auth guard: if no user is logged in, go back to AuthActivity ──────
        // This protects MainActivity if someone navigates here directly.
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, AuthActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // White status bar icons on dark topbar
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(getWindow().getDecorView());

        if (controller != null) {
            controller.setAppearanceLightStatusBars(false); // false = WHITE icons
        }

        // Default fragment - HomeFragment()
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new HomeFragment())
                    .commit();
        }

        // ── Bottom Nav ────────────────────────────────────────────────────────
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            if (item.getItemId() == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit();
            }

            return true;
        });

        // ── FAB ───────────────────────────────────────────────────────────────
//        FloatingActionButton fab = findViewById(R.id.fabAddPost);
        MaterialButton fab = findViewById(R.id.fabAddPost);
//        fab.bringToFront();

        fab.setOnClickListener(v -> {
            AddPostFragment sheet = new AddPostFragment();
            sheet.show(getSupportFragmentManager(), "AddPostSheet");
        });
    }
}