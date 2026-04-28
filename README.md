# LoFo — LOst & FOund

An Android application that makes it easier to report, search, and recover lost or found items in a community or campus setting.

Built with Java and Android Studio, using Firebase for authentication and real-time data, and Cloudinary for image storage.

---

## Try the App (APK)

You can install and test the app directly on your Android device without setting up the project locally.
 
Download `download_app.apk` from the root of this repository.
 
> Enable **Install from Unknown Sources** on your device before installing (Settings → Security → Unknown Sources).
 

---

## What it does

People lose things. Someone else finds them. The problem has always been connecting the two. LoFo is a centralized platform where users can post lost or found items with photos and contact details, browse a real-time feed, and reach out to each other directly.
 
- Post a lost or found item with title, category, location, date, description, and an optional photo
- Browse the full community feed or filter by Lost / Found / Resolved tabs
- Search live by keyword — matches against title, category, location, and description
- Tap any card to open the full item detail — contact email and phone number are tappable
- If you posted the item, you can edit it, delete it, or mark it as resolved
- Profile page shows your post counts by status and a filterable list of all your posts

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| UI | XML layouts, Material Design 3 |
| Architecture | MVVM (ViewModel + LiveData) |
| Auth | Firebase Authentication (email/password + verification) |
| Database | Cloud Firestore (real-time NoSQL) |
| Image storage | Cloudinary (unsigned upload preset) |
| Image loading | Glide |
| Build | Gradle (Kotlin DSL) |

---

## Project structure

```
com.example.lofo/
├── auth/           AuthActivity, AuthViewModel, LoginFragment, SignupFragment
├── home/           HomeFragment, HomeViewModel, ItemPostAdapter
├── addpost/        AddPostFragment, AddPostFormFragment, AddPostViewModel
├── itemdetail/     ItemDetailFragment
├── profile/        ProfileFragment, ProfileViewModel, EditProfileFragment, HelpSupportFragment
├── model/          ItemPost
├── repository/     ItemRepository
└── util/           CloudinaryConfig, CloudinaryUploader, ToastUtils
```

---

## Screenshots

### Splash & Home Feed
<img src="Screenshots/splash.png" width="220"> 
<img src="Screenshots/home_feed.png" width="220">

### Login & Sign Up
<img src="Screenshots/login.png" width="220"> 
<img src="Screenshots/signup.png" width="220"> 
<img src="Screenshots/verify_email.png" width="220">

### Home — Tab Filtering
<img src="Screenshots/tab_all.png" width="220"> 
<img src="Screenshots/tab_lost.png" width="220"> 
<img src="Screenshots/tab_found.png" width="220"> 
<img src="Screenshots/tab_resolved.png" width="220">

### Search
<img src="Screenshots/search.png" width="220">

### Item Detail & Owner Options
<img src="Screenshots/item_detail_1.png" width="220"> 
<img src="Screenshots/item_detail_2.png" width="220"> 
<img src="Screenshots/item_detail_3.png" width="220"> 
<br> 
<img src="Screenshots/owner_options.png" width="220"> 
<img src="Screenshots/mark_as_resolved.png" width="220"> 
<img src="Screenshots/edit_item.png" width="220"> 
<img src="Screenshots/delete_confirm.png" width="220">

### Add Post
<img src="Screenshots/add_post_bottom_sheet.png" width="220"> 
<img src="Screenshots/add_post_form_1.png" width="220">
<img src="Screenshots/add_post_form_2.png" width="220">

### Profile
<img src="Screenshots/profile.png" width="220"> 
<img src="Screenshots/my_posts.png" width="220">

### Edit Profile
<img src="Screenshots/edit_profile_closed.png" width="220"> 
<img src="Screenshots/edit_profile_name.png" width="220"> 
<img src="Screenshots/edit_profile_email.png" width="220"> 
<img src="Screenshots/edit_profile_password.png" width="220">

### Help & Support / Logout
<img src="Screenshots/help_support.png" width="220"> 
<img src="Screenshots/logout.png" width="220">

---

## Setup

This project requires a Firebase project and a Cloudinary account. Neither `google-services.json` nor any API credentials are included in this repository — you need to supply your own.

**Firebase**

1. Go to [Firebase Console](https://console.firebase.google.com) and create a new project.
2. Add an Android app with package name `com.example.lofo`.
3. Download `google-services.json` and place it inside the `app/` directory.
4. Enable **Email/Password** sign-in under Authentication → Sign-in method.
5. Enable **Email enumeration protection** under Authentication → Settings (recommended).
6. Create a Firestore database in your preferred region.
7. Apply the following security rules under Firestore → Rules:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
 
    match /items/{itemId} {
      allow read: if true;
 
      allow create: if request.auth != null
                    && request.auth.token.email_verified == true
                    && request.resource.data.uploadedByUid == request.auth.uid
                    && request.resource.data.title is string
                    && request.resource.data.title.size() > 0
                    && request.resource.data.status in ['lost', 'found'];
 
      allow update: if request.auth != null
                    && request.auth.token.email_verified == true
                    && resource.data.uploadedByUid == request.auth.uid;
 
      allow delete: if request.auth != null
                    && resource.data.uploadedByUid == request.auth.uid;
    }
 
    match /users/{userId} {
      allow read, write: if request.auth != null
                         && request.auth.uid == userId;
    }
  }
}
```
 
8. Create a composite index on the `items` collection: `status ASC, createdAt DESC`
   (Firestore will prompt you with a direct link the first time the query runs)

**Cloudinary**

Credentials are not hardcoded in the project. They are read from `local.properties` at build time.
 
1. Create a free account at [cloudinary.com](https://cloudinary.com)
2. Go to Settings → Upload → Upload Presets → add a new preset set to **Unsigned**
3. Create or open `local.properties` at the project root and add:
```
cloudinary.cloud_name=YOUR_CLOUD_NAME
cloudinary.api_key=YOUR_API_KEY
cloudinary.upload_preset=YOUR_PRESET_NAME
```
 
`local.properties` is listed in `.gitignore` and will never be committed.

**Build**

Open the project in Android Studio, let Gradle sync, then run on a device or emulator (API 26+).

---

## Features in detail

**Authentication**
Signup sends a verification email immediately after account creation. The app blocks login for unverified accounts. Email and password changes require re-authentication with the current password, and the user is signed out afterward — they must log back in with the updated credentials.
 
**Add Post**
The + FAB opens a bottom sheet chooser (Lost or Found). The form collects item name, category (dropdown), location, date (day/month/year scroll picker), description, and an optional photo. The Posted By name and contact email auto-fill from the logged-in account. Images upload to Cloudinary and the returned URL is stored in Firestore.
 
**Item Detail**
Opens as a bottom sheet. Shows full item info with relative timestamps ("3 days ago"). Contact email opens a mail client on tap; phone number opens the dialer. Long-pressing either copies it to clipboard. For the item owner, a three-dot icon opens a styled bottom sheet menu with Mark as Resolved, Edit Post, and Delete Post options.
 
**Search and Filter**
Typing in the search bar filters results in real time using a `MediatorLiveData` that observes both the active Firestore query and the search string simultaneously — so search works correctly within whichever tab (Lost, Found, Resolved, All) is currently selected. The filter icon switches to an X while text is present to clear the search quickly.
 
**Edit Profile**
Three accordion sections — Display Name, Change Email, Change Password — all collapsed by default. Only one section can be open at a time. Email and password changes require the current password for re-authentication before proceeding.

---

## What's Not in Yet

- In-app chat between users (Firebase Realtime Database)
- Push notifications via FCM (Firebase Cloud Messaging)
- Advanced search filters (category, date range)
- Location-based filtering / map view (Google Maps API)
- Welcome screens for new users
- Responsiveness
- Revenue Model:
    - Optional “Buy Me A Coffee” feature  
    - Triggered when a lost item is marked as resolved  
    - Redirects user to support the developer  
    - Future scope includes payment gateway integration and transaction tracking

---

## Project Status

This project was built as part of the IT Workshop 2 course at IGDTUW, Delhi for the Master of Computer Applications (MCA) program. The app is functional with live Firebase data and open for future contributions and feature additions.