plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "org.kimplify.deci.sample"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        targetSdk = 36
        applicationId = "org.kimplify.deci.sample"
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(project(":sample:composeApp"))
    implementation(libs.androidx.activityCompose)
}
