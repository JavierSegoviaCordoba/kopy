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
