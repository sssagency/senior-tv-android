plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.seniortv.player"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.seniortv.player"
        minSdk = 26
        targetSdk = 34
        versionCode = 100
        versionName = "1.0.0-v2.6"

        val playerUrl = (project.findProperty("seniorPlayerUrl") as String?)
            ?: "https://CHANGE-TO-YOUR-PLAYER.pages.dev"
        val escaped = playerUrl.replace("\\", "\\\\").replace("\"", "\\\"")
        buildConfigField("String", "PLAYER_URL", "\"${escaped}\"")
    }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
