plugins {
  alias(libs.plugins.android.application)
}

android {
  namespace = "com.example.to_do_list"
  compileSdk = 35

  packagingOptions {
    resources.excludes.add("META-INF/*")
  }

  defaultConfig {
    applicationId = "com.example.to_do_list"
    minSdk = 21
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies {

  // UI Components
  implementation(libs.recyclerview)
  implementation(libs.cardview)
  implementation(libs.material)

  // Data
  implementation(libs.gson)

  // Google Calendar API
  implementation(libs.google.api.client)
  implementation(libs.google.api.services.calendar)
  implementation(libs.google.auth.library)
  implementation(libs.play.services.auth.v2130)
  implementation(libs.google.api.client.android)


  implementation(libs.appcompat)
  implementation(libs.material)
  implementation(libs.activity)
  implementation(libs.constraintlayout)
  implementation(libs.recyclerview)
  implementation(libs.cardview)
  implementation(libs.gson)
  implementation(libs.google.api.client)
  implementation(libs.google.api.services.calendar)
  implementation(libs.google.auth.library)
  implementation(libs.play.services.auth)
  testImplementation(libs.junit)
  androidTestImplementation(libs.ext.junit)
  androidTestImplementation(libs.espresso.core)
}