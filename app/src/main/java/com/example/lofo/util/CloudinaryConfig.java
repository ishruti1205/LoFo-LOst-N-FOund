package com.example.lofo.util;

import com.example.lofo.BuildConfig;

/**
 * Cloudinary configuration constants.
 * Replace these three values with your actual Cloudinary dashboard values.
 *
 * Dashboard → https://cloudinary.com/console
 *   CLOUD_NAME   → shown on dashboard home
 *   API_KEY      → shown on dashboard home
 *   UPLOAD_PRESET → Settings → Upload → Upload Presets → create one set to "Unsigned"
 */
public class CloudinaryConfig {

    public static final String CLOUD_NAME    = BuildConfig.CLOUDINARY_CLOUD_NAME;
    public static final String API_KEY       = BuildConfig.CLOUDINARY_API_KEY;
    public static final String UPLOAD_PRESET = BuildConfig.CLOUDINARY_UPLOAD_PRESET;

    public static final String UPLOAD_URL =
            "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";
}