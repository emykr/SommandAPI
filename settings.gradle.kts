import org.gradle.kotlin.dsl.maven

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "papermc-repo"
        }
        maven {
            url = uri("https://jitpack.io")
         //    credentials.username = "jp_fc8ju7dndk5asq69k9j5duaibj"
        }
        maven {
            url = uri("https://maven.fabricmc.net/")
        }

    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"

}


rootProject.name = "SommandAPI"

