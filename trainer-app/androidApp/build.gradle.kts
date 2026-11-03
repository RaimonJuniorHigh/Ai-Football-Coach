import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

// The database address and public key live in local.properties (not in git), so every teammate can point
// at their own database. Without them the app falls back to the in-memory MockTeamRepository.
val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

android {
    namespace = "com.aifootballcoach.trainer"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.aifootballcoach.trainer"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "SUPABASE_URL", "\"${localProps.getProperty("supabase.url", "")}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${localProps.getProperty("supabase.key", "")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.activity.compose)
}
