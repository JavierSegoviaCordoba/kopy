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
