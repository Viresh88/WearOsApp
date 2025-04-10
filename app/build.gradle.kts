plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("kotlin-android")


}

android {

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

    namespace = "com.example.wearosapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.wearosapp"
        minSdk = 30
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
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
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation(libs.play.services.wearable)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.wear)
    implementation(libs.databinding.compiler.common)
    implementation(libs.room.common)

    implementation("androidx.room:room-runtime:2.5.0")
    implementation("androidx.room:room-paging:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")
    implementation(libs.foundation.android)
    implementation(libs.play.services.maps)
    implementation(libs.ui.test.android)
    implementation(libs.androidthings)
    implementation(libs.media3.common.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.play.services.location)
    ksp("androidx.room:room-compiler:2.5.0")
    implementation("androidx.palette:palette-ktx:1.0.0")




    //VIEW MODEL & LIVE DATA
    //noinspection GradleDependency
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    //noinspection GradleDependency
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    //RECYCLER VIEW
    //noinspection GradleDependency
    implementation("androidx.recyclerview:recyclerview:1.3.1")

    //Gson
    implementation("com.google.code.gson:gson:2.11.0")

    //SWEEP LAYOUT
    implementation("it.xabaras.android:recyclerview-swipedecorator:1.2.2")

    //PERMISSION
    implementation("pub.devrel:easypermissions:3.0.0")

    //Eventbus
    implementation("org.greenrobot:eventbus:3.3.1")

    //RX JAVA
    implementation("io.reactivex.rxjava3:rxjava:2.5.0")


    implementation("io.reactivex.rxjava3:rxandroid:3.0.0")
//
//    //TEST
//    testImplementation("junit:junit:4.13.2")
//    androidTestImplementation("androidx.test.ext:junit:1.2.1")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//    implementation("androidx.cardview:cardview:1.0.0")

}



