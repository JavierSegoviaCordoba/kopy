import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
        jvm {
            features {
                contextReceivers()
            }
        }
    }
}
