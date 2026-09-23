plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

fun versionCodeFrom(versionName: String): Int {
    val match = Regex("""[vV]?(\d+)\.(\d+)\.(\d+)(?:[-_ ]?([A-Za-z]+)\.?(\d+)?)?""").find(versionName) ?: return 0
    val (major, minor, patch) = match.destructured
    val stage = when (match.groupValues[4].lowercase()) {
        "beta" -> 1
        "rc" -> 2
        else -> 3
    }
    val prereleaseNumber = match.groupValues[5].toIntOrNull() ?: 0
    return (major.toInt() * 100 + minor.toInt()) * 10_000 + patch.toInt() * 100 + stage * 10 + prereleaseNumber
}

val appVersionName: String = (findProperty("appVersionName") as String?)?.takeIf { it.isNotBlank() } ?: "0.8.1-rc"
val appVersionCode: Int = (findProperty("appVersionCode") as String?)?.toIntOrNull()
    ?: versionCodeFrom(appVersionName).takeIf { it > 0 }
    ?: 1

val versionPropertiesFile = layout.buildDirectory.file("version.properties")

tasks.register("writeVersionProperties") {
    inputs.property("versionName", appVersionName)
    inputs.property("versionCode", appVersionCode)
    outputs.file(versionPropertiesFile)
    doLast {
        versionPropertiesFile.get().asFile.writeText(
            "versionName=$appVersionName\nversionCode=$appVersionCode\n"
        )
    }
}

android {
    namespace = "com.imnotndesh.truehub"
    compileSdk = 37

    defaultConfig {
        minSdk = 33
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    android.buildFeatures.buildConfig = true
    flavorDimensions.add("store")

    productFlavors {
        create("playstore") {
            dimension = "store"
            applicationId = "com.imnotndesh.truehub.app"
            buildConfigField("Boolean", "IS_PLAYSTORE_BUILD", "true")
        }
        create("github") {
            dimension = "store"
            applicationId = "com.imnotndesh.truehub"
            buildConfigField("Boolean", "IS_PLAYSTORE_BUILD", "false")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86_64")
            isUniversalApk = false
        }
    }
    buildFeatures {
        compose = true
    }
    ksp {
        arg("appfunctions:aggregateAppFunctions", "true")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.appfunctions)
    ksp(libs.androidx.appfunctions.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.compose.material3.window.size.class1)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.preview)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.coil.svg)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.accompanist.permissions)
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
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.lifecycle.service)
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
