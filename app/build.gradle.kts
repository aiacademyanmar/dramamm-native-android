plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
 namespace = "asia.aaii.dramamm"
 compileSdk = 35
 defaultConfig { applicationId = "asia.aaii.dramamm"; minSdk = 24; targetSdk = 35; versionCode = 4; versionName = "2.1.1" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
 buildTypes { release { isMinifyEnabled = false } }
}
