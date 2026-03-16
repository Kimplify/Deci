plugins {
    id("deci.kmp.library")
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlinx.serialization)
}

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val deciVersion: String = libsCatalog.findVersion("version").get().requiredVersion

kotlin {
    sourceSets {
        // Custom intermediate source set for Linux + Windows (non-Apple native)
        val nonAppleNativeMain by creating { dependsOn(nativeMain.get()) }
        val linuxX64Main by getting { dependsOn(nonAppleNativeMain) }
        val mingwX64Main by getting { dependsOn(nonAppleNativeMain) }

        val nonAppleNativeTest by creating { dependsOn(commonTest.get()) }
        val linuxX64Test by getting { dependsOn(nonAppleNativeTest) }
        val mingwX64Test by getting { dependsOn(nonAppleNativeTest) }

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotest.property)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.cedar.logger)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.cedar.logger)
        }

        jsMain.dependencies {
            implementation(npm("decimal.js", "10.6.0"))
            implementation(libs.cedar.logger)
        }

        wasmJsMain.dependencies {
            implementation(npm("decimal.js", "10.6.0"))
            implementation(libs.cedar.logger)
        }

        val iosMain by getting
        iosMain.dependencies {
            implementation(libs.cedar.logger)
        }
    }
}

android {
    namespace = "org.kimplify.deci"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }
}

// Publishing your Kotlin Multiplatform library to Maven Central
// https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-publish-libraries.html
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
