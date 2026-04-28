package com.example.lofo.model;

import com.google.firebase.Timestamp;

/**
 * Firestore document model. Collection: "items"
 *
 * MANDATORY: title, category, status, locationName, description, dateLostOrFound,
 *            contactEmail, uploadedByUid, uploadedByName, createdAt
 * OPTIONAL : imageUrl, contactNumber
 */
public class ItemPost {

    private String postId; // set after fetch, NOT stored in Firestore doc

    // ── Mandatory ──────────────────────────────────────────────────────────────
    private String title;
    private String category;
    private String status;         // "lost" | "found" | "resolved"
    private String locationName;
    private String description;
    private String contactEmail;   // auto-filled from Firebase Auth, read-only
    private String uploadedByUid;
    private String uploadedByName;
    private Timestamp createdAt;

    // ── Optional ───────────────────────────────────────────────────────────────
    private String imageUrl;
    private String contactNumber;  // clickable → opens Phone dialer
    private String dateLostOrFound; // "dd/MM/yyyy" — from NumberPicker date wheel

    public ItemPost() {} // required for Firestore deserialization

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public String    getPostId()                  { return postId; }
    public void      setPostId(String v)          { postId = v; }

    public String    getTitle()                   { return title; }
    public void      setTitle(String v)           { title = v; }

    public String    getCategory()                { return category; }
    public void      setCategory(String v)        { category = v; }

    public String    getStatus()                  { return status; }
    public void      setStatus(String v)          { status = v; }

    public String    getLocationName()            { return locationName; }
    public void      setLocationName(String v)    { locationName = v; }

    public String    getDescription()             { return description; }
    public void      setDescription(String v)     { description = v; }

    public String    getContactEmail()            { return contactEmail; }
    public void      setContactEmail(String v)    { contactEmail = v; }

    public String    getUploadedByUid()           { return uploadedByUid; }
    public void      setUploadedByUid(String v)   { uploadedByUid = v; }

    public String    getUploadedByName()          { return uploadedByName; }
    public void      setUploadedByName(String v)  { uploadedByName = v; }

    public Timestamp getCreatedAt()               { return createdAt; }
    public void      setCreatedAt(Timestamp v)    { createdAt = v; }

    public String    getImageUrl()                { return imageUrl; }
    public void      setImageUrl(String v)        { imageUrl = v; }

    public String    getContactNumber()           { return contactNumber; }
    public void      setContactNumber(String v)   { contactNumber = v; }

    public String    getDateLostOrFound()         { return dateLostOrFound; }
    public void      setDateLostOrFound(String v) { dateLostOrFound = v; }
}