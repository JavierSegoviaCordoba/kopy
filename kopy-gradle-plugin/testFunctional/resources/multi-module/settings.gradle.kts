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
    }
}

include(
    ":lib-a",
    ":lib-b",
)
