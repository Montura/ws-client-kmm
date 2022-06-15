val ktorVersion: String by project

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    kotlin("plugin.serialization") version "1.6.21"
//    id("com.android.library")
}

version = "1.0"

kotlin {
//    android()
//    iosX64()
    jvm()
    iosArm64()
//    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-websockets:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
//                implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.7.0")
                implementation(kotlin("stdlib-jdk8"))
            }
        }
//        val androidMain by getting {
//            dependencies {
//                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
//            }
//        }
//        val androidTest by getting
//        val iosX64Main by getting
        val iosArm64Main by getting
//        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
////            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
////            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
            }
        }
    }
}

//android {
//    compileSdk = 31
//    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
//    defaultConfig {
//        minSdk = 23
//        targetSdk = 31
//    }
//}