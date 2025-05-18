plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.calorie_tracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.calorie_tracker"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Room database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.7")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    
    // Navigation components - explicitly include all needed artifacts
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation("androidx.navigation:navigation-runtime:2.7.7")
    implementation("androidx.navigation:navigation-common:2.7.7")
    
    // Fragment
    implementation("androidx.fragment:fragment:1.6.2")
    
    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime:2.7.0")
    
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // MPAndroidChart for charts and graphs
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // Gson for JSON serialization/deserialization
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}