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
        projectConfig { //
            generateProjectData(true)
        }
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
        compilerOptions { //
            languageVersion(KotlinVersion.KOTLIN_2_0)
        }
        jvm {
            features {
                coroutines()
                gradle {
                    plugin {
                        gradlePlugin {
                            plugins {
                                create("kotlin-kopy") {
                                    id = "com.javiersc.kotlin.kopy"
                                    displayName = "Kopy"
                                    description =
                                        "A compiler plugin to improve how to copy data classes " +
                                            "with a nice DSL in Kotlin."
                                    implementationClass =
                                        "com.javiersc.kotlin.kopy.gradle.plugin.KopyGradlePlugin"
                                    tags.set(
                                        listOf(
                                            "kotlin",
                                            "kopy",
                                            "data class",
                                            "data",
                                            "copy",
                                            "kopy",
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            main {
                dependencies {
                    compileOnly(projects.kopyCompiler)
                    compileOnly(hubdle.jetbrains.kotlin.gradle.plugin)
                }
            }
        }
    }
}
