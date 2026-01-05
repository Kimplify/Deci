@file:OptIn(ExperimentalWasmDsl::class)

import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
}

val libsCatalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val deciVersion: String = libsCatalog.findVersion("version").get().requiredVersion

kotlin {
    jvmToolchain(17)

    androidTarget { publishLibraryVariants("release") }
    jvm()
    wasmJs { browser() }
    js { browser() }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.cedar.logger)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }

        jsMain.dependencies {
            implementation(npm("decimal.js", "10.6.0"))
        }

        wasmJsMain.dependencies {
            implementation(npm("decimal.js", "10.6.0"))
        }
    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
    }

}

android {
    namespace = "org.kimplify.deci"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }
}

//Publishing your Kotlin Multiplatform library to Maven Central
//https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-publish-libraries.html
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("org.kimplify", "deci", deciVersion)

    pom {
        name = "Deci"
        description = "Precise decimal arithmetic for Kotlin Multiplatform projects, shipping high-precision operations across every target."
        url = "https://github.com/Kimplify/Deci"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
            }
        }

        developers {
            developer {
                id = "merkost"
                name = "Konstantin Merenkov"
                email = "merkostdev@gmail.com"
            }

            developer {
                id = "diogocavaiar"
                name = "Diogo Cavaiar"
                email = "cavaiarconsulting@gmail.com"
            }
        }

        scm {
            connection = "scm:git:https://github.com/Kimplify/Deci.git"
            developerConnection = "scm:git:ssh://git@github.com/Kimplify/Deci.git"
            url = "https://github.com/Kimplify/Deci"
        }
    }
}