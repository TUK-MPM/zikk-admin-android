plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.zikk"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.zikk"
        minSdk = 29
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
    kotlinOptions {
        jvmTarget = "11"
    }
    viewBinding.enable = true
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.retrofit) // Retrofit
    implementation(libs.converter.gson)  // Gson Converter
    implementation(libs.okhttp)  // OkHttp
    implementation(libs.gson) // GSon
    implementation(libs.jwtdecode)

    implementation(libs.coil) // 이미지 로딩
    implementation(libs.kotlinx.coroutines.core) // 코루틴
    implementation(libs.kotlinx.coroutines.android) // 코루틴
    implementation(libs.playservices.location) // 위치 서비스
    implementation(libs.flexbox)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}