plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)

    alias(libs.plugins.dagger.hilt.plugin)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.example.di"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":feature:bluetooth"))
    implementation(project(":feature:transfer"))


//    //hilt
    implementation(libs.dagger.hilt)
    kapt(libs.dagger.hilt.compiler)
//    //room
    implementation (libs.androidx.room.runtime)
//    //okhttp
    implementation(libs.ok.http)
    implementation (libs.logging.interceptor)
//    //retrofit
    implementation (libs.retrofit2.retrofit)
    implementation (libs.retrofit2.converter.gson)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}