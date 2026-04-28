package com.example.lofo.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.lofo.model.ItemPost;
import com.example.lofo.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeViewModel extends ViewModel {

    private final ItemRepository repository = new ItemRepository();

    // ── Tab filter (all / lost / found / resolved) ────────────────────────────
    private final MutableLiveData<String> activeFilter = new MutableLiveData<>("all");

    // ── Search query ──────────────────────────────────────────────────────────
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    // ── Raw list from Firestore (responds to tab filter) ─────────────────────
    private final LiveData<List<ItemPost>> rawPosts =
            Transformations.switchMap(activeFilter, filter -> {
                if ("all".equals(filter)) return repository.getAllItemsLive();
                return repository.getItemsByStatusLive(filter);
            });

    // ── Filtered list (applies search on top of raw) ──────────────────────────
    // MediatorLiveData observes BOTH rawPosts and searchQuery.
    // Any change to either source triggers a re-filter.
    private final MediatorLiveData<List<ItemPost>> posts = new MediatorLiveData<>();

    public HomeViewModel() {
        posts.addSource(rawPosts,     list  -> applySearch(list, searchQuery.getValue()));
        posts.addSource(searchQuery,  query -> applySearch(rawPosts.getValue(), query));
    }

    private void applySearch(List<ItemPost> list, String query) {
        if (list == null) { posts.setValue(new ArrayList<>()); return; }
        if (query == null || query.trim().isEmpty()) { posts.setValue(list); return; }

        String q = query.toLowerCase(Locale.getDefault()).trim();
        List<ItemPost> filtered = new ArrayList<>();
        for (ItemPost item : list) {
            if (matches(item.getTitle(),        q) ||
                    matches(item.getCategory(),     q) ||
                    matches(item.getLocationName(), q) ||
                    matches(item.getDescription(),  q) ||
                    matches(item.getUploadedByName(), q)) {
                filtered.add(item);
            }
        }
        posts.setValue(filtered);
    }

    private boolean matches(String field, String query) {
        return field != null && field.toLowerCase(Locale.getDefault()).contains(query);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public LiveData<List<ItemPost>> getPosts()        { return posts; }
    public LiveData<String>         getActiveFilter() { return activeFilter; }
    public LiveData<String>         getSearchQuery()  { return searchQuery; }

    public void setFilter(String filter) {
        if (!filter.equals(activeFilter.getValue())) {
            activeFilter.setValue(filter);
        }
    }



    public void setSearchQuery(String query) {
        searchQuery.setValue(query == null ? "" : query);
    }

    public void clearSearch() {
        searchQuery.setValue("");
    }
}