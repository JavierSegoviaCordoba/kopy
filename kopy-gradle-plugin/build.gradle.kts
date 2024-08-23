import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

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
        compilerOptions { //
            languageVersion(KotlinVersion.KOTLIN_2_0)
        }
        jvm {
            features {
                jvmVersion(JavaVersion.VERSION_17)

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
                            projects.kopyRuntime,
                        )
                    }
                }
            }

            main {
                dependencies {
                    implementation(gradleKotlinDsl())
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

            testFunctional()
        }
    }
}
