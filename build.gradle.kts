plugins {
    kotlin("jvm") version "2.2.20-Beta2"
    id("com.gradleup.shadow") version "8.3.0"
//    id("fabric-loom") version "1.11-SNAPSHOT"
    `maven-publish`
    signing
}

group = "com.github.emykr"
version = "1.7.2" // SNAPSHOT 덮어쓰기 방지

//repositories {
//    mavenCentral()
//    maven("https://repo.papermc.io/repository/maven-public/") {
//        name = "papermc-repo"
//    }
//    maven("https://jitpack.io")
//}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(kotlin("reflect"))
    implementation("net.fabricmc.fabric-api:fabric-api:0.92.6+1.20.1")
    implementation("com.mojang:brigadier:1.0.18")
    implementation ("net.fabricmc:fabric-loader:0.17.3")
    implementation("net.fabricmc:yarn:1.20.1+build.10")
//    implementation("com.mojang:minecraft:1.20.1")

    //mplementation("com.github.emykr:SommandAPI:1.20.1-SNAPSHOT")
}

kotlin {
    jvmToolchain(17)
}

//tasks.jar { enabled = false } // 기본 JAR 비활성화, shadowJar만 생성

tasks.shadowJar {
    archiveClassifier.set("") // all 제거 → 기본 이름으로 사용
}



publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("SommandAPI")
                description.set("API for XYZ")
                url.set("https://github.com/emykr/SommandAPI")
            }
        }
    }
}

