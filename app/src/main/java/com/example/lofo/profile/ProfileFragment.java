package com.example.lofo.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lofo.R;
import com.example.lofo.auth.AuthActivity;
import com.example.lofo.home.ItemPostAdapter;
import com.example.lofo.itemdetail.ItemDetailFragment;
import com.example.lofo.model.ItemPost;
import com.example.lofo.repository.ItemRepository;
import com.example.lofo.util.ToastUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private FirebaseUser user;
    private ItemRepository itemRepository;

    // My Posts recycler
    private RecyclerView rvMyPosts;
    private ItemPostAdapter myPostsAdapter;
    private View layoutMyPostsEmpty;
    private TabLayout myPostsTabs;

    private static final String[] MY_POST_FILTERS = {"all", "lost", "found", "resolved"};

    public ProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();
        itemRepository = new ItemRepository();

        // Status bar inset on top bar
        View root = view.findViewById(R.id.profileTopBar);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), 0);
            return insets;
        });

        // Bottom padding so logout button clears the bottom nav bar
        View scrollView = view.findViewById(R.id.profileScroll);
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            int navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            // Add bottom padding to entire scroll content
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), navBar + dpToPx(30)); // small extra spacing
            return insets;
        });

        populateUserInfo(view);
        setupMyPosts(view);
        setupStatsFromFirestore(view);
        setupMenuOptions(view);
        setupLogout(view);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // ─── User info ────────────────────────────────────────────────────────────

    private void populateUserInfo(View view) {
        if (user == null) return;
        TextView tvName  = view.findViewById(R.id.tvUserName);
        TextView tvEmail = view.findViewById(R.id.tvUserEmail);
        String name = user.getDisplayName();
        tvName.setText((name != null && !name.isEmpty()) ? name
                : getString(R.string.placeholder_name));
        tvEmail.setText(user.getEmail() != null ? user.getEmail()
                : getString(R.string.placeholder_email_contact));
    }

    // ─── My Posts section ─────────────────────────────────────────────────────

    private void setupMyPosts(View view) {
        rvMyPosts         = view.findViewById(R.id.rvMyPosts);
        layoutMyPostsEmpty = view.findViewById(R.id.layoutMyPostsEmpty);
        myPostsTabs       = view.findViewById(R.id.myPostsTabs);

        myPostsAdapter = new ItemPostAdapter(post ->
                ItemDetailFragment.newInstance(post)
                        .show(getParentFragmentManager(), "ItemDetail"));

        rvMyPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMyPosts.setAdapter(myPostsAdapter);
        rvMyPosts.setItemViewCacheSize(0);
        rvMyPosts.setNestedScrollingEnabled(false);

        loadMyPosts("all");

        myPostsTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                myPostsAdapter.submitList(new ArrayList<>()); // clear instantly
                rvMyPosts.setVisibility(View.GONE);
                layoutMyPostsEmpty.setVisibility(View.VISIBLE);

                loadMyPosts(MY_POST_FILTERS[tab.getPosition()]);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadMyPosts(String filter) {
        if (user == null) return;
        String uid = user.getUid();

        LiveDataHelper<java.util.List<ItemPost>> liveData = "all".equals(filter)
                ? wrap(itemRepository.getAllMyItemsLive(uid))
                : wrap(itemRepository.getMyItemsByStatusLive(uid, filter));

        liveData.get().observe(getViewLifecycleOwner(), posts -> {
            if (posts == null || posts.isEmpty()) {
                myPostsAdapter.submitList(new ArrayList<>()); // force clear
            } else {
                myPostsAdapter.submitList(posts);
            }
            boolean empty = posts == null || posts.isEmpty();
            rvMyPosts.setVisibility(empty ? View.GONE : View.VISIBLE);
            layoutMyPostsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
    }

    // Simple passthrough wrapper (avoids generic wildcard issues with observeForever)
    private <T> LiveDataHelper<T> wrap(androidx.lifecycle.LiveData<T> ld) {
        return () -> ld;
    }

    @FunctionalInterface
    private interface LiveDataHelper<T> {
        androidx.lifecycle.LiveData<T> get();
    }

    // ─── Stats (counts by status) ─────────────────────────────────────────────

    private void setupStatsFromFirestore(View view) {
        if (user == null) return;
        TextView tvLost     = view.findViewById(R.id.tvLostCount);
        TextView tvFound    = view.findViewById(R.id.tvFoundCount);
        TextView tvResolved = view.findViewById(R.id.tvResolvedCount);

        String uid = user.getUid();

        itemRepository.getMyItemsByStatusLive(uid, "lost")
                .observe(getViewLifecycleOwner(), posts -> {
                    int count = (posts == null) ? 0 : posts.size();
                    tvLost.setText(String.valueOf(count));
                });

        itemRepository.getMyItemsByStatusLive(uid, "found")
                .observe(getViewLifecycleOwner(), posts -> {
                    int count = (posts == null) ? 0 : posts.size();
                    tvFound.setText(String.valueOf(count));
                });

        itemRepository.getMyItemsByStatusLive(uid, "resolved")
                .observe(getViewLifecycleOwner(), posts -> {
                    int count = (posts == null) ? 0 : posts.size();
                    tvResolved.setText(String.valueOf(count));
                });
    }

    // ─── Menu options ─────────────────────────────────────────────────────────

    private void setupMenuOptions(View view) {

        // Edit Profile → full screen fragment
        view.findViewById(R.id.ivEditProfile).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                                android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.fragmentContainer, new EditProfileFragment())
                        .addToBackStack("editProfile")
                        .commit());

        // Messages (future)
        view.findViewById(R.id.ivMessages).setOnClickListener(v ->
                ToastUtils.show(requireContext(),
                        getString(R.string.chat_coming_soon)));

        // Notifications (future)
        view.findViewById(R.id.ivNotifications).setOnClickListener(v ->
                ToastUtils.show(requireContext(),
                        getString(R.string.notifications_coming_soon)));

        // Help & Support
        view.findViewById(R.id.ivHelp).setOnClickListener(v ->
                openHelpAndSupport());
    }

    // ─── Help & Support ───────────────────────────────────────────────────────

    private void openHelpAndSupport() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.fragmentContainer, new HelpSupportFragment())
                .addToBackStack("help")
                .commit();
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    private void setupLogout(View view) {
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.dialog_logout_title))
                    .setMessage(getString(R.string.dialog_logout_message))
                    .setPositiveButton(getString(R.string.btn_logout), (d, w) -> {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(requireContext(), AuthActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    })
                    .setNegativeButton(getString(R.string.btn_cancel), null)
                    .create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(R.color.color_accent));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(getResources().getColor(R.color.icon_tint));
        });
    }
}