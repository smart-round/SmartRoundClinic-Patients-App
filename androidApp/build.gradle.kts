plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.gradlePlayPublisher)
}

// No serviceAccountCredentials — CI authenticates via Workload Identity Federation, not a
// downloaded key (our GCP org blocks key creation: iam.disableServiceAccountKeyCreation).
// google-github-actions/auth sets GOOGLE_APPLICATION_CREDENTIALS to a short-lived, federated
// token file; GPP's underlying Google auth library picks that up via standard ADC discovery.
play {
    track.set("internal")
    defaultToAppBundles.set(true)
}

// CI (see .github/workflows/deploy.yml) provides all four via env vars, pointing at a keystore
// decoded from the ANDROID_KEYSTORE_BASE64 secret. Absent locally, so local `assembleRelease`
// keeps producing an unsigned build exactly as before — this only activates in CI.
val hasReleaseSigningEnv = listOf(
    "ANDROID_KEYSTORE_PATH", "ANDROID_KEYSTORE_PASSWORD", "ANDROID_KEY_ALIAS", "ANDROID_KEY_PASSWORD"
).all { System.getenv(it) != null }

android {
    namespace = "ke.co.smartroundclinic.patient.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ke.co.smartroundclinic.patient"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = System.getenv("ANDROID_VERSION_CODE")?.toIntOrNull() ?: 22
        versionName = System.getenv("ANDROID_VERSION_NAME") ?: "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        if (hasReleaseSigningEnv) {
            create("release") {
                storeFile = file(System.getenv("ANDROID_KEYSTORE_PATH")!!)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigningEnv) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.app.update.ktx)
    debugImplementation(libs.compose.uiTooling)
}
