plugins {
    kotlin("jvm") version "2.2.20-Beta2"
    id("com.gradleup.shadow") version "8.3.0"
    `maven-publish`
    signing
}

group = "com.github.emykr"
version = "1.5.0" // SNAPSHOT 덮어쓰기 방지

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(kotlin("reflect"))
}

kotlin {
    jvmToolchain(17)
}

tasks.jar { enabled = false } // 기본 JAR 비활성화, shadowJar만 생성

tasks.shadowJar {
    archiveClassifier.set("") // all 제거 → 기본 이름으로 사용
}



publishing {
    publications {
        create("gpr", MavenPublication::class) {
            groupId = project.group.toString()
            artifactId = "SommandAPI"
            version = project.version.toString()

            artifact(tasks.shadowJar.get()) {
                builtBy(tasks.shadowJar)
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

