plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics") version "2.9.1"
}

android {
    namespace = "alragar2.isi3.uv.flagflash"
    compileSdk = 34

    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "com.google.android.gms" && requested.name == "play-services-ads")
            {
                useVersion("23.2.0")
            }
        }
    }
    defaultConfig {
        applicationId = "alragar2.isi3.uv.flagflash"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.0"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.room:room-common:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.0")
    implementation("com.google.firebase:firebase-crashlytics:18.0.0")
    implementation("com.google.firebase:firebase-analytics:20.0.1")
    implementation("com.google.firebase:firebase-messaging:23.0.0")
    implementation ("com.google.firebase:firebase-core:20.0.2") // Reemplaza con la última versión
    implementation ("com.google.android.gms:play-services-ads:21.0.1") // Reemplaza con la última versión
    implementation("com.google.android.gms:play-services-ads-lite:23.2.0")
    implementation("com.google.android.gms:play-services-ads-identifier:17.0.0")
    implementation("com.google.android.gms:play-services-ads-base:23.2.0")
    implementation("com.google.android.gms:play-services-measurement:22.0.2")
    implementation("com.google.android.gms:play-services-measurement-base:22.0.2")
    implementation("com.google.android.gms:play-services-measurement-impl:22.0.2")
    implementation("com.google.android.gms:play-services-measurement-sdk:22.0.2")
    implementation("com.google.android.gms:play-services-measurement-sdk-api:22.0.2")
    implementation("com.google.firebase:firebase-auth-ktx:23.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation ("com.afollestad.material-dialogs:core:3.3.0")
    implementation ("com.afollestad.material-dialogs:color:3.3.0")
    implementation ("com.google.guava:guava:30.1.1-android")
    implementation ("com.firebaseui:firebase-ui-auth:8.0.0")
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-analytics:21.2.0")
    implementation("com.google.firebase:firebase-crashlytics:18.3.5")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    //viewmodel
    //implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    kapt("androidx.room:room-compiler:2.6.1")


}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.7.1")

    }
}
