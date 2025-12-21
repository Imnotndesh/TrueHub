plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("org.ajoberstar.grgit") version "5.3.3"
}
val versionInfo = grgit.let { git ->
    val latestTag = git.tag.list().maxByOrNull { it.commit.dateTime }

    if (latestTag != null) {
        val tagName = latestTag.name
        val parts = tagName.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }

        if (parts.size >= 3) {
            val major = parts[0]
            val minor = parts[1]
            val patch = parts[2]

            val versionCode = major * 1000000 + minor * 10000 + patch * 100
            Pair(versionCode, tagName)
        } else {
            Pair(1, tagName)
        }
    } else {
        Pair(1, "v1.0")
    }
}

android {
    namespace = "com.imnotndesh.truehub"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.imnotndesh.truehub"
        minSdk = 33
        targetSdk = 36
        versionCode = versionInfo.first
        versionName = versionInfo.second

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.compose.material3.window.size.class1)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.material3)
    implementation(libs.jeziellago.compose.markdown)
    implementation(libs.androidx.compose.ui.text)
    testImplementation(libs.junit)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    implementation(libs.okhttp)
    implementation(libs.mpandroidchart)
    implementation(libs.moshi.kotlin)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}