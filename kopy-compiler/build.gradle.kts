import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
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
        languageSettings { //
            experimentalContracts()
        }
        projectConfig()
        publishing()
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
                    testDependencies(hubdle.javiersc.kotlin.stdlib)
                    testProjects(projects.kopyRuntime)
                }
                contextReceivers()
                serialization()
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
