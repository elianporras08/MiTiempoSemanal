plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "pe.edu.instituto.ponce.mitiemposemanal"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.senati.mitiemposemanal.eporras"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "0.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Soporte para LocalDateTime y Duration en versiones anteriores a API 26
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}