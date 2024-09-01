import com.javiersc.gradle.extensions.version.catalogs.artifact

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
    }
    kotlin {
        jvm {
            features {
                jvmVersion(JavaVersion.VERSION_11)

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
                                        ),
                                    )
                                }
                            }
                        }

                        pluginUnderTestDependencies(
                            hubdle.android.tools.build.gradle,
                            hubdle.jetbrains.kotlin.gradle.plugin,
                            hubdle.plugins.jetbrains.kotlin.plugin.atomicfu.artifact,
                            projects.kopyRuntime,
                        )
                    }
                }
            }

            main {
                dependencies {
                    implementation(gradleKotlinDsl())
                    api(projects.kopyArgs)
                    compileOnly(projects.kopyCompiler)
                    compileOnly(hubdle.jetbrains.kotlin.gradle.plugin)
                }
            }

            testIntegration {
                dependencies {
                    implementation(gradleKotlinDsl())
                    implementation(projects.kopyCompiler)
                    implementation(hubdle.android.tools.build.gradle)
                    implementation(hubdle.jetbrains.kotlin.gradle.plugin)
                }
            }

            testFunctional {
                dependencies {
                    compileOnly(hubdle.plugins.jetbrains.kotlin.plugin.atomicfu.artifact)
                }
            }
        }
    }
}
