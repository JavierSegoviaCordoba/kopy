import com.javiersc.kotlin.stdlib.notContain
import java.io.File

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
        publishing {
            maven {
                repositories { //
                    mavenLocalTest()
                }
            }
        }
    }
    kotlin {
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
                jvmVersion(JavaVersion.VERSION_1_8)
                kotest.enabled(true)
            }
            main { //
                dependencies { //
                    implementation(hubdle.javiersc.kotlin.compiler.extensions)
                    implementation(projects.kopyArgs)
                    implementation(projects.kopyRuntime)
                }
            }
        }
    }
}

// TODO: Move to Hubdle
tasks.register<Task>("deleteAllTextTestFiles") {
    doNotTrackState("Not cacheable")
    pluginManager.withPlugin(hubdle.plugins.jetbrains.kotlin.jvm.get().pluginId) {
        inputs.dir(layout.projectDirectory.dir("test-data"))
        outputs.dir(layout.projectDirectory.dir("test-data"))
        doLast {
            inputs
                .files
                .files.filter {
                    it.path.notContain("TODO") && it.path.notContain("todo") && it.extension == "txt"
                }
                .forEach(File::delete)
        }
    }
}
