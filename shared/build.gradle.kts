import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
       namespace = "com.mpi.klass.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_17
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.coil.network.okhttp)
            implementation(project.dependencies.platform(libs.firebase.bom.get()))
            implementation(libs.firebase.ai)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.gitlive.firebase.common)
            implementation(libs.gitlive.firebase.auth)
            implementation(libs.gitlive.firebase.database)
            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.coil.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel"))
            .using(module("androidx.lifecycle:lifecycle-viewmodel:${libs.versions.androidx.lifecycle.get()}"))
        substitute(module("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose"))
            .using(module("androidx.lifecycle:lifecycle-viewmodel-compose:${libs.versions.androidx.lifecycle.get()}"))
        substitute(module("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose"))
            .using(module("androidx.lifecycle:lifecycle-runtime-compose:${libs.versions.androidx.lifecycle.get()}"))
        substitute(module("org.jetbrains.androidx.lifecycle:lifecycle-runtime"))
            .using(module("androidx.lifecycle:lifecycle-runtime:${libs.versions.androidx.lifecycle.get()}"))
        substitute(module("org.jetbrains.androidx.lifecycle:lifecycle-common"))
            .using(module("androidx.lifecycle:lifecycle-common:${libs.versions.androidx.lifecycle.get()}"))
        substitute(module("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-savedstate"))
            .using(module("androidx.lifecycle:lifecycle-viewmodel-savedstate:${libs.versions.androidx.lifecycle.get()}"))
        substitute(module("org.jetbrains.androidx.savedstate:savedstate"))
            .using(module("androidx.savedstate:savedstate:1.4.0"))
        substitute(module("org.jetbrains.androidx.savedstate:savedstate-compose"))
            .using(module("androidx.savedstate:savedstate-compose:1.4.0"))
        substitute(module("org.jetbrains.androidx.navigationevent:navigationevent-compose"))
            .using(module("androidx.navigationevent:navigationevent-compose:1.1.1"))
        substitute(module("org.jetbrains.compose.annotation:annotation-internal"))
            .using(module("androidx.annotation:annotation:1.9.1"))
        substitute(module("org.jetbrains.compose.collection:collection-internal"))
            .using(module("androidx.collection:collection:1.5.0"))
    }
}
