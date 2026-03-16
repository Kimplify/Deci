plugins { `kotlin-dsl` }

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.android.gradle.plugin)
    implementation(libs.ktlint.gradle.plugin)
    implementation(libs.bcv.gradle.plugin)
}
