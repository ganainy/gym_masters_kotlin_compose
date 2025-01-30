import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
}

val properties = Properties()
properties.load(project.rootProject.file("local.properties").inputStream())


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

        buildConfigField("String", "RAPID_API_KEY", properties.getProperty("RAPID_API_KEY"))


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


}



kapt {
    correctErrorTypes = true
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
    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.ext.junit)
    kapt(libs.dagger.hilt.android.compiler)
    implementation (libs.androidx.hilt.navigation.compose)

    //time ago
    implementation (libs.timeago)

    //pull to refresh
    implementation(libs.androidx.material)

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
    kapt (libs.compiler)

    //Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    debugImplementation(libs.androidx.ui.tooling)
    ksp("androidx.room:room-compiler:2.5.0")

    //Mockito  for unit tests
    testImplementation("org.mockito:mockito-core:5.4.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    //  Robolectric framework, which provides a simulated Android environment for unit tests.
    testImplementation("org.robolectric:robolectric:4.10.3")
    //Default
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
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

