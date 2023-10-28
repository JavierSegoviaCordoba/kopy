import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

hubdle {
    config {
        analysis()
        coverage()
        documentation { //
            api()
        }
        explicitApi()
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
                    testDependencies(hubdle.javiersc.kotlin.kotlinStdlib)
                    testProjects(projects.kopyRuntime)
                }
                contextReceivers()
                serialization()
            }
            main { //
                dependencies { //
                    implementation(hubdle.javiersc.kotlin.kotlinCompilerExtensions)
                    implementation(projects.kopyRuntime)
                }
            }
        }
    }
}
