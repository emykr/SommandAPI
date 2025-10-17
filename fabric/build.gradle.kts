plugins {
    kotlin("jvm") version "2.2.20-Beta2"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "com.github.emykr.fabric"
version = rootProject.version

//repositories {
//    mavenCentral()
//    // Fabric maven for Fabric API
//    maven {
//        url = uri("https://maven.fabricmc.net/")
//    }
//}

dependencies {
    implementation(project(":")) // depend on root SommandAPI
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.mojang:brigadier:1.0.18")

    // Fabric API - use implementation for development/build so types are resolvable
    implementation("net.fabricmc.fabric-api:fabric-api:0.85.0+1.20.1")
}

kotlin {
    jvmToolchain(17)
}
