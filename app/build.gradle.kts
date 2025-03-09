plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "hcmute.edu.vn.huuloc.steptracking"
    compileSdk = 35

    defaultConfig {
        applicationId = "hcmute.edu.vn.huuloc.steptracking"
        minSdk = 26
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
    buildToolsVersion = "35.0.0"
    ndkVersion = "28.0.12674087 rc2"
}

dependencies {
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    
    // Add the lombok-mapstruct-binding to help Room work with Lombok
    implementation("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    
    implementation("com.google.android.gms:play-services-fitness:21.1.0")
    implementation("com.google.android.gms:play-services-auth:20.5.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}