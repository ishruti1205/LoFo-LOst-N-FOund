package com.example.lofo.repository;

import android.content.Context;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.lofo.model.ItemPost;
import com.example.lofo.util.CloudinaryUploader;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ItemRepository — Firestore for item data, Cloudinary for images.
 * No Firebase Storage used anywhere.
 */
public class ItemRepository {

    private static final String COLLECTION = "items";

    private final FirebaseFirestore db;

    public ItemRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // ─── Result wrapper ───────────────────────────────────────────────────────

    public static class UploadResult {
        public final boolean success;
        public final String errorMessage;
        public UploadResult(boolean success, String errorMessage) {
            this.success      = success;
            this.errorMessage = errorMessage;
        }
    }

    // ─── Real-time feeds ─────────────────────────────────────────────────────

    public LiveData<List<ItemPost>> getAllItemsLive() {
        return queryLive(
                db.collection(COLLECTION)
                        .orderBy("createdAt", Query.Direction.DESCENDING));
    }

    public LiveData<List<ItemPost>> getItemsByStatusLive(String status) {
        return queryLive(
                db.collection(COLLECTION)
                        .whereEqualTo("status", status)
                        .orderBy("createdAt", Query.Direction.DESCENDING));
    }


    // ─── Posts by current user (for Profile "My Posts") ───────────────────────

    public LiveData<List<ItemPost>> getMyItemsByStatusLive(String uid, String status) {
        Query q = db.collection(COLLECTION).whereEqualTo("uploadedByUid", uid);
        if (status != null) q = q.whereEqualTo("status", status);
        return queryLive(q.orderBy("createdAt", Query.Direction.DESCENDING));
    }

    public LiveData<List<ItemPost>> getAllMyItemsLive(String uid) {
        return queryLive(db.collection(COLLECTION)
                .whereEqualTo("uploadedByUid", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING));
    }

    private LiveData<List<ItemPost>> queryLive(Query query) {
        MutableLiveData<List<ItemPost>> liveData = new MutableLiveData<>();

        query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                android.util.Log.e("FirestoreError", "Query failed", error);
                return;
            }

            if (snapshots == null) return;

            List<ItemPost> items = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshots) {
                ItemPost item = doc.toObject(ItemPost.class);
                item.setPostId(doc.getId());
                items.add(item);
            }

            liveData.setValue(items);
        });

        return liveData;
    }

    // ─── Upload new item (image → Cloudinary, data → Firestore) ─────────────────

    /**
     * Flow:
     *   1. If imageUri != null → upload to Cloudinary → get secure URL
     *   2. Set imageUrl on the item (or leave null if no image)
     *   3. Save ItemPost document to Firestore
     */
    public LiveData<UploadResult> uploadItem(Context context, ItemPost item, Uri imageUri) {
        MutableLiveData<UploadResult> result = new MutableLiveData<>();

        // Stamp creator info and timestamp
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            item.setUploadedByUid(user.getUid());
            item.setUploadedByName(
                    user.getDisplayName() != null ? user.getDisplayName() : "Anonymous");
        }
        item.setCreatedAt(Timestamp.now());

        if (imageUri == null) {
            // No image — save directly to Firestore
            saveToFirestore(item, result);
        } else {
            // Upload image to Cloudinary first
            CloudinaryUploader.upload(context, imageUri).observeForever(url -> {
                if (url != null) {
                    item.setImageUrl(url);
                } else {
                    // Image upload failed — post without image rather than blocking
                    // (the form already marks image as optional)
                    item.setImageUrl(null);
                }
                saveToFirestore(item, result);
            });
        }

        return result;
    }

    private void saveToFirestore(ItemPost item, MutableLiveData<UploadResult> result) {
        db.collection(COLLECTION)
                .add(item)
                .addOnSuccessListener(ref -> result.setValue(new UploadResult(true, null)))
                .addOnFailureListener(e  -> result.setValue(
                        new UploadResult(false, "Failed to post. Please try again.")));
    }

    // ─── Update item status (lost → resolved / found → resolved) ─────────────

    public LiveData<UploadResult> updateStatus(String postId, String newStatus) {
        MutableLiveData<UploadResult> result = new MutableLiveData<>();
        db.collection(COLLECTION).document(postId)
                .update("status", newStatus)
                .addOnSuccessListener(v -> result.setValue(new UploadResult(true, null)))
                .addOnFailureListener(e -> result.setValue(
                        new UploadResult(false, "Failed to update status.")));
        return result;
    }

    // ─── Edit item fields (title, description, location, date, phone) ─────────

    public LiveData<UploadResult> updateItemFields(String postId,
                                                   String title,
                                                   String description,
                                                   String locationName,
                                                   String dateLostOrFound,
                                                   String contactNumber) {
        MutableLiveData<UploadResult> result = new MutableLiveData<>();
        Map<String, Object> updates = new HashMap<>();
        updates.put("title",           title);
        updates.put("description",     description);
        updates.put("locationName",    locationName);
        updates.put("dateLostOrFound", dateLostOrFound);
        updates.put("contactNumber",   contactNumber);

        db.collection(COLLECTION).document(postId)
                .update(updates)
                .addOnSuccessListener(v -> result.setValue(new UploadResult(true, null)))
                .addOnFailureListener(e -> result.setValue(
                        new UploadResult(false, "Failed to update item.")));
        return result;
    }

    // ─── Delete item ──────────────────────────────────────────────────────────

    public LiveData<UploadResult> deleteItem(String postId) {
        MutableLiveData<UploadResult> result = new MutableLiveData<>();
        db.collection(COLLECTION).document(postId)
                .delete()
                .addOnSuccessListener(v -> result.setValue(new UploadResult(true, null)))
                .addOnFailureListener(e -> result.setValue(
                        new UploadResult(false, "Failed to delete item.")));
        return result;
    }
}