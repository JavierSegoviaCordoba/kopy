import com.javiersc.gradle.extensions.version.catalogs.artifact

hubdle {
    config {
        analysis()
        coverage()
        documentation { //
            api()
        }
        explicitApi()
        projectConfig { //
            generateProjectData(true)
            generateAdditionalData {
                const(
                    name = "compilerExtensions",
                    type = "String",
                    value = "${hubdle.javiersc.kotlin.compiler.extensions.get()}",
                )
            }
        }
        publishing()
    }
    kotlin {
        jvm {
            features {
                jvmVersion(JavaVersion.VERSION_11)
                kotest()
            }

            main {
                dependencies {
                    implementation(gradleKotlinDsl())
                    api(projects.kopyArgs)
                    implementation(libs.javiersc.kotlin.compiler.gradle.extensions)
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
                        tags.set(listOf("kotlin", "kopy", "data class", "data", "copy", "kopy"))
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

val testTasks = tasks.withType<Test>()

rootProject.allprojects {
    testTasks.configureEach { //
        dependsOn(tasks.named { it == "publishToMavenLocalTest" })
    }
}
