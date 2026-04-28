import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.lofo"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.lofo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // ADD — read from local.properties so secrets stay off GitHub
        val localProps = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            localProps.load(localPropsFile.inputStream())
        }

        buildConfigField(
            "String",
            "CLOUDINARY_CLOUD_NAME",
            "\"${localProps.getProperty("cloudinary.cloud_name", "")}\""
        )

        buildConfigField(
            "String",
            "CLOUDINARY_API_KEY",
            "\"${localProps.getProperty("cloudinary.api_key", "")}\""
        )

        buildConfigField(
            "String",
            "CLOUDINARY_UPLOAD_PRESET",
            "\"${localProps.getProperty("cloudinary.upload_preset", "")}\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        buildConfig = true
    }

//    sourceSets {
//        getByName("main") {
//            java {
//                srcDirs(
//                    "src\\main\\java",
//                    "src\\main\\java\\model",
//                    "src\\main\\java\\repository",
//                    "src\\main\\java\\home",
//                    "src\\main\\java\\addpost"
//                )
//            }
//        }
//    }

//    buildFeatures {
//        viewBinding = true
//    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.splashscreen)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Import the Firebase BoM (manages all Firebase versions together)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // ViewModel + LiveData
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // When using the BoM, don't specify versions in Firebase dependencies
    implementation(libs.firebase.analytics)

    implementation(libs.firebase.firestore)
    implementation(libs.glide)
    implementation(libs.cloudinary)
    annotationProcessor(libs.glide.compiler)

}