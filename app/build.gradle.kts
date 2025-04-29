import com.android.build.api.dsl.BuildType

// Helper function to make adding buildConfigFields cleaner
fun BuildType.buildConfigStringField(name: String, value: String) {
    buildConfigField("String", name, "\"$value\"")
}

fun BuildType.buildConfigBooleanField(name: String, value: Boolean) {
    buildConfigField("Boolean", name, value.toString())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlinx.serialization)
    id("dagger.hilt.android.plugin")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}



android {
    namespace = "com.ganainy.gymmasterscompose"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ganainy.gymmasterscompose"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE.txt",
                "META-INF/DEPENDENCIES",
                "META-INF/*.kotlin_module",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigBooleanField("IS_APP_IN_DEBUG_MODE", false)
        }

        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigStringField( "SAMPLE_WORKOUT_IMAGE_URL", "\"https://firebasestorage.googleapis.com/v0/b/gym-masters.appspot.com/o/workouts%2F-Myv9QdZ0nK7WYV3JpQJk%2Fimage?alt=media&token=0e0f9f5b-9d3e-4e1e-94f1-6c9e0d7a5f0c\"")

            buildConfigBooleanField("IS_APP_IN_DEBUG_MODE", false)

            signingConfig = signingConfigs.getByName("debug")
        }

        // ---  custom build type here for testing purposes ---
        create("stagingDebug") {
            //Inherit from debug and override
            initWith(buildTypes.getByName("debug"))

            buildConfigStringField( "SAMPLE_WORKOUT_IMAGE_URL", "\"https://firebasestorage.googleapis.com/v0/b/gym-masters.appspot.com/o/workouts%2F-Myv9QdZ0nK7WYV3JpQJk%2Fimage?alt=media&token=0e0f9f5b-9d3e-4e1e-94f1-6c9e0d7a5f0c\"")

            // Override or add specific flags
            buildConfigBooleanField("IS_APP_IN_DEBUG_MODE", true)
        }
    }


    secrets {
        propertiesFileName = "secrets.properties"
        defaultPropertiesFileName = "local.defaults.properties"
    }

}



kapt {
    correctErrorTypes = true
    arguments {
        arg("dagger.processingX", "print")
    }
}

dependencies {

    //navigation
    implementation(libs.androidx.navigation.compose)
    // Places
    implementation(libs.places)
    // Map
    implementation(libs.play.services.maps)
    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    //Material Icons
    implementation(libs.androidx.material.icons.extended)
    //Coil
    implementation(libs.coil.compose)

    //Dagger Hilt
    implementation(libs.hilt.android)
    implementation(libs.testng)
    implementation(libs.androidx.ui.test.junit4.android)
    implementation(libs.androidx.runner)
    implementation(libs.hilt.android.testing)
    implementation(libs.core)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.ui.text.google.fonts)
    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.ext.junit)
    kapt(libs.dagger.hilt.android.compiler)
    implementation (libs.androidx.hilt.navigation.compose)

    //time ago
    implementation (libs.timeago)

    //  testing
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.test.manifest)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Optional but recommended for better assertions
    androidTestImplementation(libs.kotlintest.assertions)

    // Compose UI testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.6") {
        exclude(group = "androidx.test.ext", module = "junit")
        exclude(group = "androidx.test.espresso", module = "espresso-core")
    }

    // retrofit
    implementation (libs.squareup.retrofit)
    // gson converter
    implementation (libs.squareup.converter.gson)
    //okhttp
    implementation (libs.okhttp)
    //http interceptor
    implementation(libs.logging.interceptor)

    //glide for handling gifs
    implementation (libs.glide)
    ksp (libs.compiler)

    //Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    debugImplementation(libs.androidx.ui.tooling)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.serialization.json)

    //Mockito  for unit tests
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    //  Robolectric framework, which provides a simulated Android environment for unit tests.
    testImplementation(libs.robolectric)
    //Default
    implementation(libs.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // to enable preview function in stagingDebug build type
    "stagingDebugImplementation"(libs.ui.tooling)
}



// Add version alignment strategy
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "androidx.test" ||
            requested.group == "androidx.test.ext" ||
            requested.group == "androidx.test.espresso") {
            requested.version?.let { useVersion(it) }
        }
    }

}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

