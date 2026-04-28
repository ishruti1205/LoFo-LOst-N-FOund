package com.example.lofo.util;

import android.content.Context;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Uploads an image Uri to Cloudinary using unsigned upload preset.
 * Does NOT require the Cloudinary SDK — uses plain HttpURLConnection
 * to keep the dependency minimal.
 *
 * Returns a LiveData<String> that resolves to:
 *   - the secure_url string on success
 *   - null on failure (caller should check and show error)
 */
public class CloudinaryUploader {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static LiveData<String> upload(Context context, Uri imageUri) {
        MutableLiveData<String> result = new MutableLiveData<>();

        executor.execute(() -> {
            try {
                // Read image bytes from Uri
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    result.postValue(null);
                    return;
                }
                byte[] imageBytes = inputStream.readAllBytes();
                inputStream.close();

                // Build multipart body
                String boundary    = "----FormBoundary" + UUID.randomUUID();
                String lineEnd     = "\r\n";
                String twoHyphens  = "--";

                URL url = new URL(CloudinaryConfig.UPLOAD_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data; boundary=" + boundary);

                OutputStream outputStream = conn.getOutputStream();

                // -- upload_preset field
                String presetPart = twoHyphens + boundary + lineEnd
                        + "Content-Disposition: form-data; name=\"upload_preset\"" + lineEnd
                        + lineEnd
                        + CloudinaryConfig.UPLOAD_PRESET + lineEnd;
                outputStream.write(presetPart.getBytes());

                // -- file field
                String filePart = twoHyphens + boundary + lineEnd
                        + "Content-Disposition: form-data; name=\"file\"; filename=\"lofo_item.jpg\"" + lineEnd
                        + "Content-Type: image/jpeg" + lineEnd
                        + lineEnd;
                outputStream.write(filePart.getBytes());
                outputStream.write(imageBytes);
                outputStream.write(lineEnd.getBytes());

                // -- closing boundary
                String closingBoundary = twoHyphens + boundary + twoHyphens + lineEnd;
                outputStream.write(closingBoundary.getBytes());
                outputStream.flush();
                outputStream.close();

                // Read response
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream responseStream = conn.getInputStream();
                    String responseBody = new String(responseStream.readAllBytes());
                    responseStream.close();
                    conn.disconnect();

                    JSONObject json = new JSONObject(responseBody);
                    String secureUrl = json.getString("secure_url");
                    result.postValue(secureUrl);
                } else {
                    conn.disconnect();
                    result.postValue(null);
                }

            } catch (Exception e) {
                e.printStackTrace();
                result.postValue(null);
            }
        });

        return result;
    }
}