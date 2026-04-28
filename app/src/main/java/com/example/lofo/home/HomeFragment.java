package com.example.lofo.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lofo.R;
import com.example.lofo.itemdetail.ItemDetailFragment;
import com.example.lofo.model.ItemPost;
import com.example.lofo.util.ToastUtils;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private ItemPostAdapter adapter;
    private RecyclerView rvItems;
    private View layoutEmpty;
    private TextView tvEmptyMessage;
    private TabLayout tabLayout;
    private EditText etSearch;
    private ImageView ivSearchFilter;

    private static final String[] TAB_FILTERS = {"all", "lost", "found", "resolved"};
    private static final String[] TAB_LABELS  = {"items", "lost items",
            "found items", "resolved items"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View root = view.findViewById(R.id.headerContainer);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), 0);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.rvItems),
                (v, insets) -> {
                    int navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                    v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
                            v.getPaddingRight(), navBar + dpToPx(50));
                    return WindowInsetsCompat.CONSUMED;
                });

        rvItems        = view.findViewById(R.id.rvItems);
        layoutEmpty    = view.findViewById(R.id.layoutEmpty);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        tabLayout      = view.findViewById(R.id.tabLayout);
        etSearch       = view.findViewById(R.id.etSearch);
        ivSearchFilter = view.findViewById(R.id.ivSearchFilter);

        setupMenuOptions(root);
        setupRecyclerView();
        setupViewModel();

        getParentFragmentManager().setFragmentResultListener(
                "refresh_posts",
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    // OPTIONAL: force refresh logic if needed
                    viewModel.setFilter(viewModel.getActiveFilter().getValue());
                }
        );

        setupTabs();
        setupSearch();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void setupMenuOptions(View view) {
        view.findViewById(R.id.ivMessages).setOnClickListener(v ->
                ToastUtils.show(requireContext(), getString(R.string.chat_coming_soon)));
        view.findViewById(R.id.ivNotifications).setOnClickListener(v ->
                ToastUtils.show(requireContext(), getString(R.string.notifications_coming_soon)));
    }

    private void setupRecyclerView() {
        adapter = new ItemPostAdapter(this::onItemClicked);
        rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvItems.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            adapter.submitList(posts == null ? new ArrayList<>() : posts);

            boolean empty = posts == null || posts.isEmpty();
            rvItems.setVisibility(empty ? View.GONE : View.VISIBLE);
            layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);

            // Show contextual empty message
            if (empty && tvEmptyMessage != null) {
                String query = viewModel.getSearchQuery().getValue();
                int tabPos   = tabLayout.getSelectedTabPosition();
                String label = tabPos >= 0 && tabPos < TAB_LABELS.length
                        ? TAB_LABELS[tabPos] : "items";

                if (query != null && !query.trim().isEmpty()) {
                    tvEmptyMessage.setText("No results for \"" + query.trim() + "\" in " + label);
                } else {
                    tvEmptyMessage.setText("No " + label + " yet");
                }
            }
        });
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                adapter.submitList(new ArrayList<>());
                rvItems.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
                viewModel.setFilter(TAB_FILTERS[tab.getPosition()]);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // ─── Search ───────────────────────────────────────────────────────────────

    private void setupSearch() {
        // Live search as user types
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                viewModel.setSearchQuery(s.toString());
                // Show/hide the clear (X) button on the filter icon
                ivSearchFilter.setImageResource(
                        s.length() > 0 ? R.drawable.ic_close : R.drawable.ic_tune);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Filter icon: if text present → clears search; otherwise could show future filter
        ivSearchFilter.setOnClickListener(v -> {
            if (etSearch.getText() != null && etSearch.getText().length() > 0) {
                etSearch.setText("");
                viewModel.clearSearch();
            } else {
                ToastUtils.show(requireContext(), getString(R.string.filter_coming_soon));
            }
        });
    }

    private void onItemClicked(ItemPost post) {
        ItemDetailFragment.newInstance(post)
                .show(getParentFragmentManager(), "ItemDetail");
    }
}