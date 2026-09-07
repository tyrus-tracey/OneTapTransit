import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.onetaptransit"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.onetaptransit"
        minSdk = 36
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val keystoreFile = project.rootProject.file("apikeys.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())

        val TRANSLINK_API_KEY = properties.getProperty("TRANSLINK_API_KEY") ?: ""

        android.buildFeatures.buildConfig = true
        buildConfigField(
            type = "String",
            name = "TRANSLINK_API_KEY",
            value = TRANSLINK_API_KEY
        )
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    val v_kotlinx_io    = "0.9.1"
    val v_gtfs_bindings = "0.2.0"
    val v_viewmodel     = "2.11.0"
    val v_kzip          = "2.0.0"
    val v_room          = "3.0.1"
    val v_dagger_hilt   = "2.60.1"
    val v_kotlin_csv    = "2.0.0"
    val v_hilt_work     = "1.4.0"
    val v_workmanager   = "2.8.1"
    //noinspection UseTomlInstead
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:$v_kotlinx_io")
    //noinspection UseTomlInstead
    implementation("org.mobilitydata:gtfs-realtime-bindings:$v_gtfs_bindings")
    //noinspection UseTomlInstead
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$v_viewmodel")
    //noinspection UseTomlInstead
    implementation("de.jonasbroeckmann.kzip:kzip:$v_kzip")
    //noinspection UseTomlInstead
    implementation("androidx.room3:room3-runtime:$v_room")
    //noinspection UseTomlInstead
    ksp("androidx.room3:room3-compiler:$v_room")
    //noinspection UseTomlInstead
    implementation("com.google.dagger:hilt-android:$v_dagger_hilt")
    //noinspection UseTomlInstead
    ksp("com.google.dagger:hilt-android-compiler:$v_dagger_hilt")
    //noinspection UseTomlInstead
    implementation("com.jsoizo:kotlin-csv:$v_kotlin_csv")
    //noinspectoin UseTomlInstead
    implementation("androidx.work:work-runtime-ktx:$v_workmanager")
    //noinspectoin UseTomlInstead
    implementation("androidx.hilt:hilt-work:$v_hilt_work")
    //noinspection UseTomlInstead
    ksp("androidx.hilt:hilt-compiler:$v_hilt_work")

}