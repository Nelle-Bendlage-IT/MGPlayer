plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version "2.0.0"
    alias(libs.plugins.kotlinCocoapods)
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

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        framework {
            baseName = "mgtvApi"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // put your multiplatform dependencies here
            implementation(libs.kotlinx.couroutines.core)
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
