package com.example.lofo.addpost;

import android.app.Application;
import android.net.Uri;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.lofo.model.ItemPost;
import com.example.lofo.repository.ItemRepository;

/**
 * Extends AndroidViewModel (not plain ViewModel) so we can access
 * Application context for Cloudinary's ContentResolver call.
 */
public class AddPostViewModel extends AndroidViewModel {

    private final ItemRepository repository = new ItemRepository();

    private final MutableLiveData<ItemRepository.UploadResult> uploadResult
            = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isLoading  = new MutableLiveData<>(false);
    private final MutableLiveData<Uri>     imageUri   = new MutableLiveData<>(null);

    public AddPostViewModel(Application application) {
        super(application);
    }

    public LiveData<ItemRepository.UploadResult> getUploadResult() { return uploadResult; }
    public LiveData<Boolean>                     getIsLoading()    { return isLoading; }
    public LiveData<Uri>                         getImageUri()     { return imageUri; }

    public void setSelectedImageUri(Uri uri) { imageUri.setValue(uri); }

    public void submitPost(ItemPost item, Uri selectedImageUri) {
        isLoading.setValue(true);
        uploadResult.setValue(null);

        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            repository.uploadItem(getApplication(), item, selectedImageUri)
                    .observeForever(result -> {
                        isLoading.setValue(false);
                        uploadResult.setValue(result);
                    });
        });
    }

}