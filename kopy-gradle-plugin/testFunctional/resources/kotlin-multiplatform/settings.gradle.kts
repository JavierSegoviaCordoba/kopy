import java.io.File

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven(
            url = File(System.getProperty("user.home")).resolve("mavenLocalTest/repository").toURI()
        ) {
            name = "mavenLocalTest"
        }
        maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven(
            url = File(System.getProperty("user.home")).resolve("mavenLocalTest/repository").toURI()
        ) {
            name = "mavenLocalTest"
        }
        maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
}

include(
    // ":lib-a",
    // ":lib-b",
)
