plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version "2.0.0"
    id("kotlin-parcelize")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs =
                    listOf(
                        "-P",
                        "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=com.mgtvapi.Parcelize",
                    )
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()


    sourceSets {
        commonMain.dependencies {
            // put your multiplatform dependencies here
            implementation(libs.kotlinx.couroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            api(libs.koin.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.multiplatform.settings.noarg)
            implementation(libs.multiplatform.settings.serialization)
            implementation(libs.multiplatform.settings.coroutines)
            implementation(libs.multiplatform.settings)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kermit)
            implementation(project(":shared-core"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.mgtvapi"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
