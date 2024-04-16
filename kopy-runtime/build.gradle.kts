import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

hubdle {
    config {
        analysis()
        coverage()
        documentation { //
            api()
        }
        explicitApi()
        format.isEnabled = false
        publishing()
        versioning {
            semver {
                mapVersion { gradleVersion ->
                    val kotlinVersion = getKotlinPluginVersion()
                    val metadata =
                        gradleVersion.metadata?.let { "$kotlinVersion-$it" } ?: kotlinVersion
                    "${gradleVersion.copy(metadata = metadata)}"
                }
            }
        }
    }
    kotlin {
        multiplatform {
            common {
                main {
                    dependencies { //
                        implementation(hubdle.jetbrains.kotlinx.atomicfu)
                    }
                }
            }
            jvm()
        }
    }
}
