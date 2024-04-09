import com.javiersc.gradle.properties.extensions.getStringProperty
import com.javiersc.gradle.version.GradleVersion
import com.javiersc.gradle.version.isSnapshot
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
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
        languageSettings { //
            experimentalContracts()
        }
        projectConfig()
        publishing()
        versioning {
            semver {
                mapVersion { gradleVersion ->
                    gradleVersion.mapIfKotlinVersionIsProvided(getKotlinPluginVersion())
                }
            }
        }
    }
    kotlin {
        compilerOptions { //
            languageVersion(KotlinVersion.KOTLIN_2_0)
        }
        jvm {
            features { //
                compiler {
                    mainClass.set("com.javiersc.kotlin.kopy.compiler.GenerateKotlinCompilerTestsKt")
                    generateTestOnSync(false)
                    testDependencies(
                        hubdle.javiersc.kotlin.stdlib,
                        hubdle.jetbrains.kotlinx.atomicfu,
                    )
                    testProjects(projects.kopyRuntime)
                }
                contextReceivers()
            }
            main { //
                dependencies { //
                    implementation(hubdle.javiersc.kotlin.compiler.extensions)
                    implementation(projects.kopyRuntime)
                }
            }
        }
    }
}

fun GradleVersion.mapIfKotlinVersionIsProvided(kotlinVersion: String): String {
    val major: Int = major
    val minor: Int = minor
    val patch: Int = patch

    val isKotlinDevVersion = kotlinVersion.isKotlinDevVersion() || kotlinVersion.contains("dev")
    val isSnapshotStage = isSnapshot || getStringProperty("semver.stage").orNull?.isSnapshot == true

    val version: String =
        if (isKotlinDevVersion || isSnapshotStage) {
            "$major.$minor.$patch+$kotlinVersion-SNAPSHOT"
        } else {
            "$major.$minor.$patch+$kotlinVersion"
        }
    return version
}

fun String.isKotlinDevVersion(): Boolean =
    matches(Regex("""(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)-dev-(0|[1-9]\d*)"""))
