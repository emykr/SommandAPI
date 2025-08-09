plugins {
    kotlin("jvm") version "2.2.20-Beta2"
    id("com.gradleup.shadow") version "8.3.0"
    `maven-publish`
}

group = "sommand.api.v2"
version = "1.2-${System.currentTimeMillis()}" // SNAPSHOT 덮어쓰기 방지

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    // GitHub Packages (프라이빗 패키지도 포함)
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/emykr/SommandAPI")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
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
        register<MavenPublication>("gpr") {
            groupId = "sommand.api.v2"
            artifactId = "SommandAPI"
            version = project.version.toString()
            artifact(tasks.shadowJar.get()) // shadowJar만 퍼블리시
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/emykr/SommandAPI")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
